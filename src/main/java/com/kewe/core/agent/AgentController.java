package com.kewe.core.agent;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
public class AgentController {
    private final AgentSearchService searchService;

    public AgentController(AgentSearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping("/requisition-draft")
    public AgentSearchService.AgentDraftResponse draft(@RequestBody AgentPromptRequest request,
                                                       @RequestParam(required = false) String mode) {
        return searchService.createDraft(request.prompt(), "stub".equalsIgnoreCase(mode));
    }

    @GetMapping("/capabilities")
    public AgentSearchService.CapabilitiesResponse capabilities() {
        return searchService.capabilities();
    }

    @GetMapping("/ping")
    public String ping() { return "ok"; }

    public record AgentPromptRequest(@NotBlank String prompt) {}
}
