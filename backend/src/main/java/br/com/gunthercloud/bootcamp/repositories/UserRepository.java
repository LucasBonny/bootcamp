package br.com.gunthercloud.bootcamp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.gunthercloud.bootcamp.entitites.User;

public interface UserRepository extends JpaRepository<User, Long>{

}
