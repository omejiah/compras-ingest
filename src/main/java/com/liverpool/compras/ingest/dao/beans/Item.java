package com.liverpool.compras.ingest.dao.beans;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Document(collection = "items")
public class Item {
	@Indexed
	@Id
	private String id;
	private String sourceItemId;
	private String skuId;
	private String sellerSkuId;
	private Integer quantity;
	private String productId;
	private String productType;
	private String displayName;
	private String estimatedDeliveryDate;
	private Double itemTotalListPrice;
	private Double itemTotal;
	private String promotion;
	private String isSkuChanged;
	private boolean isComboItem;
	private String eddMessage;
	private String smallImage;
	private boolean isBundle;
	private boolean propina;
	private String commandNumber;
	private String somsCancelStatus;
	private String isExtendedWarranty;
	private String parcelNumber;
	private String deliveryPartner;
	private String dfProductKey;
	private String dfURL;
	private String dfInstructions;
	private String brandId;
	private String meterialGroup;
	private String departmentId;
	private String comfortService;
	private boolean comfortServiceCost;
	private String comfortServiceAddress;
	private boolean warrantyService;
	private String warrantyServiceCost;
	private String warrantyServiceAddress;
	private Integer warrantyPeriod;
	private boolean serviceCombo;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
	private Date cancellationDate;
	private String supplierId;
	private boolean giftWithPurchase;
	private String sellerName;
	private boolean sellerEnrolledInServices;
	private String status;
	private Map<String, String> variantInfo;
	private String remissionId;
	private String deliveryStatusCode;
	private String currentStatus;
	private String stage1Date;
	private String stage2Date;
	private String stage3Date;
	private String stage4Date;
	private String somsNode;
	private String stageLevel;
	private String commercestate;
	private String shippingstate;
	private String state;
	private String shippingCarrierType;
	private String trackingNumber;
	private Date timestamp;
	private String itemType;
	private boolean storePickup;
	private boolean isPackageApplied;
	private boolean isRemovedFromPackage;
	private Date removedFromPackageDate;
	private String lpBarCodeNbr;
	private String cancellationFlag;
	private String customerId;
	private List<String> associatedGiftItem;
	private boolean hasMultipleOfferListing;
}
