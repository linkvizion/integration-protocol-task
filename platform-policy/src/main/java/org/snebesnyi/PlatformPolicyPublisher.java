
package org.snebesnyi;

import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Path("/platform")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
@RequiredArgsConstructor
public class PlatformPolicyPublisher {
    private final InMemoryKafka bus;

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
