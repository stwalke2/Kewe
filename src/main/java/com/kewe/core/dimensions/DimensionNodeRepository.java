package com.kewe.core.dimensions;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DimensionNodeRepository extends MongoRepository<DimensionNode, String> {
    Optional<DimensionNode> findByTypeCodeAndCode(String typeCode, String code);
    boolean existsByTypeCodeAndCode(String typeCode, String code);
    List<DimensionNode> findByTypeCodeOrderByPathAscSortOrderAsc(String typeCode);
    List<DimensionNode> findByTypeCodeAndParentIdOrderBySortOrderAsc(String typeCode, String parentId);
    List<DimensionNode> findByTypeCodeAndParentIdIsNullOrderBySortOrderAsc(String typeCode);
    boolean existsByTypeCodeAndParentId(String typeCode, String parentId);
    List<DimensionNode> findByTypeCodeAndPathStartingWith(String typeCode, String pathPrefix);
    List<DimensionNode> findByTypeCodeAndIdIn(String typeCode, Collection<String> ids);
    List<DimensionNode> findByTypeCodeAndStatusOrderByPathAscSortOrderAsc(String typeCode, String status);
    void deleteByTypeCodeAndId(String typeCode, String id);
}
