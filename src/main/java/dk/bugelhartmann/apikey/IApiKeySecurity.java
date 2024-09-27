package dk.bugelhartmann.apikey;

import dk.bugelhartmann.UserDTO;

import java.util.function.Predicate;

/**
 * Purpose: To provide security using API keys
 */
public interface IApiKeySecurity {
    /**
     * Generates an API key for the specified user with the specified time to live and wraps it in an {@code ApiKeyDTO} object.
     * @param user the {@UserDTO} representing the user.
     * @param timeToLive the time to live in days of the API key.
     * @return an {@code ApiKeyDTO} object with the UserDTO, created, expires, and status fields.
     */
    ApiKeyDTO generateApiKey(UserDTO user, int timeToLive);

    /**
     * Validates an API key by checking if it is not expired and is active.
     * @param apiKeyDTO the API key DTO containing the key to validate, expire time and status
     * @param matchInDb a predicate to match the key in the database based on the {@userDTO.username} or {@id} and key in the apiKeyDTO
     * @return {@code true} if the key is valid (status=active,exipredate not reached and exist with the user in the database), {@code false} otherwise
     */
    boolean validateKey(ApiKeyDTO apiKeyDTO, Predicate<ApiKeyDTO> matchInDb);
}
