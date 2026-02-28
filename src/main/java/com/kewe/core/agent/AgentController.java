package com.kewe.core.agent;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
public class AgentController {
    private final AgentSearchService searchService;

    public AgentController(AgentSearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping("/requisition-draft")
    public AgentSearchService.AgentDraftResponse draft(@RequestBody AgentPromptRequest request) {
        return searchService.createDraft(request.prompt());
    }

    public record AgentPromptRequest(@NotBlank String prompt) {}
}
