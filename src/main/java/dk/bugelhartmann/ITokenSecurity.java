package dk.bugelhartmann;

import com.nimbusds.jose.JOSEException;

import java.text.ParseException;

public interface ITokenSecurity {
    /**
     *
     * @param token a JWT token
     * @return a UserDTO object with username and a Set<String> of roles
     * @throws ParseException
     */
    UserDTO getUserWithRolesFromToken(String token) throws ParseException;

    /**
     * @param token a JWT token
     * @param secret a 32 character long secret key
     * @return true if the token is valid
     * @throws ParseException
     * @throws TokenVerificationException
     */
    boolean tokenIsValid(String token, String secret) throws ParseException, TokenVerificationException;

    /**
     *
     * @param token a JWT token
     * @return true if the token is not expired
     * @throws ParseException
     */
    boolean tokenNotExpired(String token) throws ParseException;

    /**
     *
     * @param token a JWT token
     * @return the time to expire in milliseconds
     * @throws ParseException
     */
    int timeToExpire(String token) throws ParseException;

    /**
     *
     * @param user a UserDTO object with username and a Set<String> of roles
     * @param ISSUER name of the issuer company or person
     * @param TOKEN_EXPIRE_TIME in milliseconds
     * @param SECRET_KEY 32 characters long
     * @return a JWT token
     * @throws TokenCreationException
     */
    String createToken(UserDTO user, String ISSUER, String TOKEN_EXPIRE_TIME, String SECRET_KEY) throws TokenCreationException;

}
