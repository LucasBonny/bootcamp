package br.com.gunthercloud.bootcamp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.gunthercloud.bootcamp.entitites.Product;

public interface ProductRepository extends JpaRepository<Product, Long>{

}
