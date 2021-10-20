package com.liverpool.compras.ingest.beans;

import java.util.Date;

import lombok.Data;

/**
 * @author DigiSprint
 *
 */
public @Data class CancelItemInfo {

	private String cancelledSkuId;
	private Date cancelledDate;
	private boolean isCancelled;
	private String commerceItemId;
	private String shippingGroupId;
}
