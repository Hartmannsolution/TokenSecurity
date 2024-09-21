# Maven repository on github for JWT token generation and validation
## Following files are included in the repository
1. pom.xml
2. dk.bugelhartmann.ITokenSecurity.java
  - Interface for TokenSecurity with 5 methods:
    - UserDTO getUserWithRolesFromToken(String token) throws ParseException;
    - boolean tokenIsValid(String token, String secret) throws ParseException, JOSEException;
    - boolean tokenNotExpired(String token) throws ParseException;
    - int timeToExpire(String token) throws ParseException;
    - String createToken(UserDTO user, String ISSUER, String TOKEN_EXPIRE_TIME, String SECRET_KEY) throws TokenCreationException;
3. dk.bugelhartmann.TokenSecurity.java
4. dk.bugelhartmann.ITokenSecurityTest.java
5. dk.bugelhartmann.UserDTO.java
  - DTO class for User with 2 fields:
    - String username;
    - Set<String> roles;

