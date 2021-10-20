package com.liverpool.compras.ingest.scheduler;

import java.util.Date;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.liverpool.compras.ingest.configuration.ApplicationConfiguration;
import com.liverpool.compras.ingest.dao.beans.AuditInfo;
import com.liverpool.compras.ingest.repository.ComprasIngestAuditRepository;
import com.liverpool.compras.ingest.utils.ComprasIngestUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AuditingDataPurgeScheduler {

	private ComprasIngestAuditRepository comprasIngestAuditRepository;
	private ApplicationConfiguration applicationConfiguration;

	public AuditingDataPurgeScheduler(ComprasIngestAuditRepository comprasIngestAuditRepository,
			ApplicationConfiguration applicationConfiguration) {
		super();
		this.comprasIngestAuditRepository = comprasIngestAuditRepository;
		this.applicationConfiguration = applicationConfiguration;
	}

	public void setComprasIngestAuditRepository(ComprasIngestAuditRepository comprasIngestAuditRepository) {
		this.comprasIngestAuditRepository = comprasIngestAuditRepository;
	}

	@Scheduled(cron = "0 0 23 * * *")
	public void purgeAuditingData() {
		log.info("Start:: AuditingDataPurgeScheduler.purgeAuditingData()");
		Date date = ComprasIngestUtils.addDaysToDate(new Date(), applicationConfiguration.getPurgingDays());
		List<AuditInfo> auditDataList = comprasIngestAuditRepository.getAuditDataByDate(date);
		log.info("Number of records:: {}", auditDataList.size());
		comprasIngestAuditRepository.deleteAll(auditDataList);
		log.info("End:: AuditingDataPurgeScheduler.purgeAuditingData()");
	}
}