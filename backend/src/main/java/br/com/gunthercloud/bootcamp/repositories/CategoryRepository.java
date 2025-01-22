package br.com.gunthercloud.bootcamp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.gunthercloud.bootcamp.entitites.Category;

public interface CategoryRepository extends JpaRepository<Category, Long>{

}
