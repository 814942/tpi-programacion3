package com.foodstore.repository;

import com.foodstore.model.TestBaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestBaseRepository extends BaseRepository<TestBaseEntity, Long> {
}
