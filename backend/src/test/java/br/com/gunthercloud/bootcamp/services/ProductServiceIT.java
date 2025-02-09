package br.com.gunthercloud.bootcamp.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import br.com.gunthercloud.bootcamp.entitites.dto.ProductDTO;
import br.com.gunthercloud.bootcamp.repositories.ProductRepository;
import br.com.gunthercloud.bootcamp.services.exceptions.ResourceNotFoundException;

@SpringBootTest
@Transactional
public class ProductServiceIT {

	@Autowired
	private ProductService service;
	
	@Autowired
	private ProductRepository repository;

	private Long existingId;
	private Long nonExistingId;
	private int countTotalProducts;
	
	@BeforeEach
	void setUp() throws Exception {

		existingId = 1L;
		nonExistingId = 1000L;
		countTotalProducts = 25;
		
	}

	@Test
	public void deleteShouldDeleteResourceWhenIdExists() {
		
		service.delete(existingId);
		
		Assertions.assertEquals(countTotalProducts - 1, repository.count());
	}

	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.delete(nonExistingId);
		});
	}

	@Test
	public void findAllPagedShouldReturnPageProductDTO() {
		
		PageRequest page = PageRequest.of(0, 10);
		
		Page<ProductDTO> list = service.findAllPaged(page);
		
		Assertions.assertFalse(list.isEmpty());
		Assertions.assertEquals(0, page.getPageNumber());
		Assertions.assertEquals(countTotalProducts, list.getTotalElements());
		Assertions.assertEquals(page.getPageSize(), list.getSize());
		
	}
	
	@Test
	public void findAllPagedShouldReturnEmptyPageDoesNotExist() {
		
		PageRequest page = PageRequest.of(50, 10);
		
		Page<ProductDTO> list = service.findAllPaged(page);
		
		Assertions.assertTrue(list.isEmpty());
		
	}
	
	@Test
	public void findAllPagedShouldReturnSortedWhenSortByName() {
		
		PageRequest page = PageRequest.of(0, 10, Sort.by("name"));
		
		Page<ProductDTO> list = service.findAllPaged(page);

		Assertions.assertFalse(list.isEmpty());
		Assertions.assertEquals("Macbook Pro", list.getContent().get(0).getName());
		Assertions.assertEquals("PC Gamer", list.getContent().get(1).getName());
		Assertions.assertEquals("PC Gamer Alfa", list.getContent().get(2).getName());
		
	}
	
}
