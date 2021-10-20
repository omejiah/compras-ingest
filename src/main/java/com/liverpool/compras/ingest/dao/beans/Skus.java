package com.liverpool.compras.ingest.dao.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Skus {
	
	@JsonProperty("no")
	private String skuNo;
	@JsonProperty("de")
	private String description;
	@JsonProperty("ca")
	private String qty;
	@JsonProperty("fi")
	private String queuePosition;
	@JsonProperty("im")
	private String price;
	@JsonProperty("es")
	private String status;
	@JsonProperty("fe")
	private String deliveryDate;
	@JsonProperty("fc")
	private String createDate;
	@JsonProperty("ts")
	private String timestamp;
	@JsonProperty("fn")
	private String eddStartDate;
	@JsonProperty("tn")
	private String trackingNumber;
	@JsonProperty("tc")
	private String carrierType;
	@JsonProperty("fs2")
	private String eddStageTwoDate;
	@JsonProperty("fs3")
	private String eddStageThreeDate;
	@JsonProperty("sp")
	private String removePackage;
	@JsonProperty("so")
	private String somsSo;
	@JsonProperty("cf")
	private String cancellationFlag;
	@JsonProperty("nd")
	private String node;
	
}
