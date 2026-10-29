package com.smartdialer.service;
import org.springframework.stereotype.Component;
@Component public class SafetyController {
 public SafetyDecision evaluate(int requested, int availableAgents, int alreadyReserved, int maxOutstanding, boolean providerHealthy) {
   if (!providerHealthy) return new SafetyDecision(requested,0,"Provider unhealthy; pause new calls",true);
   int capacity=Math.max(0,Math.min(availableAgents,maxOutstanding-alreadyReserved));
   int approved=Math.max(0,Math.min(requested,capacity));
   String reason=approved==requested?"Approved within hard capacity":(approved==0?"Rejected: no safe capacity":"Reduced to hard capacity");
   return new SafetyDecision(requested,approved,reason,false);
 }
}
