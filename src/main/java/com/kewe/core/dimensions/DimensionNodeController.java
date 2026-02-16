package com.kewe.core.dimensions;

import com.kewe.core.dimensions.dto.DimensionNodeRequest;
import com.kewe.core.dimensions.dto.MoveNodeRequest;
import com.kewe.core.dimensions.dto.NodeStatusRequest;
import com.kewe.core.dimensions.dto.ReorderRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dimensions/{typeCode}")
public class DimensionNodeController {

    private final DimensionNodeService service;

    public DimensionNodeController(DimensionNodeService service) {
        this.service = service;
    }

    @GetMapping("/nodes")
    public List<DimensionNode> getNodes(@PathVariable String typeCode,
                                        @RequestParam(defaultValue = "false") boolean includeInactive) {
        return service.getNodes(typeCode, includeInactive);
    }

    @PostMapping("/nodes")
    @ResponseStatus(HttpStatus.CREATED)
    public DimensionNode createNode(@PathVariable String typeCode, @Valid @RequestBody DimensionNodeRequest request) {
        return service.createNode(typeCode, request);
    }

    @PutMapping("/nodes/{nodeId}")
    public DimensionNode updateNode(@PathVariable String typeCode,
                                    @PathVariable String nodeId,
                                    @Valid @RequestBody DimensionNodeRequest request) {
        return service.updateNode(typeCode, nodeId, request);
    }

    @DeleteMapping("/nodes/{nodeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNode(@PathVariable String typeCode, @PathVariable String nodeId) {
        service.deleteNode(typeCode, nodeId);
    }

    @PatchMapping("/nodes/{nodeId}/status")
    public DimensionNode setStatus(@PathVariable String typeCode,
                                   @PathVariable String nodeId,
                                   @Valid @RequestBody NodeStatusRequest request) {
        return service.setStatus(typeCode, nodeId, request.getStatus());
    }

    @GetMapping("/tree")
    public List<DimensionNode> getTree(@PathVariable String typeCode,
                                       @RequestParam(defaultValue = "false") boolean includeInactive) {
        return service.getTree(typeCode, includeInactive);
    }

    @PostMapping("/move")
    public List<DimensionNode> move(@PathVariable String typeCode, @RequestBody MoveNodeRequest request) {
        return service.move(typeCode, request);
    }

    @PostMapping("/reorder")
    public List<DimensionNode> reorder(@PathVariable String typeCode, @RequestBody ReorderRequest request) {
        return service.reorder(typeCode, request);
    }

    @GetMapping("/search")
    public List<DimensionNode> search(@PathVariable String typeCode,
                                      @RequestParam("q") String query,
                                      @RequestParam(defaultValue = "false") boolean includeInactive) {
        return service.search(typeCode, query, includeInactive);
    }
}
