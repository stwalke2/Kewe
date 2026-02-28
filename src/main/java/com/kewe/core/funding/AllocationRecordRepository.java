package com.kewe.core.funding;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AllocationRecordRepository extends MongoRepository<AllocationRecord, String> {
    List<AllocationRecord> findByBudgetPlanId(String budgetPlanId);
    List<AllocationRecord> findByAllocatedFromDimensionId(String allocatedFromDimensionId);
    List<AllocationRecord> findByAllocatedToDimensionId(String allocatedToDimensionId);
    List<AllocationRecord> findByAllocatedFromDimensionIdAndBudgetPlanId(String allocatedFromDimensionId, String budgetPlanId);
    List<AllocationRecord> findByAllocatedToDimensionIdAndBudgetPlanId(String allocatedToDimensionId, String budgetPlanId);
}
