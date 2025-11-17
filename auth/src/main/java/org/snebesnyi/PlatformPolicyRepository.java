
package org.snebesnyi;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class PlatformPolicyRepository {
    private final Map<String, PlatformPolicy> policies = new ConcurrentHashMap<>();

    private final InMemoryKafka bus;

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
