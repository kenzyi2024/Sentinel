package com.sentinel.api;

import com.sentinel.api.dto.PolicyRequest;
import com.sentinel.domain.Policy;
import com.sentinel.policy.PolicyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * CRUD API for security policies. Because the engine reads policies from the database on every
 * evaluation, changes made here take effect on the next run with no restart — demonstrating that
 * policy is configuration, not code.
 */
@RestController
@RequestMapping("/api/policies")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @GetMapping
    public List<Policy> list() {
        return policyService.findAll();
    }

    @GetMapping("/{id}")
    public Policy get(@PathVariable String id) {
        return policyService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Policy create(@Valid @RequestBody PolicyRequest request) {
        return policyService.save(toPolicy(newId(request.name()), request));
    }

    @PutMapping("/{id}")
    public Policy update(@PathVariable String id, @Valid @RequestBody PolicyRequest request) {
        policyService.findById(id); // 404 if it does not exist
        return policyService.save(toPolicy(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        policyService.delete(id);
    }

    /** Restore the built-in default policy set. */
    @PostMapping("/reset")
    public List<Policy> reset() {
        return policyService.resetToDefaults();
    }

    private Policy toPolicy(String id, PolicyRequest request) {
        return new Policy(id, request.name(), request.priority(), request.archetypes(), request.actionTypes(),
                request.resourcePattern(), request.minSensitivity(), request.requiredCapability(),
                request.effect(), request.reason(), request.enabledOrDefault());
    }

    private String newId(String name) {
        String slug = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        return "policy-" + slug + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
