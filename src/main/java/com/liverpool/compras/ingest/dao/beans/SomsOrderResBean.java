package com.liverpool.compras.ingest.dao.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SomsOrderResBean {
	
	@JsonProperty("or")
	private String id;
	@JsonProperty("em")
	private String storeBrand;
	@JsonProperty("orem")
	private String orderCode;
	@JsonProperty("no")
	private String name;
	@JsonProperty("t2")
	private String phone2;
	@JsonProperty("calle")
	private String calle;
	@JsonProperty("numExt")
	private String numExt;
	@JsonProperty("numInt")
	private String numInt;
	@JsonProperty("edi")
	private String edi;
	@JsonProperty("col")
	private String col;
	@JsonProperty("es")
	private String state;
	@JsonProperty("fe")
	private String createDate;
	@JsonProperty("eb")
	private String status;
	@JsonProperty("nv")
	private String salesRepNo;
	@JsonProperty("nb")
	private String ticket;
	@JsonProperty("al")
	private String store;
	@JsonProperty("de")
	private String storeDescription;
	@JsonProperty("cam")
	private String truckNo;
	@JsonProperty("rt")
	private String routeNo;
	@JsonProperty("nbu")
	private String unitCount;
	@JsonProperty("im")
	private String price;
	@JsonProperty("dv")
	private String returnsAmt;
	
	@JsonProperty("all")
	private String canal;
	@JsonProperty("dd")
	private String customerId;
	@JsonProperty("del")
	private String storeNumber;
	@JsonProperty("t1")
	private String phone;
	@JsonProperty("cp")
	private String codigoPostal;
	@JsonProperty("Mun")
	private String muncipioDelegacion;
	@JsonProperty("entreCalle")
	private String entreLasCalles;
	@JsonProperty("ciudad")
	private String ciudad;
	@JsonProperty("yCalle")
	private String yCalle;
	@JsonProperty("pais")
	private String pais;
	@JsonProperty("Skus")
	private Skus sku;
	
}
