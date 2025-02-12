package br.com.gunthercloud.bootcamp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.gunthercloud.bootcamp.entitites.Role;

public interface RoleRepository extends JpaRepository<Role, Long>{

}
