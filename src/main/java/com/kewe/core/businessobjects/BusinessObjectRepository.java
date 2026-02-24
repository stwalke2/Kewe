package com.kewe.core.businessobjects;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessObjectRepository extends MongoRepository<BusinessObjectInstance, String> {
    Optional<BusinessObjectInstance> findByTypeCodeAndCode(String typeCode, String code);
    List<BusinessObjectInstance> findByTypeCode(String typeCode);
}
