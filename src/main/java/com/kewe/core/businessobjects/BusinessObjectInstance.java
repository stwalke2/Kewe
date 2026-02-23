package com.kewe.core.businessobjects;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "business_objects")
@CompoundIndexes({
        @CompoundIndex(name = "uk_bo_type_code", def = "{'typeCode': 1, 'code': 1}", unique = true),
        @CompoundIndex(name = "idx_bo_kind_status", def = "{'objectKind': 1, 'status': 1}")
})
public class BusinessObjectInstance extends BusinessObject {
}
