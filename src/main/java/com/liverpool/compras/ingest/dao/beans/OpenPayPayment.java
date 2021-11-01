package com.liverpool.compras.ingest.dao.beans;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpenPayPayment {

	private String estatus;
	private String openPayReference;
	private Date openPayExpiryDateAndTime;
	private String paymentMethod;
	private String authNumber;
	
}
