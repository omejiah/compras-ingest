package com.liverpool.compras.ingest.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.liverpool.compras.ingest.dao.beans.Item;

public interface ComprasIngestItemRepository extends MongoRepository<Item, String>{

	@Query("{'remissionId':?0, 'skuId':?1}")
	Item getItem(String orderId, String skuID);
}
