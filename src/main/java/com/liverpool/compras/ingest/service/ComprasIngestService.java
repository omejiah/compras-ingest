package com.liverpool.compras.ingest.service;

import com.liverpool.compras.ingest.beans.OrderUpdateBean;
import com.liverpool.compras.ingest.dao.beans.Order;
import com.liverpool.compras.ingest.dao.beans.SomsOrderResBean;

public interface ComprasIngestService {

	public void createOrder(Order orderIngestBean);
	
	public void createItem(Order orderIngestBean);
	
	public void updateOrder(OrderUpdateBean orderUpdateBean);
	
	public void createPosItem(Order orderIngestBean);
	
	public void updateTLOParams(SomsOrderResBean somsOrderResBean);
	
	public void addAuditEntry(String json, String messageSource);
}
