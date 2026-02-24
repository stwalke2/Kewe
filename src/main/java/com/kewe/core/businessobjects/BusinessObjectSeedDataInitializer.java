package com.kewe.core.businessobjects;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;

@Configuration
public class BusinessObjectSeedDataInitializer {

    private static final String SYSTEM_USER = "seed";

    @Bean
    @ConditionalOnProperty(name = "app.seed", havingValue = "true")
    CommandLineRunner seedBusinessObjects(BusinessObjectTypeRepository typeRepository,
                                          BusinessObjectRepository objectRepository) {
        return args -> {
            if (typeRepository.count() > 0 || objectRepository.count() > 0) {
                return;
            }

            createType(typeRepository, "COST_CENTER", "Cost Center", "Responsibility");
            createType(typeRepository, "FUND", "Fund", "Funding");
            createType(typeRepository, "ENDOWMENT", "Endowment", "Funding");
            createType(typeRepository, "PROJECT", "Project", "Activity");
            createType(typeRepository, "LEDGER_ACCOUNT", "Ledger Account", "Financial");
            createType(typeRepository, "SPEND_CATEGORY", "Spend Category", "Financial");
            createType(typeRepository, "FUNCTION", "Function", "Activity/Reporting");

            createObject(objectRepository, "COST_CENTER", "BIOLOGY", "Biology", "Responsibility");
            createObject(objectRepository, "FUND", "BIO_OP", "Biology Operating", "Funding");
            createObject(objectRepository, "ENDOWMENT", "SMITH_CHAIR", "Smith Endowed Chair", "Funding");
            createObject(objectRepository, "PROJECT", "BIO_LAB_RENO", "Biology Lab Renovation", "Activity");
            createObject(objectRepository, "SPEND_CATEGORY", "PROF_DEV", "Professional Development", "Financial");
            createObject(objectRepository, "LEDGER_ACCOUNT", "EXP_PROF_DEV", "Professional Development Expense", "Financial");
            createObject(objectRepository, "FUNCTION", "INSTRUCTION", "Instruction", "Activity/Reporting");
        };
    }

    private void createType(BusinessObjectTypeRepository typeRepository, String code, String name, String objectKind) {
        if (typeRepository.findByCode(code).isPresent()) {
            return;
        }

        BusinessObjectType type = new BusinessObjectType();
        type.setType("BusinessObjectType");
        type.setCode(code);
        type.setName(name);
        type.setObjectKind(objectKind);
        type.setStatus("Active");
        touchCreate(type);
        typeRepository.save(type);
    }

    private void createObject(BusinessObjectRepository objectRepository, String typeCode, String code, String name, String objectKind) {
        if (objectRepository.findByTypeCodeAndCode(typeCode, code).isPresent()) {
            return;
        }

        BusinessObjectInstance objectValue = new BusinessObjectInstance();
        objectValue.setType("BusinessObject");
        objectValue.setTypeCode(typeCode);
        objectValue.setObjectKind(objectKind);
        objectValue.setCode(code);
        objectValue.setName(name);
        objectValue.setStatus("Active");
        touchCreate(objectValue);
        objectRepository.save(objectValue);
    }

    private void touchCreate(com.kewe.core.common.CanonicalObject value) {
        Instant now = Instant.now();
        value.setCreatedAt(now);
        value.setUpdatedAt(now);
        value.setCreatedBy(SYSTEM_USER);
        value.setUpdatedBy(SYSTEM_USER);
    }
}
