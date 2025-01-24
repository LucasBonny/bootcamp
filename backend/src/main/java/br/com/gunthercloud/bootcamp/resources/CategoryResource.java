package br.com.gunthercloud.bootcamp.resources;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.gunthercloud.bootcamp.entitites.dto.CategoryDTO;
import br.com.gunthercloud.bootcamp.services.CategoryService;

@RestController
@RequestMapping(value = "/categories")
public class CategoryResource {
	
	@Autowired
	private CategoryService categoryService;
	
	@GetMapping
	public ResponseEntity<Page<CategoryDTO>> findAll(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "12") Integer linesPerPage,
			@RequestParam(defaultValue = "name") String orderBy, @RequestParam(defaultValue = "ASC") String direction) {
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		Page<CategoryDTO> list = categoryService.findAllPaged(pageRequest);
		return ResponseEntity.ok().body(list);
	}
	
	@GetMapping(value = "/{id}")
	public ResponseEntity<CategoryDTO> findById(@PathVariable Long id) {
		return ResponseEntity.ok().body(categoryService.findById(id));
	}
	
	@PostMapping
	public ResponseEntity<CategoryDTO> insert(@RequestBody CategoryDTO obj) {
		CategoryDTO cat = categoryService.insert(obj);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(cat.getId()).toUri();
		return ResponseEntity.created(uri).body(cat);
	}
	
	@PutMapping(value = "/{id}")
	public ResponseEntity<CategoryDTO> alterar(@PathVariable Long id, @RequestBody CategoryDTO obj) {
		obj = categoryService.update(id, obj);
		return ResponseEntity.status(HttpStatus.OK).body(obj);
	}
	
	@DeleteMapping(value = "{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		categoryService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
