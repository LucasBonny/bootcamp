package br.com.gunthercloud.bootcamp.entitites.dto;

import org.springframework.beans.BeanUtils;

import br.com.gunthercloud.bootcamp.entitites.Role;

public class RoleDTO {
	
	private Long id;
	private String authority;
	
	public RoleDTO() {
		
	}

	public RoleDTO(Long id, String authority) {
		this.id = id;
		this.authority = authority;
	}
	
	public RoleDTO(Role entity) {
		BeanUtils.copyProperties(entity, this);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}

}
