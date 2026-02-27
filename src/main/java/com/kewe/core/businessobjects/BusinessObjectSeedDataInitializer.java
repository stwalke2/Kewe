package com.kewe.core.businessobjects;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.List;

@Configuration
public class BusinessObjectSeedDataInitializer {

    private static final String SYSTEM_USER = "seed";
    private static final String DIMENSION_KIND = "Business Dimension";

    @Bean
    @ConditionalOnProperty(name = "app.seed", havingValue = "true")
    CommandLineRunner seedBusinessObjects(BusinessObjectTypeRepository typeRepository,
                                          BusinessObjectRepository objectRepository) {
        return args -> {
            objectRepository.deleteAll();
            typeRepository.deleteAll();

            List<TypeSeed> typeSeeds = List.of(
                    new TypeSeed("COST_CENTER", "Cost Center"),
                    new TypeSeed("DEPARTMENT", "Department"),
                    new TypeSeed("ASSOCIATION", "Association"),
                    new TypeSeed("ACADEMIC_UNIT", "Academic Unit"),
                    new TypeSeed("ENDOWMENT", "Endowment"),
                    new TypeSeed("GIFT", "Gift"),
                    new TypeSeed("GRANT", "Grant"),
                    new TypeSeed("AWARD", "Award"),
                    new TypeSeed("APPROPRIATION", "Appropriation"),
                    new TypeSeed("COMPANY", "Company"),
                    new TypeSeed("FUND", "Fund"),
                    new TypeSeed("DEBT", "Debt"),
                    new TypeSeed("PROJECT", "Project"),
                    new TypeSeed("FUNCTION", "Function"),
                    new TypeSeed("PROGRAM", "Program"),
                    new TypeSeed("EVENT", "Event"),
                    new TypeSeed("ACTIVITY", "Activity"),
                    new TypeSeed("LOCATION", "Location")
            );

            for (TypeSeed typeSeed : typeSeeds) {
                createType(typeRepository, typeSeed.code(), typeSeed.name());
            }
        };
    }

    private void createType(BusinessObjectTypeRepository typeRepository, String code, String name) {
        BusinessObjectType type = new BusinessObjectType();
        type.setType("BusinessObjectType");
        type.setCode(code);
        type.setName(name);
        type.setObjectKind(DIMENSION_KIND);
        type.setStatus("Active");
        touchCreate(type);
        typeRepository.save(type);
    }

    private void touchCreate(com.kewe.core.common.CanonicalObject value) {
        Instant now = Instant.now();
        value.setCreatedAt(now);
        value.setUpdatedAt(now);
        value.setCreatedBy(SYSTEM_USER);
        value.setUpdatedBy(SYSTEM_USER);
    }

    private record TypeSeed(String code, String name) {}
}
