package com.liverpool.compras.ingest.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.liverpool.compras.ingest.RestInvoker;
import com.liverpool.compras.ingest.configuration.ApplicationConfiguration;
import com.liverpool.compras.ingest.constants.ComprasIngestConstants;
import com.liverpool.compras.ingest.dao.beans.Item;
import com.liverpool.compras.ingest.dao.beans.Order;
import com.liverpool.compras.ingest.dao.beans.OrderStatusMapBean;
import com.liverpool.compras.ingest.dao.beans.SkuDetails;
import com.liverpool.compras.ingest.dao.beans.Skus;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ComprasIngestUtils {

	private RestInvoker restInvoker;
	private ApplicationConfiguration applicationConfiguration;

	public ComprasIngestUtils(RestInvoker restInvoker, ApplicationConfiguration applicationConfiguration) {
		super();
		this.restInvoker = restInvoker;
		this.applicationConfiguration = applicationConfiguration;
	}

	public Item updateItemBean(Item ingestItem, Order orderIngest) {
		log.info("Start :: ComprasIngestUtils.updateItemBean()");
		ingestItem.setId(orderIngest.getSourceShipId() + "-" + ingestItem.getSourceItemId());
		ingestItem.setRemissionId(orderIngest.getOrderRef());
		ingestItem.setCustomerId(orderIngest.getCustomerId());
		log.info("End :: ComprasIngestUtils.updateItemBean()");
		return ingestItem;
	}

	public Order updateOrderBean(Order orderIngestBean) {
		log.info("Start :: ComprasIngestUtils.updateOrderBean()");
		populateItemIdstoList(orderIngestBean);
		log.info("End :: ComprasIngestUtils.updateOrderBean()");
		return orderIngestBean;
	}

	public Item updatePosItemBean(Item ingestItem, Order orderIngestBean) {
		log.info("Start :: ComprasIngestUtils.updatePosItemBean()");
		ingestItem.setId(orderIngestBean.getOrderRef() + "-" + ingestItem.getSkuId());
		ingestItem.setRemissionId(orderIngestBean.getOrderRef());
		ingestItem.setCustomerId(orderIngestBean.getCustomerId());
		log.info("End :: ComprasIngestUtils.updatePosItemBean()");
		return ingestItem;
	}

	public void populateItemIdstoList(Order orderIngestBean) {
		log.info("Start :: ComprasIngestUtils.populateItemIdstoList()");
		List<Item> items = orderIngestBean.getItems();
		List<String> itemIds = new ArrayList<>();
		if (items != null && !items.isEmpty()) {
			for (Item item : items) {
				itemIds.add(orderIngestBean.getSourceShipId() + "-" + item.getSourceItemId());
			}
		}
		orderIngestBean.setItemIds(itemIds);
		log.info("End :: ComprasIngestUtils.populateItemIdstoList()");
	}

	/**
	 * This method used to get the SKU details from the search facade
	 * 
	 * @param skuId - String
	 * @return skuDetails - SkuDetails
	 */
	public SkuDetails getSkuDetails(Map<String, String> headersMap,String skuId) {
		log.info("Start :: ComprasIngestUtils.getSkuDetails() :: {}", skuId);
		String serviceUrl = applicationConfiguration.getSkuDetailApiUrl() + skuId;
		try {
			JsonNode jsonNode = restInvoker.callServiceGet(serviceUrl, headersMap);
			if (jsonNode != null && jsonNode.get(ComprasIngestConstants.RECORDS) != null
					&& jsonNode.get(ComprasIngestConstants.RECORDS).size() > 0
					&& (ObjectNode) jsonNode.get(ComprasIngestConstants.RECORDS).get(0) != null) {
				ObjectNode recordData = (ObjectNode) jsonNode.get(ComprasIngestConstants.RECORDS).get(0);
				ObjectNode allMeta = recordData.get(ComprasIngestConstants.ALLMETA) != null
						? (ObjectNode) recordData.get(ComprasIngestConstants.ALLMETA)
						: null;
				SkuDetails skuDetails = new SkuDetails();
				if (allMeta != null && allMeta.get(ComprasIngestConstants.VARIANTS) != null
						&& allMeta.get(ComprasIngestConstants.VARIANTS).size() > 0) {
					for (JsonNode variantNode : allMeta.get(ComprasIngestConstants.VARIANTS)) {
						skuDetails = populateSKUDetails(variantNode, skuDetails, allMeta, skuId);
						if (skuDetails != null)
							return skuDetails;
					}
				}
			}
		} catch (JsonProcessingException e) {
			log.error("Exception while calling sku fetch service: {}", e.getMessage());
		}
		log.info("End :: ComprasIngestUtils.getSkuDetails()");
		return null;
	}

	public SkuDetails populateSKUDetails(JsonNode variantNode, SkuDetails skuDetails, ObjectNode allMeta,
			String skuId) {
		log.info("Start :: ComprasIngestUtils.populateSKUDetails() :: {}", skuId);
		if (variantNode.get(ComprasIngestConstants.SKUID) != null
				&& skuId.equalsIgnoreCase(variantNode.get(ComprasIngestConstants.SKUID).asText())) {
			skuDetails.setColor(variantNode.get(ComprasIngestConstants.COLOR).asText());
			skuDetails.setDepartmentId(allMeta.get(ComprasIngestConstants.DEPARTMENT).asText());
			skuDetails.setMaterialGroup(allMeta.get(ComprasIngestConstants.MATERIALGROUP).asText());
			skuDetails.setSize(variantNode.get(ComprasIngestConstants.SIZE).asText());
			skuDetails.setSmallImage(variantNode.get(ComprasIngestConstants.SMALLIMAGE).asText());
			return skuDetails;
		}
		log.info("End :: ComprasIngestUtils.populateSKUDetails() :: {}", skuId);
		return skuDetails;
	}

	/**
	 * If any of the dates as parameters is not defined it will return true If the
	 * store time is before or equals than the new timestamp from soms it will
	 * return true otherwise will be false.
	 * 
	 * @param storedTimestamp
	 * @param timestampInSomsResponse
	 * @return boolean
	 */
	public boolean checkValidDates(Date storedTimestamp, Date timestampInSomsResponse) {
		if (null == storedTimestamp || null == timestampInSomsResponse
				|| storedTimestamp.before(timestampInSomsResponse) || storedTimestamp.equals(timestampInSomsResponse)) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	public Date convertToDate(String pStringDate) {
		log.info("STARTS::ConvertToDate()");
		Date startDate = null;
		if (org.apache.commons.lang3.StringUtils.isNotBlank(pStringDate)) {
			DateFormat df = new SimpleDateFormat(ComprasIngestConstants.SMAPLE_DATE_FORMAT);
			try {
				startDate = df.parse(pStringDate);
				return startDate;

			} catch (ParseException exe) {
				log.error("parse exceptions" + exe);
			}
			log.info("END::ConvertToDate()");
		}
		return startDate;
	}

	/**
	 * calculate and return EDD for a commerce item based SOMS web service response
	 * 
	 * @param pStartDate - String
	 * @param pEndDate   - String
	 * @param pItemType  - String
	 * @return date as string
	 */

	public String caluculateAndFormatEDD(Date pStartDate, Date pEndDate, String pItemType) {
		log.debug("START :: caluculateAndFormatEDD()");
		if (pStartDate != null && pEndDate != null) {
			String dateInitial = getformatedDateforStage(pStartDate);
			String dateFinal = getformatedDateforStage(pEndDate);
			return dateInitial.concat(ComprasIngestConstants.HIPHEN) + dateFinal;
		} else if (pStartDate == null && pEndDate != null) {
			return getformatedDateforStage(pEndDate);
		}
		log.debug("END :: caluculateAndFormatEDD()");
		return null;
	}

	/**
	 * This method is returns the formatted date.
	 * 
	 * @return String
	 * @param pFormatDate Date
	 */

	public String getformatedDateforStage(Date pFormatDate) {
		log.debug("STARTS::getformatedDateforStage()");
		if (pFormatDate == null) {
			return null;
		}
		final Locale mex = new Locale(ComprasIngestConstants.ES, ComprasIngestConstants.COUNTRY_MX);
		SimpleDateFormat formateador = new SimpleDateFormat(ComprasIngestConstants.SAMPLE_DATE_FORMAT, mex);
		String estimatedDeliverydate = formateador.format(pFormatDate);
		log.debug("END::getformatedDateforStage()");
		return estimatedDeliverydate;

	}

	/**
	 * This method is returns the estimated delivery details for SL items.
	 * 
	 * @return - estimatedDeliverydate()
	 * @param pSiteId     - String
	 * @param pStartDaySL - int
	 * @param pEndDaySL   - int
	 */

	public String getEstimatedDeliveryDateForSL(int pStartDaySL, int pEndDaySL) {
		log.debug("STARTS::LiverpoolOrderManager.getEstimatedDeliveryDateForSL()");
		Calendar calInitial = Calendar.getInstance();
		Date timeInitial;
		Date timeFinal;
		String dateInitial = null;
		String dateFinal = null;
		String estimatedDeliverydate = null;
		/* END: Modified as part of defect#11407 */
		calInitial.add(Calendar.DATE, 1 + pStartDaySL);
		final Locale mex = new Locale(ComprasIngestConstants.ES, ComprasIngestConstants.COUNTRY_MX);
		SimpleDateFormat formateador = new SimpleDateFormat(ComprasIngestConstants.SAMPLE_DATE_FORMAT, mex);
		timeInitial = calInitial.getTime();
		dateInitial = formateador.format(timeInitial);

		Calendar calFinal = (Calendar) calInitial.clone();
		calFinal.add(Calendar.DATE, pEndDaySL);
		timeFinal = calFinal.getTime();
		dateFinal = formateador.format(timeFinal);
		log.debug("ENDS::getEstimatedDeliveryDateForSL()");
		log.debug("EstimatedDeliveryDate" + estimatedDeliverydate);
		estimatedDeliverydate = dateInitial.concat(ComprasIngestConstants.HIPHEN) + dateFinal;
		return estimatedDeliverydate;
	}

	public String getBarcode(Order order) {
		StringBuilder barcode = new StringBuilder();
		String storeNumber = null;
		String terminalNumber = null;
		String ballotNumber = null;
		Date orderSubmittedDate = order.getPurchaseDate();
		SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd");
		String submittedDate = formatter.format(orderSubmittedDate);

		String lpChargeNbr = order.getChargeNumber();
		if (!StringUtils.isBlank(lpChargeNbr)) {
			storeNumber = lpChargeNbr.substring(0, 3);
			terminalNumber = lpChargeNbr.substring(13);
			ballotNumber = lpChargeNbr.substring(9, 13);
			if (!StringUtils.isBlank(storeNumber)) {
				storeNumber = updateLengthForFields(storeNumber, 3);
			}
			if (!StringUtils.isBlank(terminalNumber)) {
				terminalNumber = updateLengthForFields(terminalNumber, 3);
			}
			if (!StringUtils.isBlank(ballotNumber)) {
				ballotNumber = updateLengthForFields(ballotNumber, 4);
			}
		}
		String vendorNumber = order.getCertificado();
		if (!StringUtils.isBlank(vendorNumber)) {
			vendorNumber = updateLengthForFields(vendorNumber, 8);
		}

		barcode = barcode.append(submittedDate).append(storeNumber).append(terminalNumber).append(ballotNumber)
				.append(vendorNumber);

		return barcode.toString();
	}

	/**
	 * This is the method is used to set the length based on the fixed value for all
	 * the required fields for barcode
	 * 
	 * @param fieldNum          string
	 * @param vendorConstantLen int
	 * @return string
	 */
	public String updateLengthForFields(String fieldNum, int vendorConstantLen) {
		StringBuilder str = new StringBuilder();
		int length = fieldNum.length();
		int finalVendorLength = vendorConstantLen - length;
		if (length < vendorConstantLen) {
			for (int i = 1; i <= finalVendorLength; i++) {
				str.append(0);
			}
			str.append(fieldNum);
			return str.toString();
		}
		return fieldNum;
	}

	public boolean isSinglePackageRemove(Item item) {
		log.debug("isSinglePackageRemove starts");
		boolean returnVal = Boolean.FALSE;
		if (item.isRemovedFromPackage()) {
			returnVal = Boolean.TRUE;
		}
		log.debug("isSinglePackageRemove ends with return value: {1}", returnVal);
		return returnVal;
	}

	/**
	 * 
	 * @param statusType
	 * @param status
	 * @param skuItem
	 * @param somsSku
	 * @param isMirakl
	 */
	@SuppressWarnings("unchecked")
	public void setBundleCommerceItemProperties(OrderStatusMapBean orderStatus, Item skuItem, Skus somsSku,
			boolean isMirakl) {
		log.debug("setBundleCommerceItemProperties :: " + somsSku.getSkuNo());

		Date date = new Date();
		Map<String, String> orderStatusMap = null;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		// Invoking the rest service to get the order stage level
		orderStatusMap = restInvoker.callServicePost(applicationConfiguration.getOrderStatusService(), orderStatus,
				HashMap.class);
		String stageLevel=null;
		if(null != orderStatusMap) {
			stageLevel = orderStatusMap.get(ComprasIngestConstants.STAGELEVEL);
			log.debug("stageLevel::" + stageLevel);
		}
		 

		String stage1date = skuItem.getStage1Date();
		String stage2date = skuItem.getStage2Date();
		String stage3date = skuItem.getStage3Date();
		String stage4date = skuItem.getStage4Date();

		final Locale mex = new Locale(ComprasIngestConstants.ES, ComprasIngestConstants.COUNTRY_MX);
		SimpleDateFormat formateador = new SimpleDateFormat(ComprasIngestConstants.SAMPLE_DATE_FORMAT, mex);
		String estimatedDeliverydate = formateador.format(date);

		if (isMirakl) {
			boolean removePackage = Boolean.parseBoolean(somsSku.getRemovePackage());
			if ((skuItem.getProductType().equalsIgnoreCase(ComprasIngestConstants.SOFT_LINE)
					|| skuItem.getProductType().equalsIgnoreCase(ComprasIngestConstants.BIG_TICKET))
					&& skuItem.isPackageApplied() && removePackage) {
				skuItem.setRemovedFromPackage(Boolean.parseBoolean(somsSku.getRemovePackage()));
				skuItem.setRemovedFromPackageDate(Calendar.getInstance().getTime());
			}
		}
		log.debug("Stage1date::" + stage1date + "Stage2date::" + stage2date + "Stage3date::" + stage3date
				+ "Stage4date::" + stage4date);

		String eddUpdated = caluculateAndFormatEDD(convertToDate(somsSku.getEddStartDate()),
				convertToDate(somsSku.getDeliveryDate()), skuItem.getProductType());

		log.debug("EDDupdated::" + eddUpdated);

		skuItem.setEstimatedDeliveryDate(eddUpdated);
		if (!StringUtils.isBlank(stageLevel)) {
			if (stageLevel.equalsIgnoreCase(ComprasIngestConstants.STAGE1DATE)) {
				if (stage1date == null) {
					skuItem.setStage1Date(estimatedDeliverydate);
				}
				if (skuItem.getProductType().equalsIgnoreCase(ComprasIngestConstants.SOFT_LINE) && (stage2date != null || stage3date != null || stage4date != null)) {
						log.debug("::::: Is SoftLine Product::::: ");

						String lreEstimatedDeliveryDate = getEstimatedDeliveryDateForSL(
								applicationConfiguration.getStartDayRangeSL(),
								applicationConfiguration.getEndDayRangeSL());
						log.debug("::: setCommerceItemProperties::: Re Calulated EDD ::: " + lreEstimatedDeliveryDate);

						skuItem.setEstimatedDeliveryDate(lreEstimatedDeliveryDate);
				}
				skuItem.setStage4Date(null);
				skuItem.setStage3Date(null);
				skuItem.setStage2Date(null);
			}
			if (stageLevel.equalsIgnoreCase(ComprasIngestConstants.STAGE2DATE)) {
				if (stage2date == null) {
					skuItem.setStage2Date(estimatedDeliverydate);
				}
				skuItem.setStage4Date(null);
				skuItem.setStage3Date(null);
			}
			if (stageLevel.equalsIgnoreCase(ComprasIngestConstants.STAGE3DATE)) {
				if (stage3date == null) {
					skuItem.setStage3Date(estimatedDeliverydate);
				}
				skuItem.setStage4Date(null);
			}
			if (stageLevel.equalsIgnoreCase(ComprasIngestConstants.STAGE4DATE) && stage4date == null) {
				skuItem.setStage4Date(estimatedDeliverydate);
			}

		} else {
			log.debug("Stage levels not configured for Status:" + somsSku.getStatus());
		}
		log.debug("END- setBundleCommerceItemProperties :: " + somsSku.getSkuNo());

	}

	public static Date addDaysToDate(Date date, int days) {
		log.info("Start of addDaysToDate method");
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, - days);
		log.info("End of addDaysToDate method ");
		return cal.getTime();
	}
}
