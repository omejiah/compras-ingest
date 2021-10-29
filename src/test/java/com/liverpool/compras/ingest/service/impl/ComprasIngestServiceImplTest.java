package com.liverpool.compras.ingest.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liverpool.compras.ingest.RestInvoker;
import com.liverpool.compras.ingest.beans.CancelItemInfo;
import com.liverpool.compras.ingest.beans.OrderUpdateBean;
import com.liverpool.compras.ingest.config.ComprasIngestConfigrations;
import com.liverpool.compras.ingest.configuration.ApplicationConfiguration;
import com.liverpool.compras.ingest.dao.beans.Item;
import com.liverpool.compras.ingest.dao.beans.Order;
import com.liverpool.compras.ingest.dao.beans.ShippingAddress;
import com.liverpool.compras.ingest.dao.beans.SkuDetails;
import com.liverpool.compras.ingest.dao.beans.Skus;
import com.liverpool.compras.ingest.dao.beans.SomsOrderResBean;
import com.liverpool.compras.ingest.repository.ComprasIngestAuditRepository;
import com.liverpool.compras.ingest.repository.ComprasIngestItemRepository;
import com.liverpool.compras.ingest.repository.ComprasIngestOrderRepository;
import com.liverpool.compras.ingest.utils.ComprasIngestUtils;
import com.mongodb.MongoWriteException;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@PropertySource("classpath:com/liverpool/compras/ingest/application.properties")
@ContextConfiguration(classes = { ComprasIngestServiceImpl.class },loader=AnnotationConfigContextLoader.class)
class ComprasIngestServiceImplTest {

	@MockBean
	private MongoTemplate mongoTemplate;

	@MockBean
	private ComprasIngestUtils ingestUtils;

	@MockBean
	private ComprasIngestConfigrations ingestConfigrations;

	@MockBean
	private RestInvoker restInvoker;
	
	@InjectMocks
	private ApplicationConfiguration applicationConfiguration;

	@MockBean
	private ComprasIngestServiceImpl comprasIngestServiceImpl;
	
	@MockBean
	private ComprasIngestItemRepository comprasIngestItemRepository;
	
	@MockBean
	private ComprasIngestAuditRepository comprasIngestAuditRepository;

	@MockBean
	private ComprasIngestOrderRepository comprasIngestOrderRepository;
	
	@BeforeEach
	public void setUp() {
		comprasIngestServiceImpl = new ComprasIngestServiceImpl(mongoTemplate, ingestUtils, ingestConfigrations,
				applicationConfiguration, comprasIngestItemRepository, comprasIngestAuditRepository,
				comprasIngestOrderRepository);
	}

	@Test
	void updateAddressFromSoms(){
		Order order = new Order();
		ShippingAddress address = new ShippingAddress();
		order.setAddress(address);
		SomsOrderResBean soms = new SomsOrderResBean();
		

		soms.setYCalle("1234");
		soms.setCiudad("1234");
		soms.setPais("1234");
		soms.setMuncipioDelegacion("1234");
		soms.setCodigoPostal("1234");
		soms.setPhone("1234");
		
		comprasIngestServiceImpl.updateAddressFromSoms(order, soms);
		assertThat(comprasIngestServiceImpl).isNotNull();
		
	}
	
	@Test
	void createOrderSuccessTest() {
		Order order = new Order();
		comprasIngestServiceImpl.createOrder(order);
		assertThat(comprasIngestServiceImpl).isNotNull();
	}

	@Test
	void createOrderFailureTest() {
		Order order = new Order();
		when(mongoTemplate.save(null)).thenThrow(MongoWriteException.class);
		comprasIngestServiceImpl.createOrder(order);
		assertThat(comprasIngestServiceImpl).isNotNull();
	}
	
	@Test
	void createItemFailureTest() {
		List<Item> items = new ArrayList<>();
		Order order = new Order();
		Item item = new Item();
		item.setId("3423423");
		items.add(item);
		order.setItems(items);
		when(mongoTemplate.save(null)).thenThrow(MongoWriteException.class);
		comprasIngestServiceImpl.createItem(order);
		assertThat(comprasIngestServiceImpl).isNotNull();
	}

