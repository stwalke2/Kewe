package com.kewe.core.requisition;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface RequisitionDraftRepository extends MongoRepository<RequisitionDraft, String> {
}
