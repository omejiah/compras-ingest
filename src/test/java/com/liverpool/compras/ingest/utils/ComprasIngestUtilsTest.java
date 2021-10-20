package com.liverpool.compras.ingest.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.liverpool.compras.ingest.RestInvoker;
import com.liverpool.compras.ingest.configuration.ApplicationConfiguration;
import com.liverpool.compras.ingest.constants.ComprasIngestConstants;
import com.liverpool.compras.ingest.dao.beans.Item;
import com.liverpool.compras.ingest.dao.beans.Order;
import com.liverpool.compras.ingest.dao.beans.OrderStatusMapBean;
import com.liverpool.compras.ingest.dao.beans.SkuDetails;
import com.liverpool.compras.ingest.dao.beans.Skus;


@SpringBootTest
@ExtendWith(SpringExtension.class)
@PropertySource("classpath:application.properties")
@ContextConfiguration(classes = { ComprasIngestUtils.class })
class ComprasIngestUtilsTest {

	@MockBean
	private ComprasIngestUtils comprasIngestUtils;

	@MockBean
	private RestInvoker restInvoker;
	
//	@MockBean
//	private OrderStatusMapBean orderStatus;
	

	@InjectMocks
	private ApplicationConfiguration applicationConfiguration;
	
//	private CloseableHttpClient httpClient = HttpClientBuilder.create().build();
	

	@BeforeEach
	public void setUp() {
		comprasIngestUtils = new ComprasIngestUtils(restInvoker, applicationConfiguration);
		applicationConfiguration.setSkuDetailApikey("");
		applicationConfiguration.setRestBaseURL("");
		applicationConfiguration.setSkuDetailApiUrl("");
	}

	@Test
	void populateSKUDetailsTest() throws JsonMappingException, JsonProcessingException {
		String jsonString = "{\"skuId\": \"012\",\"color\":\"\",\"size\":\"\",\"smallImage\":\"\"}";
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = mapper.readTree(jsonString);
		ObjectNode allMeta = mapper.createObjectNode();
		allMeta.set("department", mapper.convertValue("", JsonNode.class));
		allMeta.set("materialGroup", mapper.convertValue("", JsonNode.class));

		assertThat(comprasIngestUtils.populateSKUDetails(jsonNode, new SkuDetails(), allMeta, "012")).isNotNull();
		
		assertThat(comprasIngestUtils.populateSKUDetails(jsonNode, new SkuDetails(), allMeta, "234")).isNotNull();
	}
	@Test
	void checkValidDates() throws ParseException {
		assertTrue(comprasIngestUtils.checkValidDates(null,null));
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		Date t = formatter.parse("12/01/2020");
		Date t2 = formatter.parse("01/01/2020");
		assertFalse(comprasIngestUtils.checkValidDates(t,t2));
	}

	@Test
	void updateItemBeanTest() {
		Item item = new Item();
		item.setId("3423423");
		Order order = new Order();
		order.setSourceOrderId("234234");
		order.setSourceShipId("sg18328726");
		order.setOrderRef("223232");
		order.setCustomerId("234234");
		comprasIngestUtils.updateItemBean(item, order);
		assertThat(comprasIngestUtils.updateItemBean(item, order)).isNotNull();
	}

	@Test
	void populateItemIdstoListTest() {
		Order order = new Order();
		List<Item> items = new ArrayList<>();
		Item item = new Item();
		item.setId("3423423");
		items.add(item);
		order.setItems(items);
		order.setSourceOrderId("3453454");
		order.setSourceShipId("sg3863487");
		comprasIngestUtils.populateItemIdstoList(order);
		assertThat(comprasIngestUtils).isNotNull();
	}

	@Test
	void updateOrderBeanTest() {
		Order order = new Order();
		comprasIngestUtils.updateOrderBean(order);
		assertThat(comprasIngestUtils.updateOrderBean(order)).isNotNull();
	}
	
