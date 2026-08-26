package edu.eci.arsw.blueprints.persistence;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.BlueprintPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlueprintJpaRepository extends JpaRepository<Blueprint, BlueprintPK> {
    List<Blueprint> findByAuthor(String author);
}