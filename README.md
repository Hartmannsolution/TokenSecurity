# Maven repository on github for JWT token generation and validation
## Following files are included in the repository
1. pom.xml
2. dk.bugelhartmann.ITokenSecurity.java
  - Interface for TokenSecurity with 5 methods:
    - **UserDTO getUserWithRolesFromToken(String token) throws ParseException;**
    - **boolean tokenIsValid(String token, String secret) throws ParseException, JOSEException;**
    - **boolean tokenNotExpired(String token) throws ParseException;**
    - **int timeToExpire(String token) throws ParseException;**
    - **String createToken(UserDTO user, String issuer, String token_expire_time_in_milliseconds, String secret_32chars_key) throws TokenCreationException;**
3. dk.bugelhartmann.TokenSecurity.java
    - implements ITokenSecurity methods above
4. dk.bugelhartmann.ITokenSecurityTest.java
5. dk.bugelhartmann.UserDTO.java
  - DTO class for User with 3 fields:
    - String username;
    - String password;
    - Set<String> roles;

