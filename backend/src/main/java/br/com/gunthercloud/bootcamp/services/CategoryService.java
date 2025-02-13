package br.com.gunthercloud.bootcamp.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gunthercloud.bootcamp.entitites.Category;
import br.com.gunthercloud.bootcamp.entitites.dto.CategoryDTO;
import br.com.gunthercloud.bootcamp.repositories.CategoryRepository;
import br.com.gunthercloud.bootcamp.services.exceptions.DatabaseException;
import br.com.gunthercloud.bootcamp.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;

@Service
public class CategoryService {

	@Autowired
	private CategoryRepository categoryRepository;
	
	@Transactional(readOnly = true)
	public Page<CategoryDTO> findAllPaged(Pageable pageable) {
		Page<Category> list = categoryRepository.findAll(pageable);
		return list.map(CategoryDTO::new);
	}

	@Transactional(readOnly = true)
	public CategoryDTO findById(Long id) {
		Optional<Category> list = categoryRepository.findById(id);
		return new CategoryDTO(list.orElseThrow(() -> new ResourceNotFoundException("Id " + id + " not found!")));
	}

	@Transactional
	public CategoryDTO insert(CategoryDTO obj) {
		if(obj.getId() != null)
			obj.setId(null);
		Category c = categoryRepository.save(new Category(obj));
		return new CategoryDTO(c);
	}

	@Transactional
	public CategoryDTO update(Long id, CategoryDTO obj) {
		try {			
			Category c = categoryRepository.getReferenceById(id);
			c = new Category(obj);
			if(obj.getId() != null)
				obj.setId(null);
			c.setId(id);
			categoryRepository.save(c);
			return new CategoryDTO(c);
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
			if(categoryRepository.findById(id).isEmpty())
				throw new ResourceNotFoundException("Id " + id + " not found!");
			categoryRepository.deleteById(id);
		}
		catch(DataIntegrityViolationException  e) {
			throw new DatabaseException("Category delete error.");
		}
		catch(ResourceNotFoundException  e) {
			throw new ResourceNotFoundException(e.getMessage());
		}
		catch(RuntimeException  e) {
			throw new DatabaseException(e.getMessage());
		}
		
	}

}
