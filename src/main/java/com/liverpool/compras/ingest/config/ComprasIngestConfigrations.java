package com.liverpool.compras.ingest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
public class ComprasIngestConfigrations {

	@Value("${projectId}")
	String projectId;

	@Value("${createOnlineOrderSubId}")
	String createOnlineOrderSubId;

	@Value("${tloUpdatesSubId}")
	String tloUpdatesSubId;
	
	@Value("${updateOnlineOrderSubId}")
	String updateOnlineOrderSubId;

	@Value("${posUpdatesSubId}")
	String posUpdatesSubId;
	
	@Value("${ordersCollection}")
	String ordersCollection;
	
	@Value("${itemsCollection}")
	String itemsCollection;
}
