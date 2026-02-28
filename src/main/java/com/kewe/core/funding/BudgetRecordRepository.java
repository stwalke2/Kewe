package com.kewe.core.funding;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRecordRepository extends MongoRepository<BudgetRecord, String> {
    List<BudgetRecord> findByBusinessDimensionId(String businessDimensionId);
    List<BudgetRecord> findByBudgetPlanId(String budgetPlanId);
    List<BudgetRecord> findByBusinessDimensionIdAndBudgetPlanId(String businessDimensionId, String budgetPlanId);
    Optional<BudgetRecord> findFirstByBusinessDimensionIdAndBudgetPlanId(String businessDimensionId, String budgetPlanId);
}
