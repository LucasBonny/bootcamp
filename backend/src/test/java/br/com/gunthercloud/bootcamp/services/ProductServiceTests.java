package br.com.gunthercloud.bootcamp.services;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.gunthercloud.bootcamp.entitites.Category;
import br.com.gunthercloud.bootcamp.entitites.Product;
import br.com.gunthercloud.bootcamp.entitites.dto.ProductDTO;
import br.com.gunthercloud.bootcamp.repositories.CategoryRepository;
import br.com.gunthercloud.bootcamp.repositories.ProductRepository;
import br.com.gunthercloud.bootcamp.repositories.tests.Factory;
import br.com.gunthercloud.bootcamp.services.exceptions.DatabaseException;
import br.com.gunthercloud.bootcamp.services.exceptions.ResourceNotFoundException;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {

	@InjectMocks
	private ProductService service;
	
	@Mock
	private ProductRepository repository;
	
	@Mock
	private CategoryRepository categoryRepository;
	
	private long existingId;
	private long nonExistingId;
	private long dependentId;
	private Product product; // criando um objeto do tipo Product
	private Category category;
	private PageImpl<Product> page; // criando um objeto do tipo Page<Product>
	
	@BeforeEach
	void setUp() throws Exception{
		existingId = 1L;
		nonExistingId = 2L;
		dependentId = 3L;
		category = new Category(1L,"Electronics");
		product = Factory.createProduct(); // instanciando um objeto de produto
		page = new PageImpl<>(List.of(product)); // instanciando um Pageable com 1 objeto de product

		when(repository.findAll((Pageable)ArgumentMatchers.any())).thenReturn(page); //Quando findAll(Pageable) for chamado retornar PageImpl
		
		when(repository.findById(existingId)).thenReturn(Optional.of(product)); // Quando findById for chamado com o parametro existingId retornar um optional<ProductDTO> product
		when(repository.findById(nonExistingId)).thenReturn(Optional.empty()); // Quando findById for chamado com o parametro nonExistingId retornar um optional vazio
		
		doThrow(DatabaseException.class).when(repository).deleteById(dependentId);

		when(repository.getReferenceById(existingId)).thenReturn(product);
		
		when(categoryRepository.getReferenceById(existingId)).thenReturn(category);
		when(categoryRepository.getReferenceById(nonExistingId)).thenThrow(ResourceNotFoundException.class);
		
		when(repository.save(ArgumentMatchers.any())).thenReturn(product); // Quando eu chamar repository.save(ArgumentMatchers.any()) é pra retornar o product

		when(repository.existsById(existingId)).thenReturn(true);
		doNothing().when(repository).deleteById(existingId);
		when(repository.existsById(nonExistingId)).thenThrow(ResourceNotFoundException.class);
		when(repository.existsById(dependentId)).thenThrow(DatabaseException.class);
	}
	
	@Test
	@DisplayName("update deveria lançar ResourceNotFoundException quando o id não existir")
	public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.update(nonExistingId, Factory.createProductDTO());
		});
	}
	
	@Test
	@DisplayName("update deveria retornar um ProductDTO quando o id existir")
	public void updateShouldReturnProductDTOWhenIdExists() {
		ProductDTO obj = service.update(existingId, Factory.createProductDTO());
		
		Assertions.assertNotNull(obj);
	}

	@Test
	@DisplayName("delete deveria fazer nada quando id existe")
	public void deleteShouldDoNothingWhenIdExists() {

		
		Assertions.assertDoesNotThrow(() -> {
			service.delete(existingId);
		});
		
		verify(repository, Mockito.times(1)).deleteById(existingId);
		
	}
	@Test
	@DisplayName("delete deveria lançar ResourceNotFoundException quando id não existe")
	public void deleteShouldThrowExceptionWhenIdDoesNotExist() {

		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.delete(nonExistingId);
			verify(repository, Mockito.never()).deleteById(nonExistingId);
		});
		
	}
	
	@Test
	@DisplayName("delete deveria lançar DatabaseException quando id tem dependência")
	public void deleteShouldThrowExceptionWhenIdIsDependent() {

		Assertions.assertThrows(DatabaseException.class, () -> {
			service.delete(dependentId);
			verify(repository, Mockito.times(1)).deleteById(dependentId);
		});

	}
	
	@Test
	@DisplayName("findAllPaged deveria retornar uma lista paginada")
	public void findAllPagedShouldReturnPage() {
		
		Pageable pageable = PageRequest.of(0, 10);
		
		Page<ProductDTO> product = service.findAllPaged(pageable);

		verify(repository, times(1)).findAll(pageable);
		Assertions.assertNotNull(product);
	}
	
	@Test
	@DisplayName("findById deveria retornar um objeto quando existir o id")
	public void findByIdShouldReturnObjectWhenIdExists() {
		
		ProductDTO result = service.findById(existingId);

		verify(repository).findById(existingId);
		Assertions.assertEquals(existingId, result.getId());
		Assertions.assertEquals(product.getName(), result.getName());
			
	}
	
	@Test
	@DisplayName("findById deveria lançar uma exceção quando o id não existir")
	public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
				
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.findById(nonExistingId);	
		});
	}
	
	@Test
	@DisplayName("insert deveria inserir no banco o objeto e retornar o objeto com id")
	public void insertShouldInsertObjectAndReturnObjectWithId() {
		
		ProductDTO p = Factory.createProductDTO();

		ProductDTO result = service.insert(p);

		Assertions.assertFalse(result.getCategories().isEmpty());
		Assertions.assertNotNull(result.getId());
		Assertions.assertEquals("Phone", result.getName());
	}
	
	

}
