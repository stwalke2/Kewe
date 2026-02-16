package com.kewe.core.dimensions;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DimensionMappingRepository extends MongoRepository<DimensionMapping, String> {
    Optional<DimensionMapping> findByMappingTypeAndSourceTypeCodeAndSourceNodeIdAndSourceKey(
            MappingType mappingType,
            String sourceTypeCode,
            String sourceNodeId,
            String sourceKey
    );

    List<DimensionMapping> findByMappingType(MappingType mappingType);

    void deleteByMappingTypeAndSourceTypeCodeAndSourceNodeIdAndSourceKey(
            MappingType mappingType,
            String sourceTypeCode,
            String sourceNodeId,
            String sourceKey
    );
}
