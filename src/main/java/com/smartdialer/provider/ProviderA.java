package com.smartdialer.provider;
import org.springframework.stereotype.Component; import java.util.UUID;
@Component("providerA") public class ProviderA implements TelecomProvider {
 public String name(){return "ProviderA";} public boolean healthy(){return true;}
 public ProviderStartResult initiate(String key,String phone){return new ProviderStartResult("A-"+UUID.randomUUID(),true,"accepted quickly");}
}
