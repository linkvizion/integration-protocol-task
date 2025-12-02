
package org.snebesnyi.game;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snebesnyi.common.ResponseError;

import java.util.Map;

@Path("/game")
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class GameResource {
    private static final Logger log = LoggerFactory.getLogger(GameResource.class);

    private final Client client;
    private final String authBase;

    public GameResource(@ConfigProperty(name = "auth.base") String authBase) {
        this.client = ClientBuilder.newClient();
        this.authBase = authBase;
    }

    @GET
    @Path("/play")
    public Response play(@QueryParam("sessionId") String sessionId) {
        if (sessionId == null) return Response.status(401).entity(new ResponseError("missing_session")).build();
        Response response = client.target(authBase + "/validate/" + sessionId).request(MediaType.APPLICATION_JSON).get();
        if (response.getStatus() != 200) {
            return Response.status(401).entity(new ResponseError("invalid_session")).build();
        }
        GameMessage gameMessage = response.readEntity(GameMessage.class);
        log.info("Serving game to player_id {} for game_id {}", gameMessage.playerId(), gameMessage.gameId());
        return Response
                .ok(Map.of(
                        "message", "welcome to game",
                        "player", gameMessage.playerId(),
                        "game", gameMessage.gameId()
                )).build();
    }
}
