package dk.bugelhartmann.apikey;

import dk.bugelhartmann.UserDTO;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Holds information about an API key, including the key itself, the user associated with the key, and the expiration date.
 *
 * @author: Thomas Hartmann
 */
@Getter
@ToString
public class ApiKeyDTO {
    private String apiKey;
    private UserDTO user;
    private LocalDateTime expires;
    private LocalDateTime created;
    private Status status;

    /**
     * Constructs an ApiKeyDTO with the specified API key, user, and time to live.
     *
     * @param apiKey the API key
     * @param user the user associated with the API key
     * @param timeToLive the time to live in days of the API key
     * @return an {@code ApiKeyDTO} object
     */
    public ApiKeyDTO(String apiKey, UserDTO user, int timeToLive) {
        this.apiKey = apiKey;
        this.user = user;
        this.created = LocalDateTime.now();
        this.expires = this.created.plusDays(timeToLive);
        this.status = Status.ACTIVE;
    }

}
