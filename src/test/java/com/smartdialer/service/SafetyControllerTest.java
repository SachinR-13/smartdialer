package com.smartdialer.service;
import org.junit.jupiter.api.Test; import static org.junit.jupiter.api.Assertions.*;
class SafetyControllerTest {
 private final SafetyController safety=new SafetyController();
 @Test void neverApprovesMoreThanCapacity(){var d=safety.evaluate(20,10,2,8,true);assertEquals(6,d.approved());}
 @Test void providerOutageRejectsCallsAndRequestsFallback(){var d=safety.evaluate(10,10,0,10,false);assertEquals(0,d.approved());assertTrue(d.fallbackToProgressive());}
 @Test void noCapacityMeansNoCalls(){assertEquals(0,safety.evaluate(10,0,0,10,true).approved());}
}
