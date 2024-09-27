package dk.bugelhartmann.apikey;

import dk.bugelhartmann.UserDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ApiKeySecurityTest {
    IApiKeySecurity apiKeySecurity = new ApiKeySecurity();
    ApiKeyDTO apiKeyHolder;
    UserDTO user;

    @BeforeEach
    void setUp() {
        user = new UserDTO("thomas", "password123");
        apiKeyHolder = apiKeySecurity.generateApiKey(user, 30);
    }

    @Test
    void generateApiKey() {
        ApiKeyDTO apiKeyHolder = apiKeySecurity.generateApiKey(user, 30);
        boolean lengthMoreThan30 = apiKeyHolder.getApiKey().length() > 30;
        boolean notExpired = apiKeyHolder.getExpires().isAfter(LocalDateTime.now());
        boolean isActive = apiKeyHolder.getStatus().equals(Status.ACTIVE);
        assertTrue(lengthMoreThan30 && notExpired && isActive);
    }

    @Test
    void validateKey() {
        boolean isValid = apiKeySecurity.validateKey(apiKeyHolder, (key) -> key.getUser().getUsername().equals("thomas"));
        assertTrue(isValid);
    }
}