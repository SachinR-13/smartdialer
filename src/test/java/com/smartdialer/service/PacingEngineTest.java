package com.smartdialer.service;
import org.junit.jupiter.api.Test; import static org.junit.jupiter.api.Assertions.*;
class PacingEngineTest {
 private final PacingEngine engine=new PacingEngine();
 @Test void lowAnswerRateRequestsMoreCalls(){assertEquals(50,engine.requestedCalls(10,0.2,0,0.1));}
 @Test void ringingCallsReduceNewRequests(){assertEquals(17,engine.requestedCalls(10,0.5,3,0.1));}
 @Test void noSpareSlotsMeansNoCalls(){assertEquals(0,engine.requestedCalls(0,0.5,0,0.1));}
}
