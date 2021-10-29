package com.liverpool.compras.ingest.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.liverpool.compras.ingest.dao.beans.Order;

public interface ComprasIngestOrderRepository extends MongoRepository<Order, String> {

}
