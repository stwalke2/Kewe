package com.kewe.core.dimensions;

import com.kewe.core.dimensions.dto.DimensionNodeRequest;
import com.kewe.core.dimensions.dto.MoveNodeRequest;
import com.kewe.core.dimensions.dto.ReorderRequest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DimensionNodeService {

    private static final String STATUS_ACTIVE = "Active";
    private static final String STATUS_INACTIVE = "Inactive";
    private static final String TYPE_DIMENSION_NODE = "DimensionNode";
    private static final String SYSTEM_USER = "system";

    private final DimensionNodeRepository nodeRepository;
    private final DimensionTypeService dimensionTypeService;

    public DimensionNodeService(DimensionNodeRepository nodeRepository, DimensionTypeService dimensionTypeService) {
        this.nodeRepository = nodeRepository;
        this.dimensionTypeService = dimensionTypeService;
    }

    public DimensionNode createNode(String typeCode, DimensionNodeRequest request) {
        DimensionType type = dimensionTypeService.getByCode(typeCode);
        DimensionNode parent = resolveParent(type.getCode(), request.getParentId());
        int depth = parent == null ? 0 : parent.getDepth() + 1;
        validateDepth(depth, type);

        DimensionNode node = new DimensionNode();
        node.setType(TYPE_DIMENSION_NODE);
        node.setStatus(STATUS_ACTIVE);
        node.setTypeCode(type.getCode());
        node.setCode(normalizeCode(request.getCode()));
        node.setName(request.getName().trim());
        node.setDescription(request.getDescription());
        node.setParentId(parent == null ? null : parent.getId());
        node.setDepth(depth);
        node.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : nextSortOrder(type.getCode(), node.getParentId()));
        node.setAttributes(request.getAttributes() == null ? new HashMap<>() : request.getAttributes());
        touchCreate(node);

        node = saveWithDuplicateHandling(node);
        node.setPath(buildPath(node.getTypeCode(), node.getParentId(), node.getId()));
        return nodeRepository.save(node);
    }

    public DimensionNode updateNode(String typeCode, String nodeId, DimensionNodeRequest request) {
        DimensionType type = dimensionTypeService.getByCode(typeCode);
        DimensionNode node = getNode(typeCode, nodeId);
        String code = normalizeCode(request.getCode());
        if (!Objects.equals(code, node.getCode())
                && nodeRepository.existsByTypeCodeAndCode(node.getTypeCode(), code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Node code already exists in this dimension type");
        }

        DimensionNode newParent = resolveParent(type.getCode(), request.getParentId());
        if (newParent != null && isDescendant(node, newParent)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Move would create cycle");
        }

        String oldPath = node.getPath();
        int oldDepth = node.getDepth();
        int newDepth = newParent == null ? 0 : newParent.getDepth() + 1;
        validateDepth(newDepth, type);

        node.setCode(code);
        node.setName(request.getName().trim());
        node.setDescription(request.getDescription());
        node.setAttributes(request.getAttributes() == null ? new HashMap<>() : request.getAttributes());
        if (request.getSortOrder() != null) {
            node.setSortOrder(request.getSortOrder());
        }
        node.setParentId(newParent == null ? null : newParent.getId());
        node.setDepth(newDepth);
        node.setPath(buildPath(node.getTypeCode(), node.getParentId(), node.getId()));
        touchUpdate(node);
        nodeRepository.save(node);

        if (!Objects.equals(oldPath, node.getPath()) || oldDepth != newDepth) {
            int depthDelta = newDepth - oldDepth;
            List<DimensionNode> descendants = nodeRepository.findByTypeCodeAndPathStartingWith(node.getTypeCode(), oldPath + "/");
            for (DimensionNode descendant : descendants) {
                String suffix = descendant.getPath().substring(oldPath.length());
                descendant.setPath(node.getPath() + suffix);
                descendant.setDepth(descendant.getDepth() + depthDelta);
                validateDepth(descendant.getDepth(), type);
                touchUpdate(descendant);
            }
            if (!descendants.isEmpty()) {
                nodeRepository.saveAll(descendants);
            }
        }

        return node;
    }

    public List<DimensionNode> getNodes(String typeCode, boolean includeInactive) {
        String normalizedType = typeCode.trim().toUpperCase();
        dimensionTypeService.getByCode(normalizedType);
        if (includeInactive) {
            return nodeRepository.findByTypeCodeOrderByPathAscSortOrderAsc(normalizedType);
        }
        return nodeRepository.findByTypeCodeAndStatusOrderByPathAscSortOrderAsc(normalizedType, STATUS_ACTIVE);
    }

    public List<DimensionNode> getTree(String typeCode, boolean includeInactive) {
        return getNodes(typeCode, includeInactive);
    }

    public List<DimensionNode> search(String typeCode, String query, boolean includeInactive) {
        String q = query == null ? "" : query.trim().toLowerCase();
        return getNodes(typeCode, includeInactive).stream()
                .filter(node -> q.isBlank() || node.getCode().toLowerCase().contains(q) || node.getName().toLowerCase().contains(q))
                .toList();
    }

    public void deleteNode(String typeCode, String nodeId) {
        DimensionNode node = getNode(typeCode, nodeId);
        if (nodeRepository.existsByTypeCodeAndParentId(node.getTypeCode(), node.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete node with children");
        }
        nodeRepository.deleteByTypeCodeAndId(node.getTypeCode(), node.getId());
    }

    public DimensionNode setStatus(String typeCode, String nodeId, String status) {
        DimensionNode node = getNode(typeCode, nodeId);
        if (!STATUS_ACTIVE.equalsIgnoreCase(status) && !STATUS_INACTIVE.equalsIgnoreCase(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be Active or Inactive");
        }
        node.setStatus(Character.toUpperCase(status.charAt(0)) + status.substring(1).toLowerCase());
        touchUpdate(node);
        return nodeRepository.save(node);
    }

    public List<DimensionNode> move(String typeCode, MoveNodeRequest request) {
        DimensionType type = dimensionTypeService.getByCode(typeCode);
        DimensionNode node = getNode(typeCode, request.getNodeId());
        DimensionNode newParent = resolveParent(type.getCode(), request.getNewParentId());

        if (newParent != null && isDescendant(node, newParent)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Move would create cycle");
        }
        int newDepth = newParent == null ? 0 : newParent.getDepth() + 1;
        validateDepth(newDepth, type);
        int depthDelta = newDepth - node.getDepth();

        String oldPath = node.getPath();
        node.setParentId(newParent == null ? null : newParent.getId());
        node.setDepth(newDepth);
        node.setPath(buildPath(node.getTypeCode(), node.getParentId(), node.getId()));
        node.setSortOrder(nextSortOrder(node.getTypeCode(), node.getParentId()));
        touchUpdate(node);
        nodeRepository.save(node);

        List<DimensionNode> descendants = nodeRepository.findByTypeCodeAndPathStartingWith(node.getTypeCode(), oldPath + "/");
        for (DimensionNode descendant : descendants) {
            String suffix = descendant.getPath().substring(oldPath.length());
            descendant.setPath(node.getPath() + suffix);
            descendant.setDepth(descendant.getDepth() + depthDelta);
            validateDepth(descendant.getDepth(), type);
            touchUpdate(descendant);
        }
        if (!descendants.isEmpty()) {
            nodeRepository.saveAll(descendants);
        }
        return getTree(typeCode, true);
    }

    public List<DimensionNode> reorder(String typeCode, ReorderRequest request) {
        String normalizedType = typeCode.trim().toUpperCase();
        dimensionTypeService.getByCode(normalizedType);
        List<DimensionNode> siblings = StringUtils.hasText(request.getParentId())
                ? nodeRepository.findByTypeCodeAndParentIdOrderBySortOrderAsc(normalizedType, request.getParentId())
                : nodeRepository.findByTypeCodeAndParentIdIsNullOrderBySortOrderAsc(normalizedType);

        Set<String> siblingIds = siblings.stream().map(DimensionNode::getId).collect(Collectors.toSet());
        if (!siblingIds.equals(Set.copyOf(request.getNodeIds()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reorder list must contain exactly current sibling node ids");
        }

        Map<String, Integer> sortOrderById = new HashMap<>();
        for (int i = 0; i < request.getNodeIds().size(); i++) {
            sortOrderById.put(request.getNodeIds().get(i), i + 1);
        }

        List<DimensionNode> reordered = new ArrayList<>(siblings);
        for (DimensionNode node : reordered) {
            node.setSortOrder(sortOrderById.get(node.getId()));
            touchUpdate(node);
        }
        nodeRepository.saveAll(reordered);
        return reordered.stream().sorted(Comparator.comparing(DimensionNode::getSortOrder)).toList();
    }

    public DimensionNode getNode(String typeCode, String nodeId) {
        String normalizedType = typeCode.trim().toUpperCase();
        dimensionTypeService.getByCode(normalizedType);
        DimensionNode node = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dimension node not found"));
        if (!normalizedType.equals(node.getTypeCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Node does not belong to requested type");
        }
        return node;
    }

    private DimensionNode resolveParent(String typeCode, String parentId) {
        if (!StringUtils.hasText(parentId)) {
            return null;
        }
        DimensionNode parent = nodeRepository.findById(parentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent node not found"));
        if (!typeCode.equals(parent.getTypeCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent node must be same type");
        }
        return parent;
    }

    private boolean isDescendant(DimensionNode source, DimensionNode candidateParent) {
        return candidateParent.getPath().startsWith(source.getPath() + "/");
    }

    private String buildPath(String typeCode, String parentId, String id) {
        if (!StringUtils.hasText(parentId)) {
            return id;
        }
        DimensionNode parent = getNode(typeCode, parentId);
        return parent.getPath() + "/" + id;
    }

    private int nextSortOrder(String typeCode, String parentId) {
        List<DimensionNode> siblings = StringUtils.hasText(parentId)
                ? nodeRepository.findByTypeCodeAndParentIdOrderBySortOrderAsc(typeCode, parentId)
                : nodeRepository.findByTypeCodeAndParentIdIsNullOrderBySortOrderAsc(typeCode);
        return siblings.stream().map(DimensionNode::getSortOrder).filter(Objects::nonNull).max(Integer::compareTo).orElse(0) + 1;
    }

    private void validateDepth(int depth, DimensionType type) {
        if (depth > type.getMaxDepth()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Depth exceeds maxDepth for dimension type");
        }
    }

    private DimensionNode saveWithDuplicateHandling(DimensionNode node) {
        try {
            return nodeRepository.save(node);
        } catch (DuplicateKeyException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Node code already exists in this dimension type");
        }
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase();
    }

    private void touchCreate(DimensionNode value) {
        Instant now = Instant.now();
        value.setCreatedAt(now);
        value.setUpdatedAt(now);
        value.setCreatedBy(SYSTEM_USER);
        value.setUpdatedBy(SYSTEM_USER);
    }

    private void touchUpdate(DimensionNode value) {
        value.setUpdatedAt(Instant.now());
        value.setUpdatedBy(SYSTEM_USER);
    }
}
