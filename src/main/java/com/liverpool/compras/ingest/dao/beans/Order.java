package com.liverpool.compras.ingest.dao.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Document(collection = "orders")
public @Data class Order {
	
	@Id
	@Indexed
	private String orderRef;
	private String canal;
	private String customerId;
	private String sourceOrderId;
	private String sourceShipId;
	private String email;
	private String firstname;
	private String lastname;
	private String middleName;
	private String maternalName;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
	private Date purchaseDate;
	private String status;
	private Double total;
	private String boleta;
	private String terminal;
	private String tienda;
	private String storeName;
	private String chargeNumber;
	private String certificado;
	private String facturacion;
	private boolean marketPlace;
	private boolean giftRegistry;
	private String eventNumber;
	private String celebrantName;
	private boolean ratingsGiven;
	private String eventName;
	private boolean personalpurchase;
	private boolean cnc;
	private String cncStoreLocationId;
	private boolean hasTracking;
	private boolean invoiceGenerated;
	private ShippingAddress address;
	@Transient
	private List<Item> items = new ArrayList<>();
	private List<PaymentInfo> paymentInfo;
	
	private String deliveryStatusCode;
	private String currentStatus;
	private String stage1Date;
	private String stage2Date;
	private String stage3Date;
	private String stage4Date;
	
	private List<String> itemIds;
	private boolean storePickup;
	private String shippingstate;
	private String packageNbr;
	private String trackingNumber;
	private String shoppingType;
	private String itemType;
	
	private boolean cancelledItemsAvailable;
}
