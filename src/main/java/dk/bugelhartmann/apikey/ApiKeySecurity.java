package dk.bugelhartmann.apikey;

import dk.bugelhartmann.UserDTO;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.function.Predicate;

/**
 * Purpose:
 *
 * @author: Thomas Hartmann
 */
public class ApiKeySecurity implements IApiKeySecurity {
    private static final SecureRandom secureRandom = new SecureRandom(); //threadsafe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    /**
     * Generates a random API key and wraps it in an {@code ApiKeyDTO} object with the specified user and time to live.
     * @return URL-safe API key as a 32 bytes {@code String}
     */
    @Override
    public ApiKeyDTO generateApiKey(UserDTO user, int timeToLive) { // URL-safe string of 32 bytes
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String key = base64Encoder.encodeToString(randomBytes);
        return new ApiKeyDTO(key, user, timeToLive);
    }

    /**
     * Validates an API key by checking if it is not expired and is active.
     * @param apiKeyDTO the API key DTO containing the key to validate, expire time and status
     * @param matchInDb a predicate to match the key in the database based on the userDTO.username and key in the apiKeyDTO
     * @return {@code true} if the key is valid, {@code false} otherwise
     */
    @Override
    public boolean validateKey(ApiKeyDTO apiKeyDTO, Predicate<ApiKeyDTO> matchInDb) {
        System.out.println(apiKeyDTO);
        boolean isNotExpired = apiKeyDTO.getExpires().isAfter(LocalDateTime.now());
        boolean isActive = apiKeyDTO.getStatus().equals(Status.ACTIVE);
        boolean isMatch = matchInDb.test(apiKeyDTO);

        return isNotExpired && isActive && isMatch;
    }

    public static void main(String[] args) {
        ApiKeySecurity instance = new ApiKeySecurity();
        ApiKeyDTO apikeyholder = instance.generateApiKey(new UserDTO("thomas", "password"), 30);
        String validated = instance.validateKey(apikeyholder, (key) -> key.getUser().getUsername().equals("thomas"))? "Valid" : "Invalid";
        System.out.println("Key: " + apikeyholder.getApiKey() + " is " + validated);
    }
}
