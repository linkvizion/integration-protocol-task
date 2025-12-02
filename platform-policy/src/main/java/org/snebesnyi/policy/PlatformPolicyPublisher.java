
package org.snebesnyi.policy;

import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snebesnyi.common.InMemoryKafka;
import org.snebesnyi.common.PlatformPolicy;
import org.snebesnyi.common.PlatformPolicyEvent;

import java.util.Set;

@Path("/platform")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PlatformPolicyPublisher {
    private static final Logger log = LoggerFactory.getLogger(PlatformPolicyPublisher.class);
    private final InMemoryKafka bus;

    public PlatformPolicyPublisher(InMemoryKafka bus) {
        this.bus = bus;
    }

    @PostConstruct
    void init() {
        log.info("Publishing initial platform policies");
        var pA = new PlatformPolicy("PlatformA", Set.of("Game1", "Game2"), System.currentTimeMillis());
        var pB = new PlatformPolicy("PlatformB", Set.of("Game1"), System.currentTimeMillis());
        bus.publish("platform-policy-updates", new PlatformPolicyEvent(pA));
        bus.publish("platform-policy-updates", new PlatformPolicyEvent(pB));
    }

    @POST
    @Path("/policy")
    public Response updatePolicy(PlatformPolicy oldPolicy) {
        PlatformPolicy updatedPolicy = new PlatformPolicy(oldPolicy.platformId(), oldPolicy.allowedGames(), System.currentTimeMillis());
        bus.publish("platform-policy-updates", new PlatformPolicyEvent(updatedPolicy));
        return Response.ok().build();
    }
}
