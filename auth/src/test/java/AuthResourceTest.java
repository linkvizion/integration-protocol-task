import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.snebesnyi.AuthResource;
import org.snebesnyi.InMemoryKafka;
import org.snebesnyi.PlatformPolicyRepository;

import java.util.Date;

@QuarkusTest
public class AuthResourceTest {
    @Test
    public void testLoginMissingToken() {
        given().when().get("/auth/login").then().statusCode(401);
    }

    @Test
    public void testLoginValidToken() {
        AuthResource auth = new AuthResource(new PlatformPolicyRepository(new InMemoryKafka()));
        String token = Jwts.builder()
            .claim("platformId","PlatformA")
            .claim("playerId","p1")
            .claim("gameId","Game1")
            .setExpiration(new Date(System.currentTimeMillis()+60000))
            .signWith(auth.getJwtKey())
            .compact();
        given().when().get("/auth/login?token="+token).then().statusCode(302);
    }
}
