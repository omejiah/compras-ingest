package com.liverpool.compras.ingest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import org.springframework.http.ResponseEntity;

import com.liverpool.compras.ingest.RestInvoker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.liverpool.compras.ingest.configuration.ApplicationConfiguration;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@PropertySource("classpath:com/liverpool/compras/ingest/application.properties")
@ContextConfiguration(classes = { RestInvoker.class },loader=AnnotationConfigContextLoader.class)
class RestInvokerTest {
	@MockBean
	private RestInvoker restInvoker;
	
	@InjectMocks
	private ApplicationConfiguration applicationConfiguration;
	
	private CloseableHttpClient httpClient = HttpClientBuilder.create().build();
	
	@MockBean
	private RestTemplate restTemplate;
	
	@MockBean
	private HttpComponentsClientHttpRequestFactory clientHttpRequestFactory;

	@BeforeEach
	 void setUp() {
		applicationConfiguration.setRestBaseURL("https://petstore.swagger.io/");
		clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		restInvoker = new RestInvoker(applicationConfiguration);
	}
	
	@Test
	void restTemplate() {
		restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory("https://petstore.swagger.io/"));
		assertThat(restInvoker.restTemplate()).isNotNull();
	}
	
	@Test
	void clientHttpRequestFactory() {
		assertThat(restInvoker.clientHttpRequestFactory()).isNotNull();
	}
    

	@Test
	void callServiceGet() throws JsonProcessingException {
		Map<String, String> headersMap = new HashMap<String, String>();
		applicationConfiguration.setSkuDetailApikey("1234");
		String serviceUrl = "v2/swagger.json";
		headersMap.put("apikey", "65712");
		HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		clientHttpRequestFactory.setHttpClient(httpClient);
		HttpEntity<String> entity = null;
		restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory("https://petstore.swagger.io/"));
		ResponseEntity<JsonNode> responseEntity = new ResponseEntity<>(HttpStatus.OK);
		when(restTemplate.exchange("apikey", HttpMethod.GET, entity, JsonNode.class)).thenReturn(responseEntity);
		assertThat(restInvoker.callServiceGet(serviceUrl,headersMap)).isNotNull();
	}
}
