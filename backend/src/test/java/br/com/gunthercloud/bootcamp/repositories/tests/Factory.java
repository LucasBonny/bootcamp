package br.com.gunthercloud.bootcamp.repositories.tests;

import java.time.Instant;

import br.com.gunthercloud.bootcamp.entitites.Product;

public class Factory {
	
	public static Product product() {
		Product product = new Product(26L,"Test1", "Test2", 123.0, "http://local/", Instant.now());
		return product;
	}

}