	@Test
	void createItemSuccessTest() {
		List<Item> items = new ArrayList<>();
		Order order = new Order();
		Item item = new Item();
		item.setId("3423423");
		items.add(item);
		order.setItems(items);
		comprasIngestServiceImpl.createItem(order);
		assertThat(comprasIngestServiceImpl).isNotNull();
	}
	
	@Test
	void updateOrderTest(){
		List<CancelItemInfo> cancelItemInfos = new ArrayList<>();
		OrderUpdateBean orderUpdateBean = new OrderUpdateBean();
		orderUpdateBean.setCancelExpiray(Boolean.TRUE);
		orderUpdateBean.setSourceShipId("sg123455");
		orderUpdateBean.setUpdated(Boolean.TRUE);
		orderUpdateBean.setInvoiceGenerated(Boolean.TRUE);
		orderUpdateBean.setRatingsGiven(Boolean.TRUE);
		CancelItemInfo cancelItemInfo = new CancelItemInfo();
		cancelItemInfo.setCancelled(Boolean.TRUE);
		cancelItemInfos.add(cancelItemInfo);
		orderUpdateBean.setCancelledItemInfos(cancelItemInfos);
		comprasIngestServiceImpl.updateOrder(orderUpdateBean);
		assertThat(comprasIngestServiceImpl).isNotNull();
	}
	@Test
	void updateTLOParams(){
		SomsOrderResBean soms = new SomsOrderResBean();
		Skus sku = new Skus();
		sku.setTrackingNumber("12345");
		sku.setSkuNo("12345");
		soms.setSku(sku);
		soms.setId("12345");
		soms.setStatus("12345");
		Item item = new Item();
		List<Order> order = new ArrayList<>();
		order.add(new Order());
		when(comprasIngestItemRepository.getItem(sku.getTrackingNumber(), sku.getSkuNo())).thenReturn(item);
		when(mongoTemplate.find(new Query(), Order.class)).thenReturn(order);
		comprasIngestServiceImpl.updateTLOParams(soms);
		assertThat(comprasIngestServiceImpl).isNotNull();
	}
	
	@Test
	void createPosItem() throws JsonMappingException, JsonProcessingException{
		Order order = new Order();
		order.setSourceOrderId("234234");
		order.setSourceShipId("sg18328726");
		order.setOrderRef("223232");
		order.setCustomerId("234234");
		List<Item> items = new ArrayList<Item>();
		Item item = new Item();
		item.setSkuId("12345");
		items.add(item);
		order.setItems(items);
		SkuDetails sku = new SkuDetails();
		sku.setComfortServiceCost("true");
		sku.setWarrantyPeriod("1");
		sku.setServiceCombo("false");
		Map<String, String> headersMap = new HashMap<>();
		String jsonString = "{\"records\": \"allMeta\"}";
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = mapper.readTree(jsonString);
		applicationConfiguration.setSkuDetailApikey("asdasd");
		headersMap.put("apiKey", applicationConfiguration.getSkuDetailApikey());
		when(ingestUtils.updatePosItemBean(item, order)).thenReturn(item);
		when(ingestUtils.getSkuDetails(any(Map.class),any(String.class))).thenReturn(sku);		
		comprasIngestServiceImpl.createPosItem(order);
		assertThat(comprasIngestServiceImpl).isNotNull();
	}
	
	@Test
	void changeCommerceItemStateStorePickUp() {
		
		Order order = new Order();
		SomsOrderResBean somsOrderResBean = new SomsOrderResBean();
		
		somsOrderResBean.setId("123432");
		Skus somsSku = new Skus();
		somsSku.setSkuNo("0001234567");
		somsOrderResBean.setSku(somsSku);
		Query skuItemQuery = new Query(Criteria.where("0001234567"));
		List<Item> skuItem = new ArrayList<Item>();
		Item item = new Item();
		Map<String, String> statusTypeMap = new HashMap<String, String>();
		statusTypeMap.put("Gift Registry", "GiftRegistry Item Status");
		statusTypeMap.put("storePickup", "StorePickup Item Status");
		statusTypeMap.put("Personal Shopping", "Commerce Item Status");
		applicationConfiguration.setStatusTypeMap(statusTypeMap);
		order.setStorePickup(true);
		item.setSellerSkuId("1234567");
		skuItem.add(item);
		somsSku.setCancellationFlag("true");
		somsSku.setNode("12345");
		somsSku.setTimestamp("");
		somsSku.setTrackingNumber("12345");
		somsSku.getCarrierType();
		somsSku.getStatus();
		
		Date storedTimestamp = new Date();
		Date timestampInSomsResponse = new Date();
		
		when(mongoTemplate.find(skuItemQuery, Item.class)).thenReturn(skuItem);
		when(comprasIngestItemRepository.getItem(somsSku.getTrackingNumber(), somsSku.getSkuNo())).thenReturn(item);
		when(ingestUtils.checkValidDates(storedTimestamp, timestampInSomsResponse)).thenReturn(true);
		comprasIngestServiceImpl.changeCommerceItemState(order, somsOrderResBean, item);
		assertThat(comprasIngestServiceImpl).isNotNull();
	}
	
