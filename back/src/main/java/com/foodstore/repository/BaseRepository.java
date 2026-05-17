package com.foodstore.repository;

import com.foodstore.exception.ResourceNotFoundException;
import com.foodstore.model.Base;
import jakarta.transaction.Transactional;
import org.springframework.core.ResolvableType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;

@NoRepositoryBean
public interface BaseRepository<E extends Base, ID extends Serializable> extends JpaRepository<E, ID> {

    @Query("SELECT e FROM #{#entityName} e WHERE e.eliminado = false")
    @Override
    List<E> findAll();

    @Query("SELECT e FROM #{#entityName} e WHERE e.eliminado = false")
    @Override
    List<E> findAll(Sort sort);

    @Query("SELECT e FROM #{#entityName} e WHERE e.eliminado = false")
    @Override
    Page<E> findAll(Pageable pageable);

    default E findByIdOrThrow(ID id) {
        return findById(id)
            .orElseThrow(() -> {
                Class<?> entityClass = ResolvableType
                    .forClass(BaseRepository.class, getClass())
                    .resolveGeneric(0);
                String entityName = entityClass != null
                    ? entityClass.getSimpleName()
                    : "Entity";
                return new ResourceNotFoundException(entityName, "id", id.toString());
            });
    }

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE #{#entityName} e SET e.eliminado = true WHERE e.id = :id")
    @Override
    void deleteById(ID id);
}