	@Test
	void getSkuDetails() throws JsonMappingException, JsonProcessingException {
		Map<String, String> headersMap = new HashMap<>();
		
		ObjectMapper mapper = new ObjectMapper();
		// create three JSON objects
		ObjectNode vendor1 = mapper.createObjectNode();
		ObjectNode vendor = mapper.createObjectNode();
		ObjectNode vendor2 = mapper.createObjectNode();
		//vendor1.put("allMeta", "1");
		// create nested arrays

		vendor2.put("name", "asdf");
		vendor.put("garantia", "GARANTIA");
		vendor1.put("allMeta", vendor);
		vendor.put("variants", vendor2);
		ArrayNode arrayNode = mapper.createArrayNode();
		arrayNode.addAll(Arrays.asList(vendor1));
		JsonNode result = mapper.createObjectNode().set("records", arrayNode);
		headersMap.put("apiKey", applicationConfiguration.getSkuDetailApikey());
		when(restInvoker.callServiceGet(any(String.class), any(Map.class))).thenReturn(result);
		assertThat(comprasIngestUtils.getSkuDetails(headersMap,"v2/swagger.json")).isNotNull();
	}
	
	@Test
	void getSkuDetailsNull() throws JsonMappingException, JsonProcessingException {
		Map<String, String> headersMap = new HashMap<>();
		String jsonString = "{\"records\": \"allMeta\"}";
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = mapper.readTree(jsonString);
		headersMap.put("apiKey", applicationConfiguration.getSkuDetailApikey());
		when(restInvoker.callServiceGet("v2/swagger.json", headersMap)).thenReturn(jsonNode);
		assertThat(comprasIngestUtils.getSkuDetails(headersMap,"v2/swagger.json")).isNull();
	}

	@Test
	void updatePosItemBean() {
		Item item = new Item();
		item.setSkuId("342432");
		Order order = new Order();
		order.setSourceOrderId("234234");
		order.setSourceShipId("sg18328726");
		order.setOrderRef("223232");
		order.setCustomerId("234234");
		assertThat(comprasIngestUtils.updatePosItemBean(item, order)).isNotNull();
	}

	@Test
	void getBarcode() {
		Order order = new Order();
		order.setChargeNumber("1234567890");
		order.setCertificado("1234567890");
		order.setPurchaseDate(new Date());
		order.setChargeNumber("2343433232211222");
		order.setCertificado("3434344");
		assertThat(comprasIngestUtils.getBarcode(order)).isNotNull();
	}

	@Test
	void updateLengthForFields_NonZero() {
		assertThat(comprasIngestUtils.updateLengthForFields("1234567890", 1)).isNotNull();
	}

	@Test
	void updateLengthForFields_ZeroLength() {
		assertThat(comprasIngestUtils.updateLengthForFields("", 1)).isNotNull();
	}

	@Test
	void getEstimatedDeliveryDateForSLTest() {
		assertThat(comprasIngestUtils.getEstimatedDeliveryDateForSL(1, 2)).isNotNull();
	}

	@Test
	void isSinglePackageRemoveTest_Success() {
		Item item = new Item();
		item.setRemovedFromPackage(Boolean.TRUE);
		assertTrue(comprasIngestUtils.isSinglePackageRemove(item));
	}

	@Test
	void isSinglePackageRemoveTest_Failure() {
		Item item = new Item();
		item.setRemovedFromPackage(Boolean.FALSE);
		assertFalse(comprasIngestUtils.isSinglePackageRemove(item));
	}

	@Test
	void getformatedDateforStageTest_NotNull() {
		assertThat(comprasIngestUtils.getformatedDateforStage(new Date())).isNotNull();
	}

	@Test
	void getformatedDateforStageTest_Null() {
		assertThat(comprasIngestUtils.getformatedDateforStage(null)).isNull();
	}

	@Test
	void caluculateAndFormatEDD_NotNull() {
		assertThat(comprasIngestUtils.caluculateAndFormatEDD(new Date(), new Date(), "")).isNotNull();
	}

	@Test
	void caluculateAndFormatEDD_StartDateNull() {
		assertThat(comprasIngestUtils.caluculateAndFormatEDD(null, new Date(), "")).isNotNull();
	}

	@Test
	void checkValidDatesTest_Success() {
		assertTrue(comprasIngestUtils.checkValidDates(new Date(), new Date()));
	}

	@Test
	void convertToDate_Success() {
		assertThat(comprasIngestUtils.convertToDate("2020-01-22")).isNotNull();
	}

	  @Test 
	  void setBundleCommerceItemPropertiesTest() { 
	  HashMap<String, String> orderStatusMap = new HashMap<>();
	  orderStatusMap.put(ComprasIngestConstants.STAGELEVEL, "stage1Date"); 
	  Item item = new Item(); 
	  item.setStage1Date(null);
	  item.setStage2Date("stage2Date"); 
	  item.setStage3Date("stage3Date");
	  item.setStage4Date("stage4Date"); 
	  item.setProductType("SOFT_LINE");
	  item.setPackageApplied(true); 
	  item.setRemovedFromPackage(true);
	  item.setRemovedFromPackageDate(new Date()); 
	  Skus somsSku = new Skus();
	  somsSku.setSkuNo("2342343"); 
	  somsSku.setNode("node");
	  somsSku.setRemovePackage("true"); 
	  somsSku.setEddStartDate("eddStartDate");
	  somsSku.setDeliveryDate("34324324"); 
	  OrderStatusMapBean orderStatus = new OrderStatusMapBean(); 
	  orderStatus.setStatusName("statusName");
	  orderStatus.setStatusType("statusType"); 
	  orderStatus.setSomsNode("somsNode");
	  applicationConfiguration.setRestBaseURL("https://petstore.swagger.io/");
	  applicationConfiguration.setOrderStatusService("http://192.168.0.132:7003/public/v1/configuration/getOrderStatusConfiguration");
	  when(restInvoker.callServicePost(applicationConfiguration.getOrderStatusService(), orderStatus, HashMap.class)).thenReturn(orderStatusMap);
	  comprasIngestUtils.setBundleCommerceItemProperties(orderStatus, item, somsSku, true); 
	  assertThat(comprasIngestUtils).isNotNull();
	  
	  }
	  
	  @Test 
	  void setBundleCommerceItemProperties2Test() { 
	  HashMap<String, String> orderStatusMap = new HashMap<>();
	  orderStatusMap.put(ComprasIngestConstants.STAGELEVEL, "stage2Date"); 
	  Item item = new Item(); 
	  item.setStage1Date("stage1Date");
	  item.setStage2Date(null); 
	  item.setStage3Date("stage3Date");
	  item.setStage4Date("stage4Date"); 
	  item.setProductType("SOFT_LINE");
	  item.setPackageApplied(true); 
	  item.setRemovedFromPackage(true);
	  item.setRemovedFromPackageDate(new Date()); 
	  Skus somsSku = new Skus();
	  somsSku.setSkuNo("2342343"); 
	  somsSku.setNode("node");
	  somsSku.setRemovePackage("true"); 
	  somsSku.setEddStartDate("eddStartDate");
	  somsSku.setDeliveryDate("34324324"); 
	  OrderStatusMapBean orderStatus = new OrderStatusMapBean(); 
	  orderStatus.setStatusName("statusName");
	  orderStatus.setStatusType("statusType"); 
	  orderStatus.setSomsNode("somsNode");
	  applicationConfiguration.setRestBaseURL("https://petstore.swagger.io/");
	  applicationConfiguration.setOrderStatusService("http://192.168.0.132:7003/public/v1/configuration/getOrderStatusConfiguration");
	  when(restInvoker.callServicePost(applicationConfiguration.getOrderStatusService(), orderStatus, HashMap.class)).thenReturn(orderStatusMap);
	  comprasIngestUtils.setBundleCommerceItemProperties(orderStatus, item, somsSku, true); 
	  assertThat(comprasIngestUtils).isNotNull();
	  
	  }
	  
