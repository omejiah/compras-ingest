package com.liverpool.compras.ingest.beans;

import java.util.List;

import lombok.Data;

/**
 * @author DigiSprint
 *
 */
public @Data class OrderUpdateBean {

	private String orderRef;
	private String status;
	private String openPayReference;
	private String sourceShipId;
	private boolean isCancelExpiray;
	private boolean isUpdated;
	private boolean isInvoiceGenerated;
	private boolean ratingsGiven;
	private List<CancelItemInfo> cancelledItemInfos;
}
