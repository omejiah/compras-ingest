package com.liverpool.compras.ingest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.liverpool.compras.ingest.beans.OrderStatusBean;
import com.liverpool.compras.ingest.configuration.ApplicationConfiguration;
import com.liverpool.compras.ingest.constants.ComprasIngestConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is used to initialize and update orderStatusBeanList
 * @author shakti
 *
 */
@Configuration
@Component
@Slf4j
public class OrderStatusConfigurationService {
	
	private ApplicationConfiguration applicationConfiguration;
	private RestInvoker restInvoker;
	
	public OrderStatusConfigurationService(RestInvoker restInvoker, ApplicationConfiguration applicationConfiguration) {
		super();
		this.restInvoker = restInvoker;
		this.applicationConfiguration = applicationConfiguration;
	}
	/**
	 * This method will be called at the time of
	 * server startup and store Order Status
	 * data in a global variable.
	 */
	@PostConstruct
	public void tloUpdated() {
		log.debug("Starting:: tloUpdated method");
		log.debug("getting the Order Status data");
		populateTloData();
		log.debug("Ending:: tloUpdated method");
	}
	
	/**
	 * This method will call orderStatusConfigServiceEndpoint and
	 * store data in a global variable.
	 */
	public void populateTloData() {
		log.debug("Starting:: populateTloData method");
		String url = applicationConfiguration.getOrderStatusConfigServiceEndpoint();
		JsonNode jsonNodes = null;
		List<OrderStatusBean> orderStatusBeanList = new ArrayList<>();
		try {
			jsonNodes = restInvoker.callServiceGet(url, getRequestHeader());
			ArrayNode orderStatusList = (ArrayNode) jsonNodes.get("orderStatusList");

			if (null != orderStatusList) {
				Iterator<JsonNode> it = orderStatusList.iterator();
				while (it.hasNext()) {
					String jsonNode = it.next().toString();
					ObjectMapper objectMapper = new ObjectMapper();
					OrderStatusBean orderStatusBean = objectMapper.readValue(jsonNode, OrderStatusBean.class);
					orderStatusBeanList.add(orderStatusBean);
				}
				applicationConfiguration.setOrderStatusBeanList(orderStatusBeanList);
			}

		} catch (JsonProcessingException e) {
			log.error("Exception while calling get method: {}", e.getMessage());
		}catch (Exception e) {
			log.error("Exception while populateTloData: {}", e.getMessage());
		}
		log.debug("Ending:: populateTloData method");

	}
	
	/**
	 * prepare header for service call
	 * @return headersMap
	 */
	public Map<String,String> getRequestHeader(){
		Map<String, String> headersMap = new HashMap<>();
		headersMap.put(ComprasIngestConstants.LP_AUTH_HEADER, applicationConfiguration.getLpAuthHeader());
		headersMap.put(ComprasIngestConstants.BRAND, applicationConfiguration.getBrand());
		headersMap.put(ComprasIngestConstants.LP_CORRELATION_ID, applicationConfiguration.getLpCorrelationId());
		headersMap.put(ComprasIngestConstants.CHANNEL, applicationConfiguration.getChannel());
		headersMap.put(ComprasIngestConstants.CONTENT_TYPE, applicationConfiguration.getContentType());
		return headersMap;
	}
}
