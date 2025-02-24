// code review - אופק מרום 208336701 על שוהם איבגי 206698359
// עשיתי את ה code review על 4 הקבצים הבאים:
// User
// userController
// UserRepo
// UserService
  
// 4 הקבצים האלה הם קשורים ובעצם מתפקדים כיחידה אחת בקוד ולכן עשיתי עליהם ביחד את ה code review
// מצורפים כאן הקבצים האלה עם הערות ה code review שלי:



//User.java:

package com.Supplify.Supplify.entities;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
@Entity

@Table(name = "USER")

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "first_name", nullable = false, length = 50)
    @NonNull
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    @NonNull
    private String lastName;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    @NonNull
    private String username;

    @Column(name = "password", nullable = false, length = 50)
    @NonNull
    private String password;

    @Column(name = "business_name", nullable = false, unique = false, length = 50)
    @NonNull
    private String businessName;

    @Column(name = "phone", nullable = false, unique = true, length = 50)
    @NonNull
    private String  phone;

    @Column(name = "role", nullable = false, unique = true, length = 50)
    @NonNull
    private String role;
}










//UserController.java:

package com.Supplify.Supplify.controllers;

import com.Supplify.Supplify.entities.User;
import com.Supplify.Supplify.Services.UserService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterUserRequest request) {
        logger.info("Received user registration request");

        if (request == null) {
            logger.warn("Request body is null");
            return ResponseEntity.badRequest().body(Map.of("error", "Request body cannot be null"));
        }

        try {
            Map<String, String> errors = validateRequest(request);
            if (!errors.isEmpty()) {
                logger.warn("Validation errors: {}", errors);
                return ResponseEntity.badRequest().body(errors);
            }

            User user = new User(
                    request.getFirstName(),
                    request.getLastName(),
                    request.getUsername(),
                    request.getPassword(),
                    request.getBusinessName(),
                    request.getPhone(),
                    request.getRole());

            User createdUser = userService.createUser(user);
            logger.info("User registered successfully: {}", createdUser.getUsername());
            return ResponseEntity.status(201).body(createdUser);

        } catch (Exception e) {
            logger.error("Error processing user registration", e);
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        logger.info("Received login request for username: {}", loginRequest.getUsername());

        try {
            boolean isAuthenticated = userService.authenticateUser(
                    loginRequest.getUsername(),
                    loginRequest.getPassword());

            if (isAuthenticated) {
                logger.info("User authenticated successfully: {}", loginRequest.getUsername());
                return ResponseEntity.ok().body(new LoginResponse("Login successful"));
            } else {
                logger.warn("Authentication failed for username: {}", loginRequest.getUsername());
                return ResponseEntity.badRequest().body(new LoginResponse("Invalid credentials"));
            }
        } catch (Exception e) {
            logger.error("Error processing login request", e);
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable int id) {
        logger.info("Fetching user with ID: {}", id);

        try {
            User user = userService.findUserByID(id);
            if (user != null) {
                logger.info("User found: {}", user.getUsername());
                return ResponseEntity.ok(user);
            } else {
                logger.warn("User not found with ID: {}", id);
                return ResponseEntity.status(404).body("User not found");
            }
        } catch (Exception e) {
            logger.error("Error fetching user with ID: {}", id, e);
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }

    private Map<String, String> validateRequest(RegisterUserRequest request) {
        Map<String, String> errors = new HashMap<>();
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            errors.put("firstName", "First name is required");
        }
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            errors.put("lastName", "Last name is required");
        }
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            errors.put("username", "Username is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            errors.put("password", "Password is required");
        }
        if (request.getBusinessName() == null || request.getBusinessName().trim().isEmpty()) {
            errors.put("businessName", "Business name is required");
        }
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            errors.put("phone", "Phone is required");
        }
        if (request.getRole() == null || request.getRole().trim().isEmpty()) {
            errors.put("role", "Role is required");
        }
        return errors;
    }
}

@Data
class RegisterUserRequest {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String businessName;
    private String phone;
    private String role;
}

@Data
class LoginRequest {
    private String username;
    private String password;
}

@Data
class LoginResponse {
    private final String message;
}

/*
 * Code Review Notes:
 * 1.Validation and login Enhancements:
 * Current validation is basic. Consider:
 * Username length and format validation
 * Password strength checks
 * Email validation (if applicable)
 * Avoid logging sensitive information like passwords
 * 2.Response Handling:
 * Create consistent response structure across all endpoints
 * Consider using a generic ResponseDTO with status, message, and data
 * 3.Code Documentation:
 * I would suggest to add detail Documentation to the neseccsery parts.
 * 
 */













//UserRepo.java:

package com.Supplify.Supplify.repositories;

import com.Supplify.Supplify.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Integer> {
    List<User> findUsersByFirstNameAndLastName(String firstName, String lastName);

    User findByusername(String username);

    boolean existsByUsername(String username);
}

/*
 * Code Review Notes:
 * Java naming conventions use camelCase hence recommedned those changes to the
 * method's names:
 * FindByUserName
 * ExistsByUserName
 */














//UserService.java:

package com.Supplify.Supplify.Services;

import com.Supplify.Supplify.entities.User;
import com.Supplify.Supplify.repositories.UserRepo;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class UserService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public User createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (userRepo.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        // Check if the phone number has a valid length (10 digits)
        if (user.getPhone().length() != 10) {
            throw new IllegalArgumentException("Phone number must be 10 digits long");
        }
        // Encrypt password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.save(user);
        return user;
    }

    public User findUserByusername(String username) {
        return userRepo.findByusername(username);
    }

    public User findUserByID(int id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User with ID " + id + " not found"));
    }

    public List<User> findUserByFirstNameAndLastName(String firstName, String lastName) {
        return userRepo.findUsersByFirstNameAndLastName(firstName, lastName);
    }

    public boolean authenticateUser(String username, String password) {
        User user = findUserByusername(username);
        return passwordEncoder.matches(password, user.getPassword());
    }
}
/*
 * Code Review Notes:
 * 1.
 * user.getPhone().length() != 10 assumes all phone numbers have exactly 10
 * digits, which is not valid for international numbers.
 * Instead, use regex-based validation or a library like
 * javax.validation.constraints.Pattern.
 * 2.Exeption Thorw's instead handle gracefully:
 * findUserByID() throws an exception if the user isn't found instead of
 * handling it gracefully, for exmaple:return userService.findUserById(id)
 * .map(ResponseEntity::ok)
 * .orElseGet(() -> ResponseEntity.status(404).body("User not found"));
 * Same logic apply to the other method that return user.
 * 3.Potential Enhancements:
 * Add method to update user if needed
 * Implement soft delete
 * 
 */





