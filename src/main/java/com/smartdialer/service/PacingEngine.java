package com.smartdialer.service;
import org.springframework.stereotype.Component;
@Component public class PacingEngine {
 public int requestedCalls(int spareAgentSlots, double answerRate, int ringingCalls, double minAnswerRate) {
   if (spareAgentSlots<=0) return 0;
   double p=Math.max(minAnswerRate, Math.min(1.0, answerRate));
   int predicted=(int)Math.floor(spareAgentSlots/p);
   return Math.max(0, predicted-ringingCalls);
 }
}
