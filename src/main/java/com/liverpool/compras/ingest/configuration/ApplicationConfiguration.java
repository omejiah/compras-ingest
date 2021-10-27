package com.liverpool.compras.ingest.configuration;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.liverpool.compras.ingest.beans.OrderStatusBean;

import lombok.Getter;
import lombok.Setter;

@Configuration
@Getter
@Setter
public class ApplicationConfiguration {

	@Value("${apigee.baseURL:'http://dummyhost:dummyport'}")
	private String restBaseURL;
	
	@Value("${skuDetailApikey}")
	private String skuDetailApikey;
	
	@Value("${skuDetailApiUrl}")
	private String skuDetailApiUrl;
	
	@Value("#{${statusTypeMap}}")
	private Map<String, String> statusTypeMap;

	@Value("#{${lineItemStatusCodesMapForSoms}}")
	private Map<String, String> lineItemStatusCodesMapForSoms;

	@Value("#{${shippingStatusCodesMapForSoms}}")
	private Map<String, String> shippingStatusCodesMapForSoms;

	@Value("${startDayRangeSL}")
	private int startDayRangeSL;

	@Value("${endDayRangeSL}")
	private int endDayRangeSL;

	@Value("${orderStatusService}")
	private String orderStatusService;
	
	@Value("${purgingDays}")
	private int purgingDays;
	
	@Value("${tloOrderstatusUrl}")
	private String tloOrderstatusUrl;
	
	@Value("${brand}")
	private String brand;
	
	@Value("${channel}")
	private String channel;
	
	@Value("${lp_correlation_id}")
	private String lpCorrelationId;
	
	@Value("${lp_auth_header}")
	private String lpAuthHeader;
	
	@Value("${content_Type}")
	private String contentType;
	
	private List<OrderStatusBean> orderStatusBeanList;
	

	@Value("#{'${validShippingStatusList}'.split(',')}")
	private List<String> shippingGroupStates;
}