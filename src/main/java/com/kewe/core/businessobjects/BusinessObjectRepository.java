package com.kewe.core.businessobjects;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface BusinessObjectRepository extends MongoRepository<BusinessObjectInstance, String> {
    Optional<BusinessObjectInstance> findByTypeCodeAndCode(String typeCode, String code);
}
