package br.com.gunthercloud.bootcamp.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gunthercloud.bootcamp.entitites.Category;
import br.com.gunthercloud.bootcamp.entitites.dto.CategoryDTO;
import br.com.gunthercloud.bootcamp.repositories.CategoryRepository;
import br.com.gunthercloud.bootcamp.services.exceptions.EntityNotFoundException;

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
		return new CategoryDTO(list.orElseThrow(() -> new EntityNotFoundException("Entity not found!")));
	}
}