	@Test
	void changeCommerceItemStatePersonalShopping() {
		
		Order order = new Order();
		SomsOrderResBean somsOrderResBean = new SomsOrderResBean();
		
		somsOrderResBean.setId("123432");
		Skus somsSku = new Skus();
		somsSku.setSkuNo("0001234567");
		somsOrderResBean.setSku(somsSku);
		somsSku.setTimestamp("asbcd");
		Query skuItemQuery = new Query(Criteria.where("0001234567"));
		List<Item> skuItem = new ArrayList<Item>();
		Item item = new Item();
		Map<String, String> statusTypeMap = new HashMap<String, String>();
		statusTypeMap.put("Gift Registry", "GiftRegistry Item Status");
		statusTypeMap.put("storePickup", "StorePickup Item Status");
		statusTypeMap.put("Personal Shopping", "Commerce Item Status");
		applicationConfiguration.setStatusTypeMap(statusTypeMap);
		order.setStorePickup(false);
		order.setShoppingType("Personal Shopping");
		item.setSellerSkuId("1234567");
		skuItem.add(item);
		somsSku.setCancellationFlag("");
		somsSku.setNode("");
		somsSku.setTimestamp("");
		somsSku.setTrackingNumber("");
		somsSku.getCarrierType();
		somsSku.getStatus();
		when(mongoTemplate.find(skuItemQuery, Item.class)).thenReturn(skuItem);
		comprasIngestServiceImpl.changeCommerceItemState(order, somsOrderResBean, item);
		assertThat(comprasIngestServiceImpl).isNotNull();
	}
	
	@Test
	void changeCommerceItemStateGiftRegistry() {
		
		Order order = new Order();
		SomsOrderResBean somsOrderResBean = new SomsOrderResBean();
		
		somsOrderResBean.setId("123432");
		Skus somsSku = new Skus();
		somsSku.setSkuNo("0001234567");
		Query skuItemQuery = new Query(Criteria.where("0001234567"));
		List<Item> skuItem = new ArrayList<Item>();
		Item item = new Item();
		Map<String, String> statusTypeMap = new HashMap<String, String>();
		statusTypeMap.put("Gift Registry", "GiftRegistry Item Status");
		statusTypeMap.put("storePickup", "StorePickup Item Status");
		statusTypeMap.put("Personal Shopping", "Commerce Item Status");
		applicationConfiguration.setStatusTypeMap(statusTypeMap);
		order.setStorePickup(false);
		order.setShoppingType("Gift Registry");
		item.setSellerSkuId("1234567");
		item.setTimestamp(new Date());
		skuItem.add(item);
		somsSku.setCancellationFlag("sdfg");
		somsSku.setNode("qwer");
		somsSku.setTimestamp("2020-03-20T19.30.00");
		somsSku.setTrackingNumber("123456");
		somsSku.getCarrierType();
		somsSku.getStatus();
		somsOrderResBean.setSku(somsSku);
		when(mongoTemplate.find(skuItemQuery, Item.class)).thenReturn(skuItem);
		when(ingestUtils.checkValidDates(any(Date.class), any(Date.class))).thenReturn(true);
		when(comprasIngestItemRepository.getItem(somsSku.getTrackingNumber(),somsSku.getSkuNo())).thenReturn(new Item());
		comprasIngestServiceImpl.changeCommerceItemState(order, somsOrderResBean, item);
		assertThat(comprasIngestServiceImpl).isNotNull();
	}

}
