package com.liverpool.compras.ingest.dao.beans;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SkuDetails {
	private String isComboItem;
	private String eddMessage;
	private String smallImage;
	private String brandId;
	private String materialGroup;
	private String departmentId;
	private String comfortService;
	private String comfortServiceCost;
	private String comfortServiceAddress;
	private String warrantyService;
	private String warrantyServiceCost;
	private String warrantyServiceAddress;
	private String warrantyPeriod;
	private String serviceCombo;
	private String supplierId;
	private String size;
	private String material;
	private String color;
}