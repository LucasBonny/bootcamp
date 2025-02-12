package br.com.gunthercloud.bootcamp.services;

import java.util.HashSet;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gunthercloud.bootcamp.entitites.Category;
import br.com.gunthercloud.bootcamp.entitites.Product;
import br.com.gunthercloud.bootcamp.entitites.dto.CategoryDTO;
import br.com.gunthercloud.bootcamp.entitites.dto.ProductDTO;
import br.com.gunthercloud.bootcamp.repositories.CategoryRepository;
import br.com.gunthercloud.bootcamp.repositories.ProductRepository;
import br.com.gunthercloud.bootcamp.services.exceptions.DatabaseException;
import br.com.gunthercloud.bootcamp.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;

@Service
public class ProductService {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private CategoryRepository categoryRepository;
	
	@Transactional(readOnly = true)
	public Page<ProductDTO> findAllPaged(Pageable pageable) {
		Page<Product> list = productRepository.findAll(pageable);
		return list.map(x -> {
			return new ProductDTO(x, new HashSet<>(x.getCategories()));
		});
	}

	@Transactional(readOnly = true)
	public ProductDTO findById(Long id) {
		Optional<Product> obj = productRepository.findById(id);
		/* Product o = obj.orElseThrow(() -> new ResourceNotFoundException("teste")); return new ProductDTO(o, o.getCategories()); */
		return obj.map(x -> {
			return new ProductDTO(x, x.getCategories());
		}).orElseThrow(() -> new ResourceNotFoundException("Id " + id + " not found!"));
	}

	@Transactional
	public ProductDTO insert(ProductDTO obj) {
		Product entity = new Product();
		copyDtoToEntity(obj, entity);
		productRepository.save(entity);
		return new ProductDTO(entity, entity.getCategories());
	}

	@Transactional
	public ProductDTO update(Long id, ProductDTO obj) {
		try {			
			Product entity = productRepository.getReferenceById(id);
			copyDtoToEntity(obj, entity);
			obj.setId(id);
			productRepository.save(entity);
			return new ProductDTO(entity, entity.getCategories());
		}
		catch(EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id " + id + " not found!");
		}
		catch(RuntimeException e) {
			throw new ResourceNotFoundException(e.getMessage());
		}
		
	}

	public void delete(Long id) {
		if(!productRepository.existsById(id))
			throw new ResourceNotFoundException("Id " + id + " not found!");
		try {
			productRepository.deleteById(id);
		}
		catch(DataIntegrityViolationException e) {
			throw new DatabaseException("Product delete error.");
		}
		catch(RuntimeException  e) {
			throw new DatabaseException(e.getMessage());
		}
		
	}
	private Product copyDtoToEntity(ProductDTO dto, Product entity) {
		if(dto.getId() != null) entity.setId(dto.getId());
		entity.setName(dto.getName());
		entity.setDescription(dto.getDescription());
		entity.setPrice(dto.getPrice());
		entity.setImgUrl(dto.getImgUrl());
		entity.setDate(dto.getDate());
		entity.getCategories().clear();
		for(CategoryDTO d : dto.getCategories()) {
			Category cat = categoryRepository.getReferenceById(d.getId());
			entity.getCategories().add(cat);
		}
		return entity;
	}

}
