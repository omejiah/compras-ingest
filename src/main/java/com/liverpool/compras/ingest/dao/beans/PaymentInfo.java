package com.liverpool.compras.ingest.dao.beans;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentInfo {
	private Double authorizedAmount;
	private String paymentType;
	private CardPayment cardPayment;
	private OpenPayPayment openPayPayment;
	private Monedero digitalWallet;
	private PaypalPayment payPal;

}
