package dk.bugelhartmann.javalin;

import dk.bugelhartmann.token.TokenVerificationException;
import io.javalin.http.Handler;

import java.text.ParseException;

public interface ISecurityController {
    /**
     * param username credentials
     * param password credentials
     * @return the javalin Handler and a token is sent in the response
     * throws no exception but sends a 401 if the credentials are wrong
     */
    Handler login(); // to get a token
    /**
     * @purpose to register a new user with help from the SecurityDAO injected in the constructor
     * param username credentials
     * param password credentials
     * @return the javalin Handler and a token is sent in the response
     * throws no exception but sends a 401 if the credentials are wrong
     */
    Handler register(); // to get a user
    /**
     * @purpose to verify that a token was sent with the request and that it is a valid, non-expired token
     * @return the javalin Handler and the UserDTO from the token is set as an attribute in the request
     * @throws TokenVerificationException if the token is not valid
     */
    Handler authenticate(); // to verify roles inside token
    /**
     * @purpose to verify user roles
     * @return the javalin Handler and sends a 403 if the user does not have the required role
     */
    Handler authorize();
    /**
     * @purpose to verify a token and that it is not expired
     * @return the javalin Handler and sends a 401 if the token is not valid
     */
    Handler verify(); // to verify a token
    /**
     * @purpose to check how long a token is valid
     * @return the javalin Handler
     * */
    Handler timeToLive(); // to check how long a token is valid
}
