package br.com.gunthercloud.bootcamp.services;

import java.util.HashSet;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gunthercloud.bootcamp.entitites.Product;
import br.com.gunthercloud.bootcamp.entitites.dto.ProductDTO;
import br.com.gunthercloud.bootcamp.repositories.ProductRepository;
import br.com.gunthercloud.bootcamp.services.exceptions.DatabaseException;
import br.com.gunthercloud.bootcamp.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;

@Service
public class ProductService {

	@Autowired
	private ProductRepository productRepository;
	
	@Transactional(readOnly = true)
	public Page<ProductDTO> findAllPaged(PageRequest pageRequest) {
		Page<Product> list = productRepository.findAll(pageRequest);
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
		Product p = productRepository.save(new Product(obj));
		return new ProductDTO(p);
	}

	@Transactional
	public ProductDTO update(Long id, ProductDTO obj) {
		try {			
			Product p = productRepository.getReferenceById(id);
			p = new Product(obj);
			p.setId(id);
			productRepository.save(p);
			return new ProductDTO(p);
		}
		catch(EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id " + id + " not found!");
		}
		catch(RuntimeException e) {
			throw new ResourceNotFoundException(e.getMessage());
		}
		
	}

	public void delete(Long id) {
		try {
			if(productRepository.findById(id).isEmpty())
				throw new ResourceNotFoundException("Id " + id + " not found!");
			productRepository.deleteById(id);
		}
		catch(DataIntegrityViolationException  e) {
			throw new DatabaseException("Product delete error.");
		}
		catch(ResourceNotFoundException  e) {
			throw new ResourceNotFoundException(e.getMessage());
		}
		catch(RuntimeException  e) {
			throw new DatabaseException(e.getMessage());
		}
		
	}

}
