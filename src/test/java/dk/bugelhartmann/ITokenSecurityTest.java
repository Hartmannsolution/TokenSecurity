package dk.bugelhartmann;

import com.nimbusds.jose.JOSEException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ITokenSecurityTest {
    ITokenSecurity instance = new TokenSecurity();
    String token;
    String secret;

    @BeforeEach
    public void setup(){
        UserDTO user = new UserDTO("username", Set.of("Admin", "User"));
        String issuer = "Thomas Hartmann";
        String expireTime = "1800000"; // 30 min in milliseconds
        secret = "u8&!pG4sDf7Lq2Xn09BaKs4V6wTpQr!y";

        try {
            token = instance.createToken(user, issuer, expireTime, secret);
            System.out.println("TOKEN: "+token);

        } catch(TokenCreationException ex){
            ex.printStackTrace();
        }
    }

    @Test
    @DisplayName("Test if we can create a JWT Token")
    void createToken() {
        UserDTO user = new UserDTO("username", Set.of("Admin", "User"));
        String issuer = "Thomas Hartmann";
        String expireTime = "1800000";
        String key = "u8&!pG4sDfBIq2Xn09BaKs4V6wTpQr!y";
        String token = null;

        try {
            token = instance.createToken(user, issuer, expireTime, key);
            System.out.println("TOKEN: "+token);

        } catch(TokenCreationException ex){
            ex.printStackTrace();
        }
        assertEquals(token.length(), 201);
    }

    @Test
    @DisplayName("Open the token and get a UserDTO with roles from the token")
    void getUserWithRolesFromToken() {
        UserDTO userFromToken = null;
        try {
            userFromToken = instance.getUserWithRolesFromToken(token);
        } catch(ParseException ex){
            ex.printStackTrace();
        }
        assertEquals(new UserDTO("username", Set.of("Admin", "User")),userFromToken);
    }

    @Test
    @DisplayName("Token is checked for whether it was signed by the server")
    void tokenIsValid() {
        try {
            assertTrue(instance.tokenIsValid(token, secret));
        } catch(ParseException | JOSEException ex){
            ex.printStackTrace();
        }
    }

    @Test
    @DisplayName("Token is checked for whether it is expired")
    void tokenNotExpired() {
        try {
            assertTrue(instance.tokenNotExpired(token));
        } catch(ParseException ex){
            ex.printStackTrace();
        }
    }

    @Test
    @DisplayName("Token is checked to see how much time is left in milliseconds")
    void timeToExpire() {
        try {
            int timeInMilliSeconds = 1800000; // 30 min on the token
            int time = instance.timeToExpire(token);
            System.out.println(time);
            assertTrue( time > timeInMilliSeconds-10000); // less than 10 seconds since token was created
        } catch(ParseException ex){
            ex.printStackTrace();
        }
    }

}