
package org.snebesnyi.auth;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snebesnyi.common.InMemoryKafka;
import org.snebesnyi.common.PlatformPolicy;
import org.snebesnyi.common.PlatformPolicyEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class PlatformPolicyRepository {
    private final Map<String, PlatformPolicy> policies = new ConcurrentHashMap<>();

    private final InMemoryKafka bus;

    public PlatformPolicyRepository(InMemoryKafka bus) {
        this.bus = bus;
    }

    @PostConstruct
    void init() {
        bus.subscribe("platform-policy-updates", (PlatformPolicyEvent ev) -> {
            policies.put(ev.payload().platformId(), ev.payload());
        });
    }

    public PlatformPolicy getPolicy(String platformId) {
        return policies.get(platformId);
    }
}
