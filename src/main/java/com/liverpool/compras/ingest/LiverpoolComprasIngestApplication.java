package com.liverpool.compras.ingest;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.InstantiatingExecutorProvider;
import com.google.cloud.ServiceOptions;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.liverpool.compras.ingest.beans.OrderUpdateBean;
import com.liverpool.compras.ingest.config.ComprasIngestConfigrations;
import com.liverpool.compras.ingest.constants.ComprasIngestConstants;
import com.liverpool.compras.ingest.dao.beans.Order;
import com.liverpool.compras.ingest.dao.beans.SomsOrderResBean;
import com.liverpool.compras.ingest.service.ComprasIngestService;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
@EnableScheduling
public class LiverpoolComprasIngestApplication {

	private ComprasIngestService comprasService;

	private ComprasIngestConfigrations ingestConfigrations;

	public LiverpoolComprasIngestApplication(ComprasIngestService comprasService,
			ComprasIngestConfigrations ingestConfigrations) {
		super();
		this.comprasService = comprasService;
		this.ingestConfigrations = ingestConfigrations;
	}

	public static void main(String[] args) {
		SpringApplication.run(LiverpoolComprasIngestApplication.class, args);
	}

	@Bean
	public void createOnlineOrders() {
		log.info("Start :: LiverpoolComprasIngestApplication.createOnlineOrders()");
		ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(ServiceOptions.getDefaultProjectId(),
				ingestConfigrations.getCreateOnlineOrderSubId());

		Subscriber subscriber = null;
		try {
			ExecutorProvider executorProvider = InstantiatingExecutorProvider.newBuilder().setExecutorThreadCount(4)
					.build();
			subscriber = Subscriber.newBuilder(subscriptionName, createOnlineOrderMessageReceiver())
					.setParallelPullCount(2).setExecutorProvider(executorProvider).build();
		      // Listen for Unrecoverable errors. Rebuild a subscriber and restart subscribing
		      // when the current subscriber encounters permanent errors.
				subscriber.addListener(new Subscriber.Listener() {
					public void failed(Subscriber.State from, Throwable failure) {
						log.error("Unrecoverable errors ::", Arrays.toString(failure.getStackTrace()));
						if (!executorProvider.getExecutor().isShutdown()) {
							createOnlineOrders();
						}
					}
				}, MoreExecutors.directExecutor());
				subscriber.startAsync().awaitRunning();
				log.info("Listening for messages on :: {}", subscriptionName.toString());
			// Allow the subscriber to run for 30s unless an unrecoverable error occurs.
			subscriber.awaitTerminated(30, TimeUnit.SECONDS);// TODO: Need to review
		} catch (TimeoutException timeoutException) {
			// Shut down the subscriber after 30s. Stop receiving messages.
			// subscriber.stopAsync();//TODO: Need to review
			log.error("Time out while receiving the message from the subscriber ::{}", timeoutException.getMessage());
		}
		log.info("End :: LiverpoolComprasIngestApplication.createOnlineOrders()");
	}

	@Bean
	public MessageReceiver createOnlineOrderMessageReceiver() {
		log.info("Start :: LiverpoolComprasIngestApplication.createOnlineOrderMessageReceiver()");
		Gson gson = new Gson();
		return (message, ackReplyConsumer) -> {
			Order orderIngestBean = gson.fromJson(message.getData().toStringUtf8(), Order.class);
			comprasService.addAuditEntry(message.getData().toStringUtf8(), ComprasIngestConstants.SOURCE_ONLINE);
			comprasService.createOrder(orderIngestBean);
			comprasService.createItem(orderIngestBean);
			ackReplyConsumer.ack();
			log.info("End :: LiverpoolComprasIngestApplication.createOnlineOrderMessageReceiver()");
		};
	}

	@Bean
	public void updateOnlineOrder() {
		log.info("Start :: LiverpoolComprasIngestApplication.updateOnlineOrder()");
		ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(ServiceOptions.getDefaultProjectId(),
				ingestConfigrations.getUpdateOnlineOrderSubId());

		Subscriber subscriber = null;
		try {
			ExecutorProvider executorProvider = InstantiatingExecutorProvider.newBuilder().setExecutorThreadCount(4)
					.build();
			subscriber = Subscriber.newBuilder(subscriptionName, updateOnlineOrderMessageReceiver())
					.setParallelPullCount(2).setExecutorProvider(executorProvider).build();
			subscriber.addListener(new Subscriber.Listener() {
				public void failed(Subscriber.State from, Throwable failure) {
					log.error("Unrecoverable errors ::", Arrays.toString(failure.getStackTrace()));
					if (!executorProvider.getExecutor().isShutdown()) {
						createOnlineOrders();
					}
				}
			}, MoreExecutors.directExecutor());
			subscriber.startAsync().awaitRunning();
			log.info("Listening for messages on :: {}", subscriptionName.toString());
			// Allow the subscriber to run for 30s unless an unrecoverable error occurs.
			subscriber.awaitTerminated(30, TimeUnit.SECONDS);// TODO: Need to review
		} catch (TimeoutException timeoutException) {
			// Shut down the subscriber after 30s. Stop receiving messages.
			// subscriber.stopAsync();//TODO: Need to review
			log.error("Time out while receiving the message from the subscriber ::{}", timeoutException.getMessage());
		}
		log.info("End :: LiverpoolComprasIngestApplication.updateOnlineOrder()");
	}

