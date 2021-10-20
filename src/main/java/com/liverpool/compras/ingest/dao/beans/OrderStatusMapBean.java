package com.liverpool.compras.ingest.dao.beans;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OrderStatusMapBean {

	private String statusType;
	private String statusName;
	private String somsNode;
}
