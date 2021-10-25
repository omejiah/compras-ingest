package com.liverpool.compras.ingest.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.liverpool.compras.ingest.TloUpdateService;
import com.liverpool.compras.ingest.configuration.ApplicationConfiguration;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TLOUpatedScheduler {

	private ApplicationConfiguration applicationConfiguration;
	private TloUpdateService tloUpdateService;

	public TLOUpatedScheduler(
			ApplicationConfiguration applicationConfiguration, TloUpdateService tloUpdateService) {
		super();
		this.applicationConfiguration = applicationConfiguration;
		this.tloUpdateService = tloUpdateService;
	}

	@Scheduled(fixedRate = 300000)
	public void updateTLOTask() {
		log.info("Start:: TLOUpatedScheduler.updateTLOTask()");
		tloUpdateService.populateTloData();
		log.info("End:: TLOUpatedScheduler.updateTLOTask()");
	}
}