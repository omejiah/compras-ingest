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

@Configuration
@Component
@Slf4j
public class TloUpdateService {
	
	private ApplicationConfiguration applicationConfiguration;
	private RestInvoker restInvoker;
	
	public TloUpdateService(RestInvoker restInvoker, ApplicationConfiguration applicationConfiguration) {
		super();
		this.restInvoker = restInvoker;
		this.applicationConfiguration = applicationConfiguration;
	}
	
	@PostConstruct
	public void tloUpdated() {
		log.debug("getting the data from mongo for all properties");
		populateTloData();
	}
	
	public void populateTloData() {
		Map<String, String> headersMap = new HashMap<>();
		headersMap.put(ComprasIngestConstants.LP_AUTH_HEADER, applicationConfiguration.getLpAuthHeader());
		headersMap.put(ComprasIngestConstants.BRAND, applicationConfiguration.getBrand());
		headersMap.put(ComprasIngestConstants.LP_CORRELATION_ID, applicationConfiguration.getLpCorrelationId());
		headersMap.put(ComprasIngestConstants.CHANNEL, applicationConfiguration.getChannel());
		headersMap.put(ComprasIngestConstants.CONTENT_TYPE, applicationConfiguration.getContentType());

		String url = applicationConfiguration.getTloOrderstatusUrl();
		JsonNode jsonNodes = null;
		List<OrderStatusBean> orderStatusBeanList = new ArrayList();
		try {
			jsonNodes = restInvoker.callServiceGet(url, headersMap);
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

	}
}
