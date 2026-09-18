package com.sentinel.repository;

import com.sentinel.domain.enums.PolicyEffect;
import com.sentinel.persistence.PolicyEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PolicyRepositoryTest {

    @Autowired
    private PolicyRepository repository;

    @Test
    void persistsAndReturnsPoliciesOrderedByPriorityDescending() {
        repository.save(policy("low", 10));
        repository.save(policy("high", 100));
        repository.save(policy("mid", 50));

        List<PolicyEntity> ordered = repository.findAllByOrderByPriorityDesc();

        assertThat(ordered).extracting(PolicyEntity::getPriority).containsExactly(100, 50, 10);
        assertThat(ordered).extracting(PolicyEntity::getId).containsExactly("high", "mid", "low");
    }

    private PolicyEntity policy(String id, int priority) {
        PolicyEntity entity = new PolicyEntity();
        entity.setId(id);
        entity.setName(id);
        entity.setPriority(priority);
        entity.setEffect(PolicyEffect.DENY);
        entity.setReason("test");
        entity.setEnabled(true);
        return entity;
    }
}
