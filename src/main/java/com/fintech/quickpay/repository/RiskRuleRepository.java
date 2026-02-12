package com.fintech.quickpay.repository;

import com.fintech.quickpay.entity.RiskRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RiskRuleRepository extends JpaRepository<RiskRule, Long> {
    List<RiskRule> findByEnabledTrue();

    Optional<RiskRule> findByRuleCode(String ruleCode);

    List<RiskRule> findByRuleTypeAndEnabledTrue(RiskRule.RuleType ruleType);
}
