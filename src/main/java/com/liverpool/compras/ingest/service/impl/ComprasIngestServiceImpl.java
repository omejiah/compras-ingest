package com.liverpool.compras.ingest.service.impl;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.liverpool.compras.ingest.beans.CancelItemInfo;
import com.liverpool.compras.ingest.beans.OrderUpdateBean;
import com.liverpool.compras.ingest.config.ComprasIngestConfigrations;
import com.liverpool.compras.ingest.configuration.ApplicationConfiguration;
import com.liverpool.compras.ingest.constants.ComprasIngestConstants;
import com.liverpool.compras.ingest.dao.beans.AuditInfo;
import com.liverpool.compras.ingest.dao.beans.Item;
import com.liverpool.compras.ingest.dao.beans.Order;
import com.liverpool.compras.ingest.dao.beans.OrderStatusMapBean;
import com.liverpool.compras.ingest.dao.beans.ShippingAddress;
import com.liverpool.compras.ingest.dao.beans.SkuDetails;
import com.liverpool.compras.ingest.dao.beans.Skus;
import com.liverpool.compras.ingest.dao.beans.SomsOrderResBean;
import com.liverpool.compras.ingest.repository.ComprasIngestAuditRepository;
import com.liverpool.compras.ingest.repository.ComprasIngestItemRepository;
import com.liverpool.compras.ingest.service.ComprasIngestService;
import com.liverpool.compras.ingest.utils.ComprasIngestUtils;
import com.mongodb.MongoWriteException;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ComprasIngestServiceImpl implements ComprasIngestService {

	private MongoTemplate mongoTemplate;
	private ComprasIngestUtils ingestUtils;
	private ComprasIngestConfigrations ingestConfigrations;
	private ApplicationConfiguration applicationConfiguration;
	private ComprasIngestItemRepository comprasIngestItemRepository;
	private ComprasIngestAuditRepository comprasIngestAuditRepository;

	public ComprasIngestServiceImpl(MongoTemplate mongoTemplate, ComprasIngestUtils ingestUtils,
			ComprasIngestConfigrations ingestConfigrations, ApplicationConfiguration applicationConfiguration,
			ComprasIngestItemRepository comprasIngestItemRepository,
			ComprasIngestAuditRepository comprasIngestAuditRepository) {
		super();
		this.mongoTemplate = mongoTemplate;
		this.ingestUtils = ingestUtils;
		this.ingestConfigrations = ingestConfigrations;
		this.applicationConfiguration = applicationConfiguration;
		this.comprasIngestItemRepository = comprasIngestItemRepository;
		this.comprasIngestAuditRepository = comprasIngestAuditRepository;
	}

	@Override
	public void createOrder(Order orderIngestBean) {
		log.info("Start :: ComprasIngestServiceImpl.createOrder()");
		try {
			Order order = ingestUtils.updateOrderBean(orderIngestBean);
			mongoTemplate.save(order);
		} catch (MongoWriteException me) {
			log.error("Error while inserting the Order from ATG preview instance :: ", me.getMessage());
		}
		log.info("End :: ComprasIngestServiceImpl.createOrder()");
	}

	@Override
	public void createItem(Order orderIngestBean) {
		log.info("Start :: ComprasIngestServiceImpl.createItem()");
		try {
			List<Item> items = orderIngestBean.getItems();
			for (Item item : items) {
				Item itemObject = ingestUtils.updateItemBean(item, orderIngestBean);
				mongoTemplate.save(itemObject);
			}
		} catch (MongoWriteException me) {
			log.error("Error while inserting the Items from ATG preview instance :: ", me.getMessage());
		}
		log.info("End :: ComprasIngestServiceImpl.createItem()");
	}
	@Override
	public void updateOrder(OrderUpdateBean orderUpdateBean) {
		log.info("Start :: ComprasIngestServiceImpl.updateOrder()");
		if (orderUpdateBean.isCancelExpiray() && orderUpdateBean.getSourceShipId() != null) {
			log.info("Updating the Open pay order expiray status :: {}", orderUpdateBean.getSourceShipId());
			updateCollectionProperty(ComprasIngestConstants.PROP_SOURCESHIPID, orderUpdateBean.getSourceShipId(),
					ComprasIngestConstants.PROP_STATUS, ComprasIngestConstants.STATUS_VENCIDO,
					ingestConfigrations.getOrdersCollection());

		}
		if (orderUpdateBean.isUpdated()) {
			if (orderUpdateBean.isInvoiceGenerated()) {
				updateCollectionProperty(ComprasIngestConstants.PROP_ID, orderUpdateBean.getOrderRef(),
						ComprasIngestConstants.PROP_INVOICEGENERATED, Boolean.TRUE,
						ingestConfigrations.getOrdersCollection());
			}
			if (orderUpdateBean.isRatingsGiven()) {
				updateCollectionProperty(ComprasIngestConstants.PROP_ID, orderUpdateBean.getOrderRef(),
						ComprasIngestConstants.PROP_RATINGSGIVEN, Boolean.TRUE,
						ingestConfigrations.getOrdersCollection());
			}
		}
		if (orderUpdateBean.getCancelledItemInfos() != null && !orderUpdateBean.getCancelledItemInfos().isEmpty()) {
			List<CancelItemInfo> cancelledItemInfos = orderUpdateBean.getCancelledItemInfos();
			cancelledItemInfos.forEach(cancelledItemInfo -> {
				Update updateItem = new Update();
				updateItem.set(ComprasIngestConstants.PROP_CANCELLATIONDATE, cancelledItemInfo.getCancelledDate());
				updateItem.set(ComprasIngestConstants.PROP_DELIVERYSTATUSCODE, ComprasIngestConstants.PROP_CANC);
				mongoTemplate.updateFirst(
						query(where(ComprasIngestConstants.PROP_SKU_ID).is(cancelledItemInfo.getCancelledSkuId())),
						updateItem, ingestConfigrations.getItemsCollection());
				mongoTemplate.updateFirst(
						query(where(ComprasIngestConstants.PROP_SOURCEITEMID).is(cancelledItemInfo.getCommerceItemId())),
						updateItem, ingestConfigrations.getItemsCollection());
				Update updateOrder = new Update();
				updateOrder.set(ComprasIngestConstants.PROP_CANCELLEDITEMS_AVAILABLE, Boolean.TRUE);
				mongoTemplate.updateFirst(
						query(where(ComprasIngestConstants.PROP_SOURCESHIPID).is(cancelledItemInfo.getShippingGroupId())),
						updateOrder, ingestConfigrations.getOrdersCollection());
			});

		}
		log.info("End :: ComprasIngestServiceImpl.updateOrder()");
	}

	private void updateCollectionProperty(String inputPropertyName, Object inputPropertyValue, String updateProperty,
			Object updateValue, String colletionName) {
		mongoTemplate.updateFirst(query(where(inputPropertyName).is(inputPropertyValue)),
				Update.update(updateProperty, updateValue), colletionName);

	}

	@Override
	public void createPosItem(Order orderIngestBean) {

		log.info("Start :: ComprasIngestServiceImpl.createPosItem()");
		try {
			List<Item> items = orderIngestBean.getItems();
			for (Item item : items) {
				Item itemObject = ingestUtils.updatePosItemBean(item, orderIngestBean);
				Map<String, String> headersMap = new HashMap<>();
				headersMap.put(ComprasIngestConstants.APIKEY, applicationConfiguration.getSkuDetailApikey());
				SkuDetails skuDetails = ingestUtils.getSkuDetails(headersMap,itemObject.getSkuId());
				if (skuDetails != null) {
					Map<String, String> variantMap = new HashMap<>();
					variantMap.put(ComprasIngestConstants.COLOR, skuDetails.getColor());
					variantMap.put(ComprasIngestConstants.MATERIAL, skuDetails.getMaterial());
					variantMap.put(ComprasIngestConstants.SIZE, skuDetails.getSize());
					itemObject.setVariantInfo(variantMap);
					itemObject.setComfortService(skuDetails.getComfortService());
					itemObject.setComfortServiceCost(Boolean.getBoolean(skuDetails.getComfortServiceCost()));
					itemObject.setComfortServiceAddress(skuDetails.getComfortServiceAddress());
					itemObject.setWarrantyService(Boolean.getBoolean(skuDetails.getWarrantyService()));
					itemObject.setWarrantyServiceCost(skuDetails.getWarrantyServiceCost());
					itemObject.setWarrantyServiceAddress(skuDetails.getWarrantyServiceAddress());
					itemObject.setWarrantyPeriod(Integer.parseInt((skuDetails.getWarrantyPeriod())));
					itemObject.setServiceCombo(Boolean.getBoolean(skuDetails.getServiceCombo()));
					itemObject.setBrandId(skuDetails.getBrandId());
					itemObject.setSmallImage(skuDetails.getSmallImage());
					itemObject.setMeterialGroup(skuDetails.getMaterialGroup());
					itemObject.setDepartmentId(skuDetails.getDepartmentId());
					itemObject.setSupplierId(skuDetails.getSupplierId());
				}
				mongoTemplate.save(itemObject);
			}
		} catch (MongoWriteException me) {
			log.error("Error while inserting the Items from POS system :: ", me.getMessage());
		}
		log.info("End :: ComprasIngestServiceImpl.createPosItem()");

	}

	@Override
	public void updateTLOParams(SomsOrderResBean somsOrderResBean) {
		log.info("Start :: ComprasIngestServiceImpl.updateTLOParams()");
		Skus somsSku = somsOrderResBean.getSku();
		
		Item item = comprasIngestItemRepository.getItem(somsSku.getTrackingNumber(), somsSku.getSkuNo());
		Query orderItemQuery = new Query(Criteria.where(ComprasIngestConstants.ID).is(somsOrderResBean.getId()));
		List<Order> orderItem = mongoTemplate.find(orderItemQuery, Order.class);
		final String status = somsOrderResBean.getStatus();
		Update orderItemToUpdate = new Update().set(orderItem.toString(), orderItemQuery);
		orderItemToUpdate.set(ComprasIngestConstants.SHIPPING_STATE, somsSku.getStatus());
		log.debug("Updating with Shipping status code: " + status);
		if(!orderItem.isEmpty()) {
			changeCommerceItemState(orderItem.get(0), somsOrderResBean, item);
		}
		mongoTemplate.updateFirst(orderItemQuery, orderItemToUpdate, Order.class);
		log.info("End :: ComprasIngestServiceImpl.updateTLOParams()");
	}
	/**
	 * 
	 * @param orderItem
	 * @param somsOrderResBean
	 * @param skuItem
	 */
	public void changeCommerceItemState(Order orderItem, SomsOrderResBean somsOrderResBean, Item skuItem) {
		log.debug("changeCommerceItemState Method: starts");

		String shoppingType = orderItem.getShoppingType();
		boolean isStorePickUp = (boolean) orderItem.isStorePickup();
		String statusType = null;
		Date storedTimestamp = null;
		Date timestampInSomsResponse = null;
		StringBuilder finalCatalogRefId = null;
		String miraklSkuId = null;
		int zero = 0;
		Skus somsSku = somsOrderResBean.getSku();
		SimpleDateFormat formatter = new SimpleDateFormat(ComprasIngestConstants.DATE_FORMAT);
		Item item = comprasIngestItemRepository.getItem(somsSku.getTrackingNumber(), somsSku.getSkuNo());
		if (isStorePickUp) {
			statusType = applicationConfiguration.getStatusTypeMap().get(ComprasIngestConstants.STORE_PICKUP);
		} else if (shoppingType.equalsIgnoreCase(ComprasIngestConstants.PERSONAL_SHOPPING)) {
			statusType = applicationConfiguration.getStatusTypeMap().get(ComprasIngestConstants.PERSONAL_SHOPPING);
		} else if (shoppingType.equalsIgnoreCase(ComprasIngestConstants.GIFT_REGISTRY)) {
			statusType = applicationConfiguration.getStatusTypeMap().get(ComprasIngestConstants.GIFT_REGISTRY);
		}
		if (skuItem != null) {
			if(!StringUtils.isBlank(skuItem.getSellerSkuId())) {
				finalCatalogRefId = new StringBuilder(skuItem.getSellerSkuId());
				int noOfZeroes = 10 - finalCatalogRefId.length();
				for (int i = 0; i < noOfZeroes; i++) {
					finalCatalogRefId.insert(zero, ComprasIngestConstants.ZERO);
					log.debug("final catalog ref id :: " + finalCatalogRefId.toString());
					miraklSkuId = finalCatalogRefId.toString();
				}
			}
			try {
				if (!StringUtils.isBlank(somsSku.getTimestamp())) {
					timestampInSomsResponse = formatter.parse(somsSku.getTimestamp());
				}
			} catch (ParseException e) {
				log.error("ParseException in LiverpoolOrderTrackService.changeCommerceItemState()", e);
			}
			log.debug("processing SOMS sku no :: " + somsSku.getSkuNo());

			if (!somsSku.getCancellationFlag().isEmpty()) {
				item.setCancellationFlag(somsSku.getCancellationFlag());
			}
			if (!somsSku.getNode().isEmpty()) {
				item.setSomsNode(somsSku.getNode());
			}
			storedTimestamp = skuItem.getTimestamp();
			OrderStatusMapBean orderStatus = new OrderStatusMapBean();

			orderStatus.setSomsNode(somsSku.getNode());
			orderStatus.setStatusName(somsSku.getStatus());
			orderStatus.setStatusType(statusType);
			if (ingestUtils.checkValidDates(storedTimestamp, timestampInSomsResponse)) {
				
				item.setTimestamp(timestampInSomsResponse);
				item.setTrackingNumber(somsSku.getTrackingNumber());
				item.setShippingCarrierType(somsSku.getCarrierType());
				ingestUtils.setBundleCommerceItemProperties(orderStatus, skuItem,somsSku,false);
				item.setState(somsSku.getStatus());
				log.debug("OrderTrackService. Bundle commerceItem update status code:" + somsSku.getSkuNo());

			} else {
				log.debug("Skip update for commerceItem {0} because time on new message timestamp is older than the existing on the db:"
								+ " actual timestamp for commerceItem {1}, timestamp from Soms {2}",
						skuItem.getId(), storedTimestamp, timestampInSomsResponse);
			}

			if (!StringUtils.isBlank(miraklSkuId) && somsSku.getSkuNo().equalsIgnoreCase(miraklSkuId)) {

				storedTimestamp = skuItem.getTimestamp();
				if (ingestUtils.checkValidDates(storedTimestamp, timestampInSomsResponse)) {
					item.setTimestamp(timestampInSomsResponse);
					item.setTrackingNumber(somsSku.getTrackingNumber());
					item.setShippingCarrierType(somsSku.getCarrierType());
					ingestUtils.setBundleCommerceItemProperties(orderStatus, skuItem,somsSku,true);
					item.setState(somsSku.getStatus());
					log.debug("OrderTrackService. Bundle commerceItem update status code:" + somsSku.getSkuNo());

				} else {
					log.debug("Skip update for commerceItem {0} because time on new message timestamp is older than the existing on the db:"
									+ " actual timestamp for commerceItem {1}, timestamp from Soms {2}",
							skuItem.getId(), storedTimestamp, timestampInSomsResponse);
				}
			}
		}
		updateAddressFromSoms(orderItem,somsOrderResBean);
		mongoTemplate.save(item);
		log.debug("changeCommerceItemState Method: END");
	}

	/**
	 * This is the method is used to set the address from soms response 
	 * to the existing order.
	 * 
	 * @param orderItem
	 * @param somsOrderResBean
	 * @return 
	 */
	public void updateAddressFromSoms(Order orderItem, SomsOrderResBean somsOrderResBean) {
		log.debug("updateAddressFromSoms starts");
		
		ShippingAddress address = orderItem.getAddress();
		if(address != null) {
			address.setYCalle(somsOrderResBean.getYCalle());
			address.setCiudad(somsOrderResBean.getCiudad());
			address.setPais(somsOrderResBean.getPais());
			address.setMuncipioDelegacion(somsOrderResBean.getMuncipioDelegacion());
			address.setCodigoPostal(somsOrderResBean.getCodigoPostal());
			address.setPhone(somsOrderResBean.getPhone());
		}
		orderItem.setAddress(address);
		mongoTemplate.save(orderItem);
		log.debug("updateAddressFromSoms End");
	}
	/**
	 * This method is used to audit the messages into audit collection
	 */
	@Override
	public void addAuditEntry(String json, String messageSource) {
		log.info("Start:: ComprasIngestServiceImpl.addAuditEntry");
		AuditInfo auditInfo = new AuditInfo();
		auditInfo.setMessage(json);
		auditInfo.setMessageReceivedDate(new Date());
		auditInfo.setMessageSource(messageSource);
		comprasIngestAuditRepository.save(auditInfo);
		log.info("End:: ComprasIngestServiceImpl.addAuditEntry");
	}
}
