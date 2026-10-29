package com.smartdialer.service;
import com.smartdialer.domain.CallStatus; import java.util.*;
public final class CallStateMachine {
 private static final Map<CallStatus,Set<CallStatus>> ALLOWED=Map.of(
  CallStatus.QUEUED,Set.of(CallStatus.RESERVED,CallStatus.CANCELLED),
  CallStatus.RESERVED,Set.of(CallStatus.INITIATED,CallStatus.FAILED,CallStatus.CANCELLED),
  CallStatus.INITIATED,Set.of(CallStatus.RINGING,CallStatus.ANSWERED,CallStatus.FAILED,CallStatus.CANCELLED),
  CallStatus.RINGING,Set.of(CallStatus.ANSWERED,CallStatus.FAILED,CallStatus.CANCELLED),
  CallStatus.ANSWERED,Set.of(CallStatus.CONNECTED,CallStatus.COMPLETED,CallStatus.FAILED),
  CallStatus.CONNECTED,Set.of(CallStatus.COMPLETED,CallStatus.FAILED),
  CallStatus.COMPLETED,Set.of(),CallStatus.FAILED,Set.of(),CallStatus.CANCELLED,Set.of());
 public boolean canTransition(CallStatus from,CallStatus to){return from==to || ALLOWED.getOrDefault(from,Set.of()).contains(to);}
}
