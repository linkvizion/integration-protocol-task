import io.jsonwebtoken.security.Keys;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.when;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.snebesnyi.common.PlatformPolicy;
import org.snebesnyi.auth.PlatformPolicyRepository;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;

@QuarkusTest
public class AuthResourceTest {
    @InjectMock
    PlatformPolicyRepository policyRepo;

    @Test
    public void testLoginMissingToken() {
        given()
        .when()
            .get("/auth/login")
        .then()
            .statusCode(401);
    }

    @Test
    public void testLoginValidToken() {
        when(policyRepo.getPolicy("PlatformA"))
                .thenReturn(new PlatformPolicy("PlatformA", Set.of("Game1"), System.currentTimeMillis()));

        String secret = "1234567910super_secret12345678910";

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .claim("platformId", "PlatformA")
                .claim("playerId", "p1")
                .claim("gameId", "Game1")
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        given()
            .redirects()
            .follow(false)
        .when()
            .get("/auth/login?token=" + token)
        .then()
            .statusCode(303);
    }
}
