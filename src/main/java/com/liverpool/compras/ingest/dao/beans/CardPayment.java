package com.liverpool.compras.ingest.dao.beans;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author DigiSprint
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardPayment {
	private String cardType;
	private String cardSuffix;
}