	@Bean
	public MessageReceiver updateOnlineOrderMessageReceiver() {
		log.info("Start :: LiverpoolComprasIngestApplication.updateOnlineOrderMessageReceiver()");
		Gson gson = new Gson();
		return (message, ackReplyConsumer) -> {
			OrderUpdateBean orderUpdateBean = gson.fromJson(message.getData().toStringUtf8(), OrderUpdateBean.class);
			comprasService.addAuditEntry(message.getData().toStringUtf8(), ComprasIngestConstants.SOURCE_ONLINE);
			comprasService.updateOrder(orderUpdateBean);
			ackReplyConsumer.ack();
			log.info("End :: LiverpoolComprasIngestApplication.updateOnlineOrderMessageReceiver()");
		};
	}

	@Bean
	public void tloUpdates() {
		log.info("Start :: LiverpoolComprasIngestApplication.tloUpdates()");
		ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(ServiceOptions.getDefaultProjectId(),
				ingestConfigrations.getTloUpdatesSubId());

		Subscriber subscriber = null;
		try {
			ExecutorProvider executorProvider = InstantiatingExecutorProvider.newBuilder().setExecutorThreadCount(4)
					.build();
			subscriber = Subscriber.newBuilder(subscriptionName, tloUpdatesMessageReceiver()).setParallelPullCount(2)
					.setExecutorProvider(executorProvider).build();
			subscriber.addListener(new Subscriber.Listener() {
				public void failed(Subscriber.State from, Throwable failure) {
					log.error("Unrecoverable errors ::", Arrays.toString(failure.getStackTrace()));
					if (!executorProvider.getExecutor().isShutdown()) {
						createOnlineOrders();
					}
				}
			}, MoreExecutors.directExecutor());
			subscriber.startAsync().awaitRunning();
			log.info("Listening for messages on :: {}", subscriptionName.toString());
			// Allow the subscriber to run for 30s unless an unrecoverable error occurs.
			subscriber.awaitTerminated(30, TimeUnit.SECONDS);// TODO: Need to review
		} catch (TimeoutException timeoutException) {
			// Shut down the subscriber after 30s. Stop receiving messages.
			// subscriber.stopAsync();//TODO: Need to review
			log.error("Time out while receiving the message from the subscriber ::{}", timeoutException.getMessage());
		}
		log.info("End :: LiverpoolComprasIngestApplication.tloUpdates()");
	}

	@Bean
	public MessageReceiver tloUpdatesMessageReceiver() {
		log.info("Start :: LiverpoolComprasIngestApplication.tloUpdatesMessageReceiver()");
		return (message, ackReplyConsumer) -> {
			ObjectMapper oMapper = new ObjectMapper();
			try {
				SomsOrderResBean sob = oMapper.readValue(message.getData().toStringUtf8(), SomsOrderResBean.class);
				comprasService.addAuditEntry(message.getData().toStringUtf8(), ComprasIngestConstants.SOURCE_TLO);
				comprasService.updateTLOParams(sob);
				ackReplyConsumer.ack();
			} catch (JsonProcessingException e) {
				log.error("JsonProcessingException ::",e);
			}
			log.info("End :: LiverpoolComprasIngestApplication.tloUpdatesMessageReceiver()");
		};
	}

	@Bean
	public void createPosOrders() {
		log.info("Start :: LiverpoolComprasIngestApplication.createPosOrders()");
		ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(ServiceOptions.getDefaultProjectId(),
				ingestConfigrations.getPosUpdatesSubId());

		Subscriber subscriber = null;
		try {
			ExecutorProvider executorProvider = InstantiatingExecutorProvider.newBuilder().setExecutorThreadCount(4)
					.build();
			subscriber = Subscriber.newBuilder(subscriptionName, createPosOrdersMessageReceiver())
					.setParallelPullCount(2).setExecutorProvider(executorProvider).build();
			subscriber.addListener(new Subscriber.Listener() {
				public void failed(Subscriber.State from, Throwable failure) {
					log.error("Unrecoverable errors ::", Arrays.toString(failure.getStackTrace()));
					if (!executorProvider.getExecutor().isShutdown()) {
						createOnlineOrders();
					}
				}
			}, MoreExecutors.directExecutor());
			subscriber.startAsync().awaitRunning();
			log.info("Listening for messages on :: {}", subscriptionName.toString());
			// Allow the subscriber to run for 30s unless an unrecoverable error occurs.
			subscriber.awaitTerminated(30, TimeUnit.SECONDS);// TODO: Need to review
		} catch (TimeoutException timeoutException) {
			// Shut down the subscriber after 30s. Stop receiving messages.
			// subscriber.stopAsync();//TODO: Need to review
			log.error("Time out while receiving the message from the subscriber ::{}", timeoutException.getMessage());
		}
		log.info("End :: LiverpoolComprasIngestApplication.createPosOrders()");
	}

	@Bean
	public MessageReceiver createPosOrdersMessageReceiver() {
		log.info("Start :: LiverpoolComprasIngestApplication.createPosOrdersMessageReceiver()");
		Gson gson = new Gson();
		return (message, ackReplyConsumer) -> {
			Order orderIngestBean = gson.fromJson(message.getData().toStringUtf8(), Order.class);
			comprasService.addAuditEntry(message.getData().toStringUtf8(), ComprasIngestConstants.SOURCE_PHYSICAL);
			comprasService.createOrder(orderIngestBean);
			comprasService.createPosItem(orderIngestBean);
			ackReplyConsumer.ack();
			log.info("End :: LiverpoolComprasIngestApplication.createPosOrdersMessageReceiver()");
		};
	}
}
