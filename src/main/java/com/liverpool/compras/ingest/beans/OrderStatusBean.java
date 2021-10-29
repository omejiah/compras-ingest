package com.liverpool.compras.ingest.beans;

import lombok.Data;

/**
 * @author DigiSprint
 *
 */
public @Data class OrderStatusBean {

	private String statusType;
	private String statusName;
	private String translatedStatusName;
	private String stageLevel;
	private String somsNode;

}
