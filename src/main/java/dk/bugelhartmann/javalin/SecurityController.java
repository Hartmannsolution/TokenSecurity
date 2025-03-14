package dk.bugelhartmann.javalin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.bugelhartmann.*;
import dk.bugelhartmann.token.ITokenSecurity;
import dk.bugelhartmann.token.TokenSecurity;
import dk.bugelhartmann.token.TokenVerificationException;
import io.javalin.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Purpose: To handle security in the API
 * Author: Thomas Hartmann
 */
public class SecurityController implements ISecurityController {
    private static final Logger logger = LoggerFactory.getLogger(SecurityController.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ITokenSecurity tokenSecurity = new TokenSecurity();
    private final ISecurityDAO securityDAO;
    private String issuer;
    private int expireTimeInMillis;
    private String secretKey;

    public SecurityController(ISecurityDAO securityDAO, String issuer, int expireTimeInMillis, String secretKey) {
        this.securityDAO = securityDAO;
        this.issuer = System.getenv("DEPLOYED") != null ? System.getenv("ISSUER") : issuer;
        this.expireTimeInMillis = System.getenv("DEPLOYED") != null ? Integer.parseInt(System.getenv("TOKEN_EXPIRE_TIME")) : expireTimeInMillis;
        this.secretKey = System.getenv("DEPLOYED") != null ? System.getenv("SECRET_KEY") : secretKey;
    }

    @Override
    public Handler login() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode(); // for sending json messages back to the client
            UserDTO user = null;
            try {
                user = ctx.bodyAsClass(UserDTO.class);
                UserDTO verifiedUser = securityDAO.getVerifiedUser(user.getUsername(), user.getPassword());
                String token = createToken(verifiedUser);

                ctx.status(200).json(returnObject
                        .put("token", token)
                        .put("username", verifiedUser.getUsername()));

            } catch (Exception ex) { // securityDAO might throw an EntityNotFoundException or validationException
                logger.error(SecurityMessages.UNAUTHORIZED_LOGIN+": "+user.getUsername(), ex);
                sendErrorResponse(ctx, HttpStatus.UNAUTHORIZED, SecurityMessages.UNAUTHORIZED_LOGIN);
            }
        };
    }

    @Override
    public Handler register() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                UserDTO userInput = ctx.bodyAsClass(UserDTO.class);
                System.out.println("User input: " + userInput);

                UserDTO created = securityDAO.createUser(userInput.getUsername(), userInput.getPassword());
                System.out.println("Created user::::: " + created);
                String token = createToken(created);
                ctx.status(HttpStatus.CREATED).json(returnObject
                        .put("token", token)
                        .put("username", created.getUsername()));
            } catch (Exception ex) {
                logger.error(SecurityMessages.USER_EXISTS, ex);
                sendErrorResponse(ctx, HttpStatus.BAD_REQUEST, SecurityMessages.USER_EXISTS);
            }
        };
    }

    @Override
    public Handler authenticate() {
        ObjectNode returnObject = objectMapper.createObjectNode();

        return (ctx) -> {
            // This is a preflight request => no need for authentication
            if (ctx.method().toString().equals("OPTIONS")) {
                ctx.status(200);
                return;
            }

            // If the endpoint is not protected with roles or is open to ANYONE role, then skip
            Set<String> allowedRoles = ctx.routeRoles().stream().map(role -> role.toString().toUpperCase()).collect(Collectors.toSet());
            if (isOpenEndpoint(allowedRoles))
                return;

            String token = extractToken(ctx);
            UserDTO verifiedTokenUser = verifyToken(token);
            if (verifiedTokenUser == null)
                throw new UnauthorizedResponse(SecurityMessages.INVALID_TOKEN);
            ctx.attribute("user", verifiedTokenUser); // -> ctx.attribute("user") in ApplicationConfig beforeMatched filter
        };
    }

    @Override
    public Handler authorize() {
        ObjectNode returnObject = objectMapper.createObjectNode();

        return (ctx) -> {
            Set<String> allowedRoles = ctx.routeRoles()
                    .stream()
                    .map(role -> role.toString().toUpperCase())
                    .collect(Collectors.toSet());

            // 1. Check if the endpoint is open to all (either by not having any roles or having the ANYONE role set
            if (isOpenEndpoint(allowedRoles))
                return;
            // 2. Get user and ensure it is not null
            UserDTO user = ctx.attribute("user");
            if (user == null) {
                throw new ForbiddenResponse("No user was added from the token");
//                throw new dk.cphbusiness.exceptions.ApiException(401, "No user was added from token");
            }

            // 3. See if any role matches
            if (!userHasAllowedRole(user, allowedRoles))
                throw new ForbiddenResponse("User was not authorized with roles: " + user.getRoles() + ". Needed roles are: " + allowedRoles);
//                throw new ApiException(403,"User was not authorized with roles: "+ user.getRoles()+". Needed roles are: "+ allowedRoles);

        };
    }

    @Override
    public Handler verify() {
        return (ctx) -> {
            String token = extractToken(ctx);
            UserDTO verifiedTokenUser = verifyToken(token);
            ctx.status(200).json(objectMapper.createObjectNode().put("msg", "Token is valid"));
        };
    }

    @Override
    public Handler timeToLive() {
        return (ctx) -> {
            String token = extractToken(ctx);

            verifyToken(token); // throws exception if token is not valid
            String[] chunks = token.split("\\.");
            if (chunks.length != 3) {
                throw new UnauthorizedResponse("Token is not valid");
            }

            Base64.Decoder decoder = Base64.getUrlDecoder();
            String jwtHeader = new String(decoder.decode(chunks[0]));
            String payload = new String(decoder.decode(chunks[1]));
            JsonNode node = objectMapper.readTree(payload);
            Long time = node.get("exp").asLong();
            LocalDateTime expireTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(time), TimeZone.getDefault().toZoneId());
            ZonedDateTime ztime = expireTime.atZone(TimeZone.getDefault().toZoneId());
            ZonedDateTime now = ZonedDateTime.now();
            Long difference = ztime.toEpochSecond() - now.toEpochSecond();


            ctx.status(200)
                    .json(objectMapper.createObjectNode()
                            .put("msg", "Token is valid until: " + ztime)
                            .put("expireTime", ztime.toOffsetDateTime().toString())
                            .put("secondsToLive", difference));
        };
    }


    private static boolean userHasAllowedRole(UserDTO user, Set<String> allowedRoles) {
        return user.getRoles().stream()
                .anyMatch(role -> allowedRoles.contains(role.toUpperCase()));
    }

    private String extractToken(Context ctx) {
        String header = ctx.header("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new UnauthorizedResponse("Authorization header is missing or malformed");
        }
        return header.substring(7); // Extracts token after "Bearer "
    }



    private boolean isOpenEndpoint(Set<String> allowedRoles) {
        return allowedRoles.isEmpty() || allowedRoles.contains("ANYONE");
    }

    private String createToken(UserDTO user) {
            return tokenSecurity.createToken(user, issuer, expireTimeInMillis, secretKey);
    }

    private UserDTO verifyToken(String token) {
        try {
            if (tokenSecurity.tokenIsValid(token, secretKey) && tokenSecurity.tokenNotExpired(token)) {
                return tokenSecurity.getUserWithRolesFromToken(token);
            }
            throw new UnauthorizedResponse("Invalid user or token");
        } catch (ParseException | TokenVerificationException e) {
            logger.error("Unauthorized. Could not verify token", e);
            throw new UnauthorizedResponse("Unauthorized. Could not verify token");
        }
    }

    private void sendErrorResponse(Context ctx, HttpStatus status, String errorMessage) {
        ctx.status(status)
                .json(Map.of("error", errorMessage));
    }
}