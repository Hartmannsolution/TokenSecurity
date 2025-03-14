package dk.bugelhartmann.javalin;

import dk.bugelhartmann.UserDTO;

public interface ISecurityDAO {
    UserDTO getVerifiedUser(String username, String password) throws ValidationException;
    UserDTO createUser(String username, String password);
    UserDTO addRoleToUser(String username, String role);
    UserDTO removeRoleFromUser(String username, String role);
}
