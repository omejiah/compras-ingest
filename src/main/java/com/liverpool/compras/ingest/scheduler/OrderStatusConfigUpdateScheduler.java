package com.liverpool.compras.ingest.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.liverpool.compras.ingest.OrderStatusConfigurationService;
import lombok.extern.slf4j.Slf4j;
/**
 * This scheduler will run in every 5 min and will updated the OrderStatusBeanList
 * @author shakti
 *
 */
@Component
@Slf4j
public class OrderStatusConfigUpdateScheduler {

	private OrderStatusConfigurationService orderStatusConfigurationService;

	public OrderStatusConfigUpdateScheduler(OrderStatusConfigurationService orderStatusConfigurationService) {
		super();
		this.orderStatusConfigurationService = orderStatusConfigurationService;
	}

	/**
	 * This method will be called every 5 minutes and will update the order status bean list.
	 */
	@Scheduled(fixedRate = 300000)
	public void updateTLOTask() {
		log.debug("Start:: OrderStatusConfigUpdateScheduler.updateTLOTask()");
		orderStatusConfigurationService.populateTloData();
		log.debug("End:: OrderStatusConfigUpdateScheduler.updateTLOTask()");
	}
}