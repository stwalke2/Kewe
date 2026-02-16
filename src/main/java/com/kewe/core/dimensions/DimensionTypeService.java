package com.kewe.core.dimensions;

import com.kewe.core.dimensions.dto.DimensionTypeRequest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class DimensionTypeService {

    public static final String STATUS_ACTIVE = "Active";
    private static final String TYPE_DIMENSION_TYPE = "DimensionType";
    private static final String SYSTEM_USER = "system";

    private final DimensionTypeRepository repository;

    public DimensionTypeService(DimensionTypeRepository repository) {
        this.repository = repository;
    }

    public List<DimensionType> getAll() {
        return repository.findAll();
    }

    public DimensionType getByCode(String code) {
        return repository.findByCode(normalizeCode(code))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dimension type not found"));
    }

    public DimensionType create(DimensionTypeRequest request) {
        DimensionType type = new DimensionType();
        apply(type, request, true);
        try {
            return repository.save(type);
        } catch (DuplicateKeyException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Dimension type code already exists");
        }
    }

    public DimensionType update(String code, DimensionTypeRequest request) {
        DimensionType existing = getByCode(code);
        if (!existing.getCode().equals(normalizeCode(request.getCode()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dimension type code cannot be changed");
        }
        apply(existing, request, false);
        return repository.save(existing);
    }

    private void apply(DimensionType type, DimensionTypeRequest request, boolean create) {
        type.setType(TYPE_DIMENSION_TYPE);
        type.setCode(normalizeCode(request.getCode()));
        type.setName(request.getName().trim());
        type.setDescription(request.getDescription());
        type.setHierarchical(Boolean.TRUE.equals(request.getHierarchical()));
        type.setMaxDepth(request.getMaxDepth());
        type.setEntryBehavior(request.getEntryBehavior());
        if (create) {
            type.setStatus(STATUS_ACTIVE);
            touchCreate(type);
        } else {
            touchUpdate(type);
        }
    }

    private String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase();
    }

    private void touchCreate(DimensionType value) {
        Instant now = Instant.now();
        value.setCreatedAt(now);
        value.setUpdatedAt(now);
        value.setCreatedBy(SYSTEM_USER);
        value.setUpdatedBy(SYSTEM_USER);
    }

    private void touchUpdate(DimensionType value) {
        value.setUpdatedAt(Instant.now());
        value.setUpdatedBy(SYSTEM_USER);
    }
}
