package com.liverpool.compras.ingest.dao.beans;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaypalPayment {

	private String folioPago;
	private String folioPaypal;
}
