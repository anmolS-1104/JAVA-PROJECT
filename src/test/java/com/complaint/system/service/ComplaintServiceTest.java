package com.complaint.system.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ComplaintServiceTest {

    private final ClassificationEngine engine = new ClassificationEngine();

    @Test
    void testFinanceKeyword() {
        assertEquals("Finance", engine.classify("My payment failed").getName());
    }

    @Test
    void testLogisticsKeyword() {
        assertEquals("Logistics", engine.classify("My package was not delivered").getName());
    }

    @Test
    void testTechnicalKeyword() {
        assertEquals("Technical", engine.classify("My laptop is not working").getName());
    }

    @Test
    void testHighPriority() {
        assertEquals("HIGH", engine.getPriority("This is urgent, system is broken"));
    }

    @Test
    void testNormalPriority() {
        assertEquals("NORMAL", engine.getPriority("I have a general question"));
    }

    @Test
    void testRefundGoesToFinance() {
        assertEquals("Finance", engine.classify("I need a refund for my order").getName());
    }

    @Test
    void testShipmentGoesToLogistics() {
        assertEquals("Logistics", engine.classify("My shipment is delayed by 5 days").getName());
    }

    @Test
    void testBillingGoesToFinance() {
        assertEquals("Finance", engine.classify("I was charged twice on my invoice").getName());
    }

    @Test
    void testMediumPriority() {
        assertEquals("MEDIUM", engine.getPriority("There is an error on the website"));
    }

    @Test
    void testUnknownDefaultsToTechnical() {
        assertEquals("Technical", engine.classify("I have a complaint").getName());
    }

    @Test
    void testNullInputClassify() {
        assertEquals("Technical", engine.classify(null).getName());
    }

    @Test
    void testNullInputPriority() {
        assertEquals("NORMAL", engine.getPriority(null));
    }

    @Test
    void testEmptyStringClassify() {
        assertEquals("Technical", engine.classify("").getName());
    }

    @Test
    void testWhitespaceOnlyClassify() {
        assertEquals("Technical", engine.classify("   ").getName());
    }

    @Test
    void testVeryLongInput() {
        String long_input = "payment ".repeat(1000);
        assertEquals("Finance", engine.classify(long_input).getName());
    }

    @Test
    void testSpecialCharacters() {
        assertEquals("Technical", engine.classify("!@#$%^&*()").getName());
    }

    @Test
    void testSQLInjectionInput() {
        String sql = "' OR '1'='1'; DROP TABLE complaints;--";
        assertNotNull(engine.classify(sql));
    }
}