	  @Test 
	  void setBundleCommerceItemProperties3Test() { 
	  HashMap<String, String> orderStatusMap = new HashMap<>();
	  orderStatusMap.put(ComprasIngestConstants.STAGELEVEL, "stage3Date"); 
	  Item item = new Item(); 
	  item.setStage1Date("stage1Date");
	  item.setStage2Date("stage2Date"); 
	  item.setStage4Date("stage3Date");
	  item.setStage3Date(null); 
	  item.setProductType("Big Ticket");
	  item.setPackageApplied(true); 
	  item.setRemovedFromPackage(true);
	  item.setRemovedFromPackageDate(new Date()); 
	  Skus somsSku = new Skus();
	  somsSku.setSkuNo("2342343"); 
	  somsSku.setNode("node");
	  somsSku.setRemovePackage("true"); 
	  somsSku.setEddStartDate("eddStartDate");
	  somsSku.setDeliveryDate("34324324"); 
	  OrderStatusMapBean orderStatus = new OrderStatusMapBean(); 
	  orderStatus.setStatusName("statusName");
	  orderStatus.setStatusType("statusType"); 
	  orderStatus.setSomsNode("somsNode");
	  applicationConfiguration.setRestBaseURL("https://petstore.swagger.io/");
	  applicationConfiguration.setOrderStatusService("http://192.168.0.132:7003/public/v1/configuration/getOrderStatusConfiguration");
	  when(restInvoker.callServicePost(applicationConfiguration.getOrderStatusService(), orderStatus, HashMap.class)).thenReturn(orderStatusMap);
	  comprasIngestUtils.setBundleCommerceItemProperties(orderStatus, item, somsSku, true); 
	  assertThat(comprasIngestUtils).isNotNull();
	  
	  }
	  
	  @Test 
	  void setBundleCommerceItemProperties4Test() { 
	  HashMap<String, String> orderStatusMap = new HashMap<>();
	  orderStatusMap.put(ComprasIngestConstants.STAGELEVEL, "stage4Date"); 
	  Item item = new Item(); 
	  item.setStage1Date("stage1Date");
	  item.setStage2Date("stage2Date"); 
	  item.setStage3Date("stage3Date");
	  item.setStage4Date(null); 
	  item.setProductType("SOFT_LINE");
	  item.setPackageApplied(true); 
	  item.setRemovedFromPackage(true);
	  item.setRemovedFromPackageDate(new Date()); 
	  Skus somsSku = new Skus();
	  somsSku.setSkuNo("2342343"); 
	  somsSku.setNode("node");
	  somsSku.setRemovePackage("true"); 
	  somsSku.setEddStartDate("eddStartDate");
	  somsSku.setDeliveryDate("34324324"); 
	  OrderStatusMapBean orderStatus = new OrderStatusMapBean(); 
	  orderStatus.setStatusName("statusName");
	  orderStatus.setStatusType("statusType"); 
	  orderStatus.setSomsNode("somsNode");
	  applicationConfiguration.setRestBaseURL("https://petstore.swagger.io/");
	  applicationConfiguration.setOrderStatusService("http://192.168.0.132:7003/public/v1/configuration/getOrderStatusConfiguration");
	  when(restInvoker.callServicePost(applicationConfiguration.getOrderStatusService(), orderStatus, HashMap.class)).thenReturn(orderStatusMap);
	  comprasIngestUtils.setBundleCommerceItemProperties(orderStatus, item, somsSku, true); 
	  assertThat(comprasIngestUtils).isNotNull();
	  
	  }
	
}
