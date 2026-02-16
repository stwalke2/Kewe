package com.kewe.core.dimensions;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface DimensionTypeRepository extends MongoRepository<DimensionType, String> {
    Optional<DimensionType> findByCode(String code);
    boolean existsByCode(String code);
}
