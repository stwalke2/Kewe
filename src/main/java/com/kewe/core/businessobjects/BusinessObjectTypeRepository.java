package com.kewe.core.businessobjects;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface BusinessObjectTypeRepository extends MongoRepository<BusinessObjectType, String> {
    Optional<BusinessObjectType> findByCode(String code);
}
