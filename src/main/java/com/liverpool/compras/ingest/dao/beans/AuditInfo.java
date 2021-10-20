package com.liverpool.compras.ingest.dao.beans;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Document(collection = "auditmessages")
public class AuditInfo {
	@Id
	private String id;
	private String messageSource;
	private String message;
	private Date messageReceivedDate;
}
