package dk.bugelhartmann.javalin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dk.bugelhartmann.SecurityMessages;
import dk.bugelhartmann.UserDTO;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import io.javalin.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;
import java.util.Set;

class ISecurityControllerTest {

    private ISecurityController securityController;
    private Javalin app;

    @Mock
    private ISecurityDAO securityDAO;  // Mocked DAO

    @BeforeEach
    void setUp() {
        String ISSUER = "test-issuer";
        int EXPIRE_TIME = 60000;
        String SECRET_KEY = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

        // Reset mock behavior before each test
        securityDAO = mock(ISecurityDAO.class);
        MockitoAnnotations.openMocks(this);  // Initialize mocks
        reset(securityDAO);
        try{
            UserDTO mockUser = new UserDTO("testuser", "hashedpassword", Set.of("USER")); // setting the Mockito mock behavior for the dependency (DAO method)
            when(securityDAO.getVerifiedUser("testuser", "password123")).thenReturn(mockUser);
            when(securityDAO.createUser("newuser","password123")).thenReturn(new UserDTO("newuser", Set.of("USER")));
        } catch(ValidationException ex){
            fail("User credentials are invalid");
        }

        securityController = new SecurityController(securityDAO, ISSUER, EXPIRE_TIME, SECRET_KEY);

        if (app != null) {
            app.stop();
        }

        app = Javalin.create();
        app.beforeMatched(securityController.authenticate());
        app.beforeMatched(securityController.authorize());
        app.post("/login", securityController.login());
        app.post("/register", securityController.register());

    }

    @Test
    @DisplayName("Test login with valid credentials")
    void testLogin_Success() {
//        try {
//            UserDTO mockUser = new UserDTO("testuser", "hashedpassword", Set.of("USER")); // setting the Mockito mock behavior for the dependency (DAO method)
//            when(securityDAO.getVerifiedUser("testuser", "password123")).thenReturn(mockUser);
//        } catch(ValidationException ex){
//            fail("User credentials are invalid");
//        }
        JavalinTest.test(app, (server, client) -> {
            var response = client.post("/login", Map.of("username", "testuser", "password", "password123"));
            assertEquals(HttpStatus.OK.getCode(), response.code());
            assertNotNull(response.body());
        });
    }

    @Test
    @DisplayName("Test login with invalid credentials")
    void testLogin_Failure() throws ValidationException{
        JavalinTest.test(app, (server, client) -> {
            var response = client.post("/login", Map.of("username", "testuser", "password", "wrongpassword"));
            assertEquals(HttpStatus.UNAUTHORIZED.getCode(), response.code());
        });
    }

    @Test
    @DisplayName("Test register new user")
    void testRegister() {

        JavalinTest.test(app, (server, client) -> {
            var response = client.post("/register", "{\"username\": \"newuser\", \"password\": \"password123\"}");
            // Print response for debugging
            System.out.println("Response Code: " + response.code());
            System.out.println("Response Body: " + response.body().string());

            assertEquals(HttpStatus.CREATED.getCode(), response.code());
        });
    }
}
