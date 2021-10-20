package com.liverpool.compras.ingest;

import java.util.Map;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.liverpool.compras.ingest.configuration.ApplicationConfiguration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Component
@Slf4j
public class RestInvoker {

	private ApplicationConfiguration applicationConfiguration;

	public RestInvoker(ApplicationConfiguration applicationConfiguration) {
		this.applicationConfiguration = applicationConfiguration;
	}

	private CloseableHttpClient httpClient = HttpClientBuilder.create().build();

	@Bean
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());
		restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(applicationConfiguration.getRestBaseURL()));
		return restTemplate;
	}

	@Bean
	@ConditionalOnMissingBean
	public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory() {
		HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		clientHttpRequestFactory.setHttpClient(httpClient);
		return clientHttpRequestFactory;
	}

	public <I, O> O callServicePost(String servicePath, I requestObj, Class<O> responseType) {

		RestTemplate restTemplate = restTemplate();
		HttpEntity<I> request = new HttpEntity<>(requestObj);
		log.debug("calling service {} for request {}", servicePath, requestObj);
		return restTemplate.postForObject(servicePath, request, responseType);
	}

	public JsonNode callServiceGet(String servicePath, Map<String, String> headersMap) throws JsonProcessingException {
		RestTemplate restTemplate = restTemplate();
		HttpEntity<String> entity = null;
		if (headersMap != null && !headersMap.isEmpty()) {
			HttpHeaders headers = new HttpHeaders();
			headersMap.entrySet().stream().forEach(header -> headers.set(header.getKey(), header.getValue()));
			entity = new HttpEntity<>(headers);
		}
		log.info("Calling service for the request :::{}", servicePath);
		ResponseEntity<JsonNode> jsonNode = restTemplate.exchange(servicePath, HttpMethod.GET, entity, JsonNode.class);
		log.info("Response received is :::{}", jsonNode);
		return jsonNode.getBody();
	}
}
