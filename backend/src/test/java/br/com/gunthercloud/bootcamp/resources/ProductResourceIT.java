package br.com.gunthercloud.bootcamp.resources;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.gunthercloud.bootcamp.entitites.dto.ProductDTO;
import br.com.gunthercloud.bootcamp.repositories.tests.Factory;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProductResourceIT {

	@Autowired
	private MockMvc mockMvc;

	private Long existingId;
	private Long nonExistingId;
	private Long countTotalProducts;
	private ProductDTO productDTO;
	
	void setUp() throws Exception {
		existingId = 1L;
		nonExistingId = 1000L;
		countTotalProducts = 25L;
		productDTO = Factory.createProductDTO();
	}
	
	@Test
	public void findAllShouldReturnSortedPageWhenSortByName() throws Exception {
		var result = mockMvc.perform(get("/products?page=0&size=12&sort=name,asc")
			.accept(MediaType.APPLICATION_JSON));
		
			result.andExpect(status().isOk());
			result.andExpect(jsonPath("$.totalElements").value(countTotalProducts));
			result.andExpect(jsonPath("$.content").exists());
			result.andExpect(jsonPath("$.content[0]").value("Macbook Pro"));
			result.andExpect(jsonPath("$.content[1]").value("PC Gamer"));
			result.andExpect(jsonPath("$.content[2]").value("PC Gamer Alfa"));
	}

	@Test
	public void updateShouldReturnUpdatedProductDTOWhenIdExists() throws Exception {
		
		String json = new ObjectMapper().writeValueAsString(productDTO);
		String expectedName = productDTO.getName();
		
		var result = mockMvc.perform(put("/products/{id}", existingId)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json));

		result.andExpect(jsonPath("$.id").value(existingId));
		result.andExpect(jsonPath("$.name").value(expectedName));
		
	}
	
	@Test
	public void updateShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
		
		String json = new ObjectMapper().writeValueAsString(productDTO);
		
		var result = mockMvc.perform(put("/products/{id}", nonExistingId)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json));

		result.andExpect(status().isNotFound());
		
	}
	
}
