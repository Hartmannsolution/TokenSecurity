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

## JitPack
0. Go to Github repository and create a release. Tagname and release name should be the same (1.0.0)
1. Go to [JitPack](https://jitpack.io/)
2. Login with your github account
3. CI -> Get started -> Find repository -> Hartmannsolution/TokenSecurity -> Settings -> Set `Maven`, `JDK version` and `Build version` -> Save
4. Go back to jitpack.io and click `Get it` -> Copy the dependency
5. Add the dependency to your project's pom.xml togethr with the repository tag:
```xml
	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
```
6. Run `mvn clean install` to get the dependency from JitPack into your project

## JavaDoc
- JavaDoc is added to all classes and methods
- Maven plugin is added to generate JavaDoc
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-javadoc-plugin</artifactId>
    <version>3.2.0</version>
    <executions>
        <execution>
            <id>attach-javadocs</id>
            <goals>
                <goal>jar</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```
- Version 3.2.0 is a version that is supported by JitPack (3.10.0 is not supported)
- Now when running `mvn clean install` JavaDoc is generated and can be found in the target folder
- On JitPack the JavaDoc is also available
- When using the dependency in your project, you can see the JavaDoc in your IDE (else go to the intellij maven tab and download sources and JavaDoc)

## Api Key
A new package is added: `apikey`. It contains 2 classes, an enum and an interface:
1. `ApiKeyDTO.java`
  - UserDTO
  - String apiKey
  - LocalDateTime created
  - LocalDateTime expires
  - Status
2. `IApiKeySecurity.java`
  - Interface for ApiKeySecurity with 4 methods:
    - **ApiKeyDTO generateApiKey(UserDTO user, int timeToLive);**
    - **boolean validateKey(ApiKeyDTO apiKeyDTO, Predicate<ApiKeyDTO> matchInDb);**
      - Predicate is a functional interface that takes an ApiKeyDTO and returns a boolean to check if the key is in the database and associated with the user.
3. `ApiKeySecurity.java`
4. `Status.java` (enum)