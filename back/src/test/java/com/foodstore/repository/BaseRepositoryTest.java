package com.foodstore.repository;

import com.foodstore.exception.ResourceNotFoundException;
import com.foodstore.model.TestBaseEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class BaseRepositoryTest {

    @Autowired
    private TestBaseRepository repository;

    @Test
    void findAll_ShouldReturnOnlyNonDeletedEntities() {
        var entity1 = TestBaseEntity.builder().name("Entity 1").build();
        var entity2 = TestBaseEntity.builder().name("Entity 2").build();
        var entity3 = TestBaseEntity.builder().name("Entity 3").build();

        repository.save(entity1);
        repository.save(entity2);
        repository.save(entity3);

        repository.deleteById(entity3.getId());

        var result = repository.findAll();
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(e -> e.getId().equals(entity1.getId())));
        assertTrue(result.stream().anyMatch(e -> e.getId().equals(entity2.getId())));
        assertFalse(result.stream().anyMatch(e -> e.getId().equals(entity3.getId())));
    }

    @Test
    void findByIdOrThrow_ShouldReturnEntityWhenExists() {
        var entity = TestBaseEntity.builder().name("Test").build();
        repository.save(entity);

        var found = repository.findByIdOrThrow(entity.getId());
        assertEquals(entity.getId(), found.getId());
        assertEquals("Test", found.getName());
    }

    @Test
    void findByIdOrThrow_ShouldThrowWhenNotFound() {
        assertThrows(ResourceNotFoundException.class, () ->
                repository.findByIdOrThrow(999L));
    }

    @Test
    void deleteById_ShouldSetEliminadoTrue() {
        var entity = TestBaseEntity.builder().name("To Delete").build();
        repository.save(entity);

        repository.deleteById(entity.getId());

        var all = repository.findAll();
        assertFalse(all.contains(entity));
        assertEquals(0, all.size());
    }

    @Test
    void findAll_WithPageable_ShouldRespectSoftDelete() {
        var entity1 = TestBaseEntity.builder().name("Entity 1").build();
        var entity2 = TestBaseEntity.builder().name("Entity 2").build();

        repository.save(entity1);
        repository.save(entity2);

        repository.deleteById(entity2.getId());

        var page = repository.findAll(PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals(1, page.getContent().size());
        assertEquals("Entity 1", page.getContent().get(0).getName());
    }
}
