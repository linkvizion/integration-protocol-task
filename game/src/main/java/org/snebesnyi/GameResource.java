
package org.snebesnyi;

import jakarta.ws.rs.*;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Path("/game")
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class GameResource {
    private final Client client = ClientBuilder.newClient();
    private static final String AUTH_BASE = "http://localhost:8082/auth";

    @GET
    @Path("/play")
    public Response play(@QueryParam("sessionId") String sessionId) {
        if (sessionId == null) return Response.status(401).entity(new ResponseError("missing_session")).build();
        Response response = client.target(AUTH_BASE + "/validate/" + sessionId).request(MediaType.APPLICATION_JSON).get();
        if (response.getStatus() != 200) {
            return Response.status(401).entity(new ResponseError("invalid_session")).build();
        }
        GameMessage gameMessage = response.readEntity(GameMessage.class);
        log.info("Serving game to player_id {} for game_id {}", gameMessage.player(), gameMessage.game());
        return Response
                .ok(Map.of(
                        "message", "welcome to game",
                        "player", gameMessage.player(),
                        "game", gameMessage.game()
                )).build();
    }
}
