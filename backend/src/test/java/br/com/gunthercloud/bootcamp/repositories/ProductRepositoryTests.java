package br.com.gunthercloud.bootcamp.repositories;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;

import br.com.gunthercloud.bootcamp.entitites.Product;
import br.com.gunthercloud.bootcamp.repositories.tests.Factory;

@DataJpaTest
public class ProductRepositoryTests {
	
	@Autowired
	private ProductRepository repository;
	
	private long existingId;
	private long nonExistingId;
	private long countTotalProducts;
	
	@BeforeEach
	private void setUp() throws Exception {
		existingId = 1L;
		nonExistingId = 1000L;
		countTotalProducts = 25L;
	}
	
	@Test
	public void saveSouldPersistWithAutoincrementWhenIdIsNull() {
		
		Product product = Factory.createProduct();
		product.setId(null);
		
		product = repository.save(product);
		// Não presente
		Optional<Product> result = repository.findById(product.getId());
		//
		
		Assertions.assertNotNull(product.getId());
		Assertions.assertEquals(countTotalProducts + 1 , product.getId());
		// Não presente
		Assertions.assertFalse(result.isEmpty());
		Assertions.assertSame(product, result.get());
		//
	}
	
	@Test
	public void findByidShouldReturnNotNullWhenIdExists() {
		
		Optional<Product> product = repository.findById(existingId);
		
		Assertions.assertFalse(product.isEmpty());
	}
	
	@Test
	public void findByIdShouldReturnNullWhenIdNotExists() {
		
		Optional<Product> product = repository.findById(nonExistingId);
		
		Assertions.assertTrue(product.isEmpty());
	}
	
	@Test
	public void deleteShouldDeleteObjectWhenIdExists() {
		
		repository.deleteById(existingId);
		Optional<Product> result = repository.findById(existingId);
		
		Assertions.assertTrue(result.isEmpty());
	}

	@Test
	public void deleteShouldThrowExceptionWhenIdDoesNotExists() {
		
		Assertions.assertThrows(EmptyResultDataAccessException.class, () -> {
			if(!repository.existsById(nonExistingId))
				throw new EmptyResultDataAccessException(0);
			repository.deleteById(nonExistingId);
		});
	}
	
}
