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

    @Bean
    @ConditionalOnProperty(name = "app.seed", havingValue = "true")
    CommandLineRunner seedBusinessObjects(BusinessObjectTypeRepository typeRepository,
                                          BusinessObjectRepository objectRepository) {
        return args -> {
            if (typeRepository.count() > 0 || objectRepository.count() > 0) {
                return;
            }

            List<TypeSeed> typeSeeds = List.of(
                    new TypeSeed("COST_CENTER", "Cost Center", "Financial"),
                    new TypeSeed("DEPARTMENT", "Department", "Organization"),
                    new TypeSeed("ASSOCIATION", "Association", "Organization"),
                    new TypeSeed("ACADEMIC_UNIT", "Academic Unit", "Organization"),
                    new TypeSeed("ENDOWMENT", "Endowment", "Funding"),
                    new TypeSeed("EXPENDABLE_GIFTS", "Expendable Gifts", "Funding"),
                    new TypeSeed("GRANT", "Grant", "Funding"),
                    new TypeSeed("COST_SHARE", "Cost Share", "Funding"),
                    new TypeSeed("APPROPRIATION", "Appropriation", "Funding"),
                    new TypeSeed("COMPANY", "Company", "Legal Entity"),
                    new TypeSeed("FUND", "Fund", "Funding"),
                    new TypeSeed("DEBT", "Debt", "Funding"),
                    new TypeSeed("PROJECT", "Project", "Activity"),
                    new TypeSeed("FUNCTION", "Function", "Reporting"),
                    new TypeSeed("PROGRAM", "Program", "Activity"),
                    new TypeSeed("EVENT", "Event", "Activity"),
                    new TypeSeed("ACTIVITY", "Activity", "Activity"),
                    new TypeSeed("LOCATION", "Location", "Organization"),
                    new TypeSeed("AGENT", "Agent", "Party"),
                    new TypeSeed("PERSON", "Person", "Party"),
                    new TypeSeed("ORGANIZATION", "Organization", "Party"),
                    new TypeSeed("OTHER_AGENT", "Other Agent", "Party")
            );

            for (TypeSeed typeSeed : typeSeeds) {
                createType(typeRepository, typeSeed.code(), typeSeed.name(), typeSeed.objectKind());
            }

            List<ObjectSeed> objectSeeds = List.of(
                    new ObjectSeed("COST_CENTER", "CC_BIOLOGY", "Biology Cost Center"),
                    new ObjectSeed("COST_CENTER", "CC_ENGINEERING", "Engineering Cost Center"),
                    new ObjectSeed("COST_CENTER", "CC_ATHLETICS", "Athletics Cost Center"),
                    new ObjectSeed("COST_CENTER", "CC_LIBRARY", "Library Cost Center"),
                    new ObjectSeed("COST_CENTER", "CC_STUDENT_AFFAIRS", "Student Affairs Cost Center"),
                    new ObjectSeed("DEPARTMENT", "DEPT_BIOLOGY", "Department of Biology"),
                    new ObjectSeed("DEPARTMENT", "DEPT_COMPUTER_SCIENCE", "Department of Computer Science"),
                    new ObjectSeed("DEPARTMENT", "DEPT_CHEMISTRY", "Department of Chemistry"),
                    new ObjectSeed("DEPARTMENT", "DEPT_HISTORY", "Department of History"),
                    new ObjectSeed("DEPARTMENT", "DEPT_ECONOMICS", "Department of Economics"),
                    new ObjectSeed("ASSOCIATION", "ASSOC_ALUMNI", "Alumni Association"),
                    new ObjectSeed("ASSOCIATION", "ASSOC_FACULTY_SENATE", "Faculty Senate Association"),
                    new ObjectSeed("ASSOCIATION", "ASSOC_PARENT_COUNCIL", "Parent Council Association"),
                    new ObjectSeed("ASSOCIATION", "ASSOC_FOUNDATION_AUX", "Foundation Auxiliary Association"),
                    new ObjectSeed("ASSOCIATION", "ASSOC_RESEARCH_CONSORTIUM", "Research Consortium Association"),
                    new ObjectSeed("ACADEMIC_UNIT", "UNIT_COLLEGE_ARTS", "College of Arts and Sciences"),
                    new ObjectSeed("ACADEMIC_UNIT", "UNIT_COLLEGE_ENGINEERING", "College of Engineering"),
                    new ObjectSeed("ACADEMIC_UNIT", "UNIT_GRADUATE_SCHOOL", "Graduate School"),
                    new ObjectSeed("ACADEMIC_UNIT", "UNIT_MEDICAL_SCHOOL", "Medical School"),
                    new ObjectSeed("ACADEMIC_UNIT", "UNIT_BUSINESS_SCHOOL", "School of Business"),
                    new ObjectSeed("ENDOWMENT", "END_SCHOLARSHIP_GENERAL", "General Scholarship Endowment"),
                    new ObjectSeed("ENDOWMENT", "END_CHAIR_BIOLOGY", "Biology Endowed Chair"),
                    new ObjectSeed("ENDOWMENT", "END_LIBRARY_COLLECTION", "Library Collection Endowment"),
                    new ObjectSeed("ENDOWMENT", "END_ATHLETICS_SUPPORT", "Athletics Support Endowment"),
                    new ObjectSeed("ENDOWMENT", "END_STEM_INITIATIVE", "STEM Initiative Endowment"),
                    new ObjectSeed("EXPENDABLE_GIFTS", "GIFT_ANNUAL_FUND", "Annual Fund Gift"),
                    new ObjectSeed("EXPENDABLE_GIFTS", "GIFT_LAB_EQUIPMENT", "Lab Equipment Gift"),
                    new ObjectSeed("EXPENDABLE_GIFTS", "GIFT_STUDENT_SUCCESS", "Student Success Gift"),
                    new ObjectSeed("EXPENDABLE_GIFTS", "GIFT_CAPITAL_RENO", "Capital Renovation Gift"),
                    new ObjectSeed("EXPENDABLE_GIFTS", "GIFT_ATHLETICS_DISCRETIONARY", "Athletics Discretionary Gift"),
                    new ObjectSeed("GRANT", "GRANT_NSF_MARINE", "NSF Marine Research Grant"),
                    new ObjectSeed("GRANT", "GRANT_NIH_GENOMICS", "NIH Genomics Grant"),
                    new ObjectSeed("GRANT", "GRANT_DOD_CYBER", "DoD Cybersecurity Grant"),
                    new ObjectSeed("GRANT", "GRANT_STATE_WORKFORCE", "State Workforce Grant"),
                    new ObjectSeed("GRANT", "GRANT_FOUNDATION_K12", "Foundation K-12 Outreach Grant"),
                    new ObjectSeed("COST_SHARE", "CS_NSF_MARINE", "Cost Share - NSF Marine"),
                    new ObjectSeed("COST_SHARE", "CS_NIH_GENOMICS", "Cost Share - NIH Genomics"),
                    new ObjectSeed("COST_SHARE", "CS_DOD_CYBER", "Cost Share - DoD Cybersecurity"),
                    new ObjectSeed("COST_SHARE", "CS_STATE_WORKFORCE", "Cost Share - State Workforce"),
                    new ObjectSeed("COST_SHARE", "CS_FOUNDATION_K12", "Cost Share - Foundation K-12"),
                    new ObjectSeed("APPROPRIATION", "APP_STATE_OPERATING", "State Operating Appropriation"),
                    new ObjectSeed("APPROPRIATION", "APP_STATE_CAPITAL", "State Capital Appropriation"),
                    new ObjectSeed("APPROPRIATION", "APP_FED_EXTENSION", "Federal Extension Appropriation"),
                    new ObjectSeed("APPROPRIATION", "APP_RESEARCH_INFRA", "Research Infrastructure Appropriation"),
                    new ObjectSeed("APPROPRIATION", "APP_STUDENT_AID", "Student Aid Appropriation"),
                    new ObjectSeed("COMPANY", "COMP_MAIN_CAMPUS", "Main Campus Company"),
                    new ObjectSeed("COMPANY", "COMP_HEALTH_SYSTEM", "Health System Company"),
                    new ObjectSeed("COMPANY", "COMP_RESEARCH_PARK", "Research Park Company"),
                    new ObjectSeed("COMPANY", "COMP_FOUNDATION", "University Foundation Company"),
                    new ObjectSeed("COMPANY", "COMP_GLOBAL_PROGRAMS", "Global Programs Company"),
                    new ObjectSeed("FUND", "FUND_UNRESTRICTED_OPERATING", "Unrestricted Operating Fund"),
                    new ObjectSeed("FUND", "FUND_RESTRICTED_SCHOLARSHIP", "Restricted Scholarship Fund"),
                    new ObjectSeed("FUND", "FUND_CAPITAL_PROJECTS", "Capital Projects Fund"),
                    new ObjectSeed("FUND", "FUND_RESEARCH_RECOVERY", "Research Recovery Fund"),
                    new ObjectSeed("FUND", "FUND_AUXILIARY_ENTERPRISE", "Auxiliary Enterprise Fund"),
                    new ObjectSeed("DEBT", "DEBT_SERIES_2020A", "Debt Series 2020A"),
                    new ObjectSeed("DEBT", "DEBT_SERIES_2022B", "Debt Series 2022B"),
                    new ObjectSeed("DEBT", "DEBT_EQUIPMENT_LEASE", "Equipment Lease Debt"),
                    new ObjectSeed("DEBT", "DEBT_STUDENT_HOUSING", "Student Housing Debt"),
                    new ObjectSeed("DEBT", "DEBT_ENERGY_UPGRADE", "Energy Upgrade Debt"),
                    new ObjectSeed("PROJECT", "PROJ_LAB_RENOVATION", "Biology Lab Renovation Project"),
                    new ObjectSeed("PROJECT", "PROJ_STUDENT_CENTER", "Student Center Modernization Project"),
                    new ObjectSeed("PROJECT", "PROJ_DATA_PLATFORM", "Research Data Platform Project"),
                    new ObjectSeed("PROJECT", "PROJ_ERP_MODERNIZATION", "ERP Modernization Project"),
                    new ObjectSeed("PROJECT", "PROJ_RESIDENCE_HALL", "Residence Hall Upgrade Project"),
                    new ObjectSeed("FUNCTION", "FUNC_INSTRUCTION", "Instruction Function"),
                    new ObjectSeed("FUNCTION", "FUNC_RESEARCH", "Research Function"),
                    new ObjectSeed("FUNCTION", "FUNC_PUBLIC_SERVICE", "Public Service Function"),
                    new ObjectSeed("FUNCTION", "FUNC_ACADEMIC_SUPPORT", "Academic Support Function"),
                    new ObjectSeed("FUNCTION", "FUNC_STUDENT_SERVICES", "Student Services Function"),
                    new ObjectSeed("PROGRAM", "PROG_HONORS", "Honors Program"),
                    new ObjectSeed("PROGRAM", "PROG_STEM_SUCCESS", "STEM Student Success Program"),
                    new ObjectSeed("PROGRAM", "PROG_GLOBAL_EXPERIENCE", "Global Experience Program"),
                    new ObjectSeed("PROGRAM", "PROG_EXEC_ED", "Executive Education Program"),
                    new ObjectSeed("PROGRAM", "PROG_UNDERGRAD_RESEARCH", "Undergraduate Research Program"),
                    new ObjectSeed("EVENT", "EVENT_COMMENCEMENT", "Commencement Event"),
                    new ObjectSeed("EVENT", "EVENT_HOMECOMING", "Homecoming Event"),
                    new ObjectSeed("EVENT", "EVENT_RESEARCH_SYMPOSIUM", "Research Symposium Event"),
                    new ObjectSeed("EVENT", "EVENT_GIVING_DAY", "Giving Day Event"),
                    new ObjectSeed("EVENT", "EVENT_ORIENTATION", "Orientation Event"),
                    new ObjectSeed("ACTIVITY", "ACT_FIELD_RESEARCH", "Field Research Activity"),
                    new ObjectSeed("ACTIVITY", "ACT_CURRICULUM_DEVELOPMENT", "Curriculum Development Activity"),
                    new ObjectSeed("ACTIVITY", "ACT_FACULTY_DEVELOPMENT", "Faculty Development Activity"),
                    new ObjectSeed("ACTIVITY", "ACT_STUDENT_RECRUITMENT", "Student Recruitment Activity"),
                    new ObjectSeed("ACTIVITY", "ACT_LAB_OPERATIONS", "Lab Operations Activity"),
                    new ObjectSeed("LOCATION", "LOC_MAIN_CAMPUS", "Main Campus"),
                    new ObjectSeed("LOCATION", "LOC_DOWNTOWN_CENTER", "Downtown Center"),
                    new ObjectSeed("LOCATION", "LOC_RESEARCH_FARM", "Research Farm"),
                    new ObjectSeed("LOCATION", "LOC_MEDICAL_CENTER", "Medical Center"),
                    new ObjectSeed("LOCATION", "LOC_ONLINE", "Online Location"),
                    new ObjectSeed("AGENT", "AGENT_SAP_CONSULTING", "SAP Consulting Agent"),
                    new ObjectSeed("AGENT", "AGENT_STRATEGIC_SOURCING", "Strategic Sourcing Agent"),
                    new ObjectSeed("AGENT", "AGENT_CAPITAL_PM", "Capital Program Agent"),
                    new ObjectSeed("AGENT", "AGENT_DONOR_RELATIONS", "Donor Relations Agent"),
                    new ObjectSeed("AGENT", "AGENT_GRANTS_ADMIN", "Grants Administration Agent"),
                    new ObjectSeed("PERSON", "PERSON_J_SMITH", "Jordan Smith"),
                    new ObjectSeed("PERSON", "PERSON_A_PATEL", "Anika Patel"),
                    new ObjectSeed("PERSON", "PERSON_R_JOHNSON", "Riley Johnson"),
                    new ObjectSeed("PERSON", "PERSON_M_GARCIA", "Morgan Garcia"),
                    new ObjectSeed("PERSON", "PERSON_T_CHEN", "Taylor Chen"),
                    new ObjectSeed("ORGANIZATION", "ORG_UNIVERSITY", "University Organization"),
                    new ObjectSeed("ORGANIZATION", "ORG_FOUNDATION", "University Foundation"),
                    new ObjectSeed("ORGANIZATION", "ORG_ALUMNI_ASSOC", "Alumni Organization"),
                    new ObjectSeed("ORGANIZATION", "ORG_RESEARCH_INST", "Research Institute Organization"),
                    new ObjectSeed("ORGANIZATION", "ORG_HEALTH_SYSTEM", "Health System Organization"),
                    new ObjectSeed("OTHER_AGENT", "OTHER_AGENT_BANK", "Banking Partner Agent"),
                    new ObjectSeed("OTHER_AGENT", "OTHER_AGENT_CATERER", "Campus Caterer Agent"),
                    new ObjectSeed("OTHER_AGENT", "OTHER_AGENT_CONTRACTOR", "General Contractor Agent"),
                    new ObjectSeed("OTHER_AGENT", "OTHER_AGENT_BOOKSTORE", "Bookstore Operator Agent"),
                    new ObjectSeed("OTHER_AGENT", "OTHER_AGENT_IT_VENDOR", "IT Vendor Agent")
            );

            for (ObjectSeed objectSeed : objectSeeds) {
                TypeSeed typeSeed = typeSeeds.stream().filter(value -> value.code().equals(objectSeed.typeCode())).findFirst().orElse(null);
                createObject(objectRepository, objectSeed.typeCode(), objectSeed.code(), objectSeed.name(), typeSeed == null ? "Seeded" : typeSeed.objectKind());
            }
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

    private record TypeSeed(String code, String name, String objectKind) {}

    private record ObjectSeed(String typeCode, String code, String name) {}
}
