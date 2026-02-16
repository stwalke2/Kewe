package com.kewe.core.dimensions;

import com.kewe.core.dimensions.dto.DimensionTypeRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dimension-types")
public class DimensionTypeController {

    private final DimensionTypeService service;

    public DimensionTypeController(DimensionTypeService service) {
        this.service = service;
    }

    @GetMapping
    public List<DimensionType> getAll() {
        return service.getAll();
    }

    @GetMapping("/{code}")
    public DimensionType getByCode(@PathVariable String code) {
        return service.getByCode(code);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DimensionType create(@Valid @RequestBody DimensionTypeRequest request) {
        return service.create(request);
    }

    @PutMapping("/{code}")
    public DimensionType update(@PathVariable String code, @Valid @RequestBody DimensionTypeRequest request) {
        return service.update(code, request);
    }
}
