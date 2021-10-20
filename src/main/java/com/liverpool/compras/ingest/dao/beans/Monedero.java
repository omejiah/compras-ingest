package com.liverpool.compras.ingest.dao.beans;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Monedero {

	private String dwAccountNumber;
	private Double promotionAmount;
}
