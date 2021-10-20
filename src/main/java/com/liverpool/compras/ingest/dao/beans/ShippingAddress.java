package com.liverpool.compras.ingest.dao.beans;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddress {
	private String nickName;
	private String street;
	private String entreLasCalles;
	private String yCalle;
	private String ciudad;
	private String estado;
	private String pais;
	private String muncipioDelegacion;
	private String exteriorNumber;
	private String codigoPostal;
	private String phone;
	private String officePhoneNumber;
	private String cellPhone;
}
