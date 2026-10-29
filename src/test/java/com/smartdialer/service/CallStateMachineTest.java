package com.smartdialer.service;
import com.smartdialer.domain.CallStatus; import org.junit.jupiter.api.Test; import static org.junit.jupiter.api.Assertions.*;
class CallStateMachineTest {
 private final CallStateMachine machine=new CallStateMachine();
 @Test void allowsNormalProgression(){assertTrue(machine.canTransition(CallStatus.RESERVED,CallStatus.INITIATED));assertTrue(machine.canTransition(CallStatus.INITIATED,CallStatus.RINGING));assertTrue(machine.canTransition(CallStatus.RINGING,CallStatus.ANSWERED));}
 @Test void terminalCallCannotReopen(){assertFalse(machine.canTransition(CallStatus.COMPLETED,CallStatus.ANSWERED));}
}
