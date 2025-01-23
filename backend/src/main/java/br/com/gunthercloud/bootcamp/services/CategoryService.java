package br.com.gunthercloud.bootcamp.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gunthercloud.bootcamp.entitites.Category;
import br.com.gunthercloud.bootcamp.entitites.dto.CategoryDTO;
import br.com.gunthercloud.bootcamp.repositories.CategoryRepository;
import br.com.gunthercloud.bootcamp.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;

@Service
public class CategoryService {

	@Autowired
	private CategoryRepository categoryRepository;
	
	@Transactional(readOnly = true)
	public List<CategoryDTO> findAll() {
		List<Category> list = categoryRepository.findAll();
		return list.stream().map(CategoryDTO::new).toList();
	}

	@Transactional(readOnly = true)
	public CategoryDTO findById(Long id) {
		Optional<Category> list = categoryRepository.findById(id);
		return new CategoryDTO(list.orElseThrow(() -> new ResourceNotFoundException("Id " + id + " not found!")));
	}

	@Transactional
	public CategoryDTO insert(CategoryDTO obj) {
		Category cat = categoryRepository.save(new Category(obj));
		return new CategoryDTO(cat);
	}

	@Transactional
	public CategoryDTO update(Long id, CategoryDTO obj) {
		try {			
			Category cat = categoryRepository.getReferenceById(id);
			cat.setName(obj.getName());
			categoryRepository.save(cat);
			return new CategoryDTO(cat);
		}
		catch(EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id " + id + " not found!");
		}
		catch(RuntimeException e) {
			throw new ResourceNotFoundException(e.getMessage());
		}
		
	}
}
