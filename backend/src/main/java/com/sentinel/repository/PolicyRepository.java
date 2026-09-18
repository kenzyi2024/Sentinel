package com.sentinel.repository;

import com.sentinel.persistence.PolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data repository for security policies.
 */
public interface PolicyRepository extends JpaRepository<PolicyEntity, String> {

    List<PolicyEntity> findAllByOrderByPriorityDesc();
}
