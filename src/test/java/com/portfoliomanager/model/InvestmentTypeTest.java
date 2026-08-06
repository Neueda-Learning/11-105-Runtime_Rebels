package com.portfoliomanager.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvestmentTypeTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void jsonDeserialization_acceptsCommodity() throws Exception {
        InvestmentType type = mapper.readValue("\"COMMODITY\"", InvestmentType.class);
        assertEquals(InvestmentType.COMMODITY, type);
    }

    @Test
    void jsonDeserialization_acceptsCommodityAliasAndCaseVariants() throws Exception {
        InvestmentType alias = mapper.readValue("\"commodities\"", InvestmentType.class);
        InvestmentType mixedCase = mapper.readValue("\"CoMmOdItY\"", InvestmentType.class);
        assertEquals(InvestmentType.COMMODITY, alias);
        assertEquals(InvestmentType.COMMODITY, mixedCase);
    }

    @Test
    void from_throwsHelpfulMessageForInvalidValue() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> InvestmentType.from("crypto"));
        assertTrue(ex.getMessage().contains("Allowed values"));
    }
}

