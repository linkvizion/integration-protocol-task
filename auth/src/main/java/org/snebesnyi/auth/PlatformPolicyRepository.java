
package org.snebesnyi.auth;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.snebesnyi.common.InMemoryKafka;
import org.snebesnyi.common.PlatformPolicy;
import org.snebesnyi.common.PlatformPolicyEvent;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Set;
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
        populateTestData();

        bus.subscribe("platform-policy-updates", (PlatformPolicyEvent ev) -> {
            policies.put(ev.payload().platformId(), ev.payload());
        });
    }

    public PlatformPolicy getPolicy(String platformId) {
        return policies.get(platformId);
    }

    private void populateTestData() {
        policies.put("test_platform_id", new PlatformPolicy("test_platform_id", Set.of("test_game_id"), LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)));
    }
}
