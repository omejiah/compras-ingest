package com.liverpool.compras.ingest.configuration;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

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
}