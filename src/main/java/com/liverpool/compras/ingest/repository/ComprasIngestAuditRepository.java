package com.liverpool.compras.ingest.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.liverpool.compras.ingest.dao.beans.AuditInfo;

public interface ComprasIngestAuditRepository extends MongoRepository<AuditInfo, String> {

	@Query("{'messageReceivedDate' : { $lte: ?0 } }")
	public List<AuditInfo> getAuditDataByDate(Date from);
}
