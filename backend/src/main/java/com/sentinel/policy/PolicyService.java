package com.sentinel.policy;

import com.sentinel.domain.Policy;
import com.sentinel.exception.ResourceNotFoundException;
import com.sentinel.persistence.PolicyMapper;
import com.sentinel.repository.PolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application service for security policies: CRUD, seeding the defaults, and supplying the active
 * rule set to the evaluation pipeline. Because policies live in the database and are read fresh on
 * each evaluation, they can be edited at runtime through the API without restarting or recompiling
 * — the separation the design calls for between policy authoring and policy enforcement.
 */
@Service
public class PolicyService {

    private final PolicyRepository repository;
    private final PolicyMapper mapper;

    public PolicyService(PolicyRepository repository, PolicyMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<Policy> findAll() {
        return repository.findAllByOrderByPriorityDesc().stream().map(mapper::toDomain).toList();
    }

    @Transactional(readOnly = true)
    public List<Policy> enabledPolicies() {
        return findAll().stream().filter(Policy::enabled).toList();
    }

    @Transactional(readOnly = true)
    public Policy findById(String id) {
        return repository.findById(id).map(mapper::toDomain)
                .orElseThrow(() -> ResourceNotFoundException.of("Policy", id));
    }

    @Transactional
    public Policy save(Policy policy) {
        return mapper.toDomain(repository.save(mapper.toEntity(policy)));
    }

    @Transactional
    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw ResourceNotFoundException.of("Policy", id);
        }
        repository.deleteById(id);
    }

    @Transactional
    public void seedDefaultsIfEmpty() {
        if (repository.count() == 0) {
            DefaultPolicies.all().forEach(policy -> repository.save(mapper.toEntity(policy)));
        }
    }

    @Transactional
    public List<Policy> resetToDefaults() {
        repository.deleteAll();
        DefaultPolicies.all().forEach(policy -> repository.save(mapper.toEntity(policy)));
        return findAll();
    }
}
