package com.smartdialer.provider;
import org.springframework.stereotype.Component; import java.util.UUID; import java.util.concurrent.atomic.AtomicInteger;
@Component("providerB") public class ProviderB implements TelecomProvider {
 private final AtomicInteger attempts=new AtomicInteger();
 public String name(){return "ProviderB";} public boolean healthy(){return attempts.get()%5!=4;}
 public ProviderStartResult initiate(String key,String phone){int n=attempts.incrementAndGet(); if(n%5==0) return new ProviderStartResult(null,false,"simulated timeout/failure"); return new ProviderStartResult("B-"+UUID.randomUUID(),true,"accepted with simulated latency");}
}
