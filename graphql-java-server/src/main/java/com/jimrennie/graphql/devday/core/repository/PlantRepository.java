package com.jimrennie.graphql.devday.core.repository;

import com.jimrennie.graphql.devday.core.entity.Plant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantRepository extends JpaRepository<Plant, Long> {

	List<Plant> findAllByPlantTypeIgnoreCase(String plantType);

}