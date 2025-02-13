package br.com.gunthercloud.bootcamp.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gunthercloud.bootcamp.entitites.User;
import br.com.gunthercloud.bootcamp.entitites.dto.UserDTO;
import br.com.gunthercloud.bootcamp.entitites.dto.UserInsertDTO;
import br.com.gunthercloud.bootcamp.repositories.RoleRepository;
import br.com.gunthercloud.bootcamp.repositories.UserRepository;
import br.com.gunthercloud.bootcamp.services.exceptions.DatabaseException;
import br.com.gunthercloud.bootcamp.services.exceptions.ResourceNotFoundException;

@Service
public class UserService {

	@Autowired
	private UserRepository repository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Transactional(readOnly = true)
	public Page<UserDTO> findAllPaged(Pageable pageable) {
		Page<User> obj = repository.findAll(pageable);
		return obj.map(x -> new UserDTO(x));
	}

	@Transactional(readOnly = true)
	public UserDTO findById(Long id) {
		Optional<User> user = repository.findById(id);
		User entity = user.orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
		return new UserDTO(entity);
	}
	
	@Transactional
	public UserDTO insert(UserInsertDTO dto) {
		dto.getRoles().forEach(x -> x.setAuthority(roleRepository.findById(x.getId()).get().getAuthority()));
		if(dto.getId() != null)
			dto.setId(null);
		User entity = new User(dto);
		entity.setPassword(passwordEncoder.encode(dto.getPassword()));
		entity = repository.save(entity);
		return new UserDTO(entity);
	}
	
	@Transactional
	public UserDTO update(Long id, UserDTO dto) {
		dto.getRoles().forEach(x -> x.setAuthority(roleRepository.findById(x.getId()).get().getAuthority()));
		if(repository.findById(id).isEmpty())
			throw new ResourceNotFoundException("Entity not found " + id);
		if(dto.getId() != null)
			dto.setId(null);
		dto.setId(id);
		User entity = repository.save(new User(dto));
		return new UserDTO(entity);
	}

	@Transactional
	public void delete(Long id) {
		try{
			if (repository.findById(id).isEmpty()) {
				throw new ResourceNotFoundException("Entity not found " + id);
			}
			repository.deleteById(id);
		}
		catch(ResourceNotFoundException e) {
			throw new ResourceNotFoundException("Entity not found " + id);
		}
		catch(DataIntegrityViolationException e) {
			throw new DatabaseException("Integrity violation");
		}
	}
}
