
package org.snebesnyi;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.security.Key;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class AuthResource {
    private final PlatformPolicyRepository policyRepo;

    private final ConcurrentHashMap<String, PlayerSession> sessions; // In real world use separate redis POD to be able horizontally scale AuthResource if needed
    @Getter
    private final Key jwtKey;

    public AuthResource(
            @ConfigProperty(name = "jwt.secret.key") String jwtSecretKey,
            PlatformPolicyRepository policyRepo) {
        this.policyRepo = policyRepo;
        this.jwtKey = Keys.hmacShaKeyFor(jwtSecretKey.getBytes());
        this.sessions = new ConcurrentHashMap<>();
    }

    @GET
    @Path("/login")
    public Response login(@QueryParam("token") String token) {
        if (token == null) return Response.status(401).entity(new ResponseError("missing_token")).build();

        Jws<Claims> jwt;
        try {
            jwt = Jwts.parserBuilder().setSigningKey(jwtKey).build().parseClaimsJws(token);
        } catch (Exception e) {
            return Response.status(403).entity(new ResponseError("invalid_token")).build();
        }

        Claims claims = jwt.getBody();
        String platformId = claims.get("platformId", String.class);
        String playerId = claims.get("playerId", String.class);
        String gameId = claims.get("gameId", String.class);
        String currency = claims.get("currency", String.class);
        String returnUrl = claims.get("returnUrl", String.class);

        Date exp = claims.getExpiration();
        if (exp == null || exp.before(new Date())) return redirectError(returnUrl, "token_expired");

        if (platformId == null || playerId == null || gameId == null) return redirectError(returnUrl, "missing_claims");

        PlatformPolicy policy = policyRepo.getPolicy(platformId);
        if (policy == null) return redirectError(returnUrl, "platform_unknown");

        if (!policy.allowedGames().contains(gameId)) return redirectError(returnUrl, "game_not_allowed");

        String sessionId = UUID.randomUUID().toString();
        PlayerSession s = new PlayerSession(sessionId, playerId, platformId, gameId, currency);
        sessions.put(sessionId, s);
        log.info("Created session {} for player {}", sessionId, playerId);

        return Response.seeOther(URI.create("/game/play?sessionId=" + sessionId)).build();
    }

    @GET
    @Path("/validate/{sessionId}")
    public Response validate(@PathParam("sessionId") String sessionId) {
        PlayerSession playerSession = sessions.get(sessionId);
        if (playerSession == null) return Response.status(401).entity(new ResponseError("invalid_session")).build();
        return Response.
                ok(playerSession)
                .build();
    }

    private Response redirectError(String returnUrl, String code) {
        String redirect = (returnUrl != null && !returnUrl.isBlank()) ? returnUrl + "?error=" + code : "/error?error=" + code;
        return Response.seeOther(URI.create(redirect)).build();
    }
}
