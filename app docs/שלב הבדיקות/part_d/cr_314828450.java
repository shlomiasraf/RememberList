// code review - ירין קשת 314828450 על גלעד אברביה 315284554
// עשיתי את ה code review על 4 הקבצים הבאים:
// OrderController
// Order
// OrderRepo
// OrderService
  
// 4 הקבצים האלה הם קשורים ובעצם מתפקדים כיחידה אחת בקוד ולכן עשיתי עליהם ביחד את ה code review
// מצורפים כאן הקבצים האלה עם הערות ה code review שלי:
//את ההערות רשמתי באנגלית לצורך נוחות(לא לערבב בין עברית לאנגלית)

//OrderController file code:

package com.Supplify.Supplify.controllers;

import com.Supplify.Supplify.entities.Order;
import com.Supplify.Supplify.entities.User;
import com.Supplify.Supplify.entities.Business;
import com.Supplify.Supplify.services.OrderService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    // Logger for logging request processing information
    private final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    // Endpoint to create an order
    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestBody OrderRequest orderRequest) {

        try {
            validateOrderRequest(orderRequest);
            // Creating an order
            Order createdOrder = orderService.createOrder(
                    orderRequest.getUser(),
                    orderRequest.getBusiness(),
                    orderRequest.getTotalAmount(),
                    orderRequest.getStatus()
            );

            return ResponseEntity.status(201).body(createdOrder);

        } catch (ValidationException e) {
            logger.warn("Validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing order creation", e);
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }
    // Endpoint to fetch orders by business ID
    @GetMapping("/business/{businessId}")
    public ResponseEntity<List<Order>> getOrdersByBusiness(@PathVariable int businessId) {
        logger.info("Fetching orders for business ID: {}", businessId);
        List<Order> orders = orderService.getOrdersByBusinessId(businessId);
        return ResponseEntity.ok(orders);
    }

    // Endpoint to fetch orders by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUser(@PathVariable int userId) {
        logger.info("Fetching orders for user ID: {}", userId);
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    // Method to validate incoming order request data
    private void validateOrderRequest(OrderRequest orderRequest) throws ValidationException {
        if (orderRequest.getUser() == null) {
            throw new ValidationException("User is required");
        }
        if (orderRequest.getBusiness() == null) {
            throw new ValidationException("Business is required");
        }
        if (orderRequest.getTotalAmount() <= 0) {
            throw new ValidationException("Total amount must be greater than zero");
        }
        if (orderRequest.getStatus() == null || orderRequest.getStatus().isEmpty()) {
            throw new ValidationException("Order status is required");
        }
    }


    // Custom exception class for validation failures
    private static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }


    // DTO for order request
    @Getter
    @Setter
    public static class OrderRequest {
        private User user;
        private Business business;
        private int totalAmount;
        private String status;
    }
}

//הערות לקובץ OrderController:

//*Code Readability:

//Variable name 'totalAmount' should be renamed to orderTotalAmount for better readability.
//Variable name 'status' should be renamed to orderStatus for better readability.
//Function names are generally good and follow a clear naming pattern.
//Missing comments inside the function(I added them myself in the code above).

//----------------------------------------------------------------------------------------------------------------------------------------------------------

//*Improving Structure:

//Issue: Validation logic is tightly coupled with the controller logic.
//Improvement: Extract validation logic into a separate OrderValidator class to improve modularity and reusability.

//----------------------------------------------------------------------------------------------------------------------------------------------------------

//*Bug Detection and Efficiency:

//Issue: int is not ideal for representing money, as it cannot handle decimal values.
//Fix: Use BigDecimal instead of int for monetary values.

//Issue: String allows invalid values (e.g., typos like "pinding" instead of "pending").
//Fix: Use an enum for OrderStatus.

//----------------------------------------------------------------------------------------------------------------------------------------------------------

//*Coding Standards:

//Issue: OrderRequest is defined inside the controller, violating 'Separation of Concerns'(SoC).
//Fix: Move OrderRequest to a DTO package (dto.OrderRequest).



/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//Order file code:

package com.Supplify.Supplify.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    // Many orders can be associated with a single user
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "user_id_fk"))
    private User user;


    // Many orders can be associated with a single business
    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false, foreignKey = @ForeignKey(name = "business_id_fk"))
    private Business business;

    @Column(name = "total_amount", nullable = false)
    private double totalAmount;

    @Column(name = "status", nullable = false, length = 45)
    private String status;


    // Timestamp of order creation
    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;
}


//הערות לקובץ OrderController:

//*Code Readability:

//Variable name 'totalAmount' should be renamed to orderTotalAmount for better readability.
//Variable name 'status' should be renamed to orderStatus for better readability.
//Function names are generally good and follow a clear naming pattern.
//Missing comments inside the function(I added them myself in the code above).

//----------------------------------------------------------------------------------------------------------------------------------------------------------

//*Improving Structure:

//Overall structure is good

//----------------------------------------------------------------------------------------------------------------------------------------------------------

//*Bug Detection and Efficiency:

//double can cause precision errors in financial calculations.
//Suggested Fix: Use BigDecimal instead of double for monetary values.

//Unexpected behavior can occur if the date is not changed from its deafult value of 'null'.
//Suggested Fix: Add Default Values for orderDate

//status is a String, which can lead to invalid values(like typos) being stored in the database.
//Suggested Fix: Improve status Handling with an Enum

//----------------------------------------------------------------------------------------------------------------------------------------------------------

//*Coding Standards:

//Use of Lombok (@Getter, @Setter, @AllArgsConstructor, @NoArgsConstructor)
//Reduces boilerplate code(repetitive code that is commonly written in many places but does not add much unique functionality) for getters, setters, and constructors.
//Ensures immutability where necessary.

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//OrderRepo file code:

package com.Supplify.Supplify.repositories;
import com.Supplify.Supplify.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepo extends JpaRepository<Order, Integer> {
    List<Order> findByBusinessId(int business_id);  // Fetch orders by business ID

    List<Order> findByUserId(int userId);  // Fetch orders by user ID
}

//הערות לקובץ OrderRepo:

//*Code Readability:

//Overall names of functions and variables are good.
//Comments are included

//----------------------------------------------------------------------------------------------------------------------------------------------------------

//*Improving Structure:

// -No need becasue the code is pretty short in this file

//----------------------------------------------------------------------------------------------------------------------------------------------------------

//*Bug Detection and Efficiency:

// -No bugs found.

//----------------------------------------------------------------------------------------------------------------------------------------------------------

//*Coding Standards:

// -No need becasue the code is pretty short in this file

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//OrderService file code:

package com.Supplify.Supplify.services;
import com.Supplify.Supplify.entities.Order;
import com.Supplify.Supplify.entities.User;
import com.Supplify.Supplify.entities.Business;
import com.Supplify.Supplify.repositories.BusinessRepo;
import com.Supplify.Supplify.repositories.OrderRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderService {
    private final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepo orderRepo;
    private final BusinessRepo businessRepo;

    // Method to create and save an order
    public Order createOrder(User user, Business business, int totalAmount, String status) {
        log.info("Creating a new order for user: {}", user.getId());

        try {
            // Validate inputs
            if (totalAmount <= 0) {
                log.error("Invalid total amount: {}", totalAmount);
                throw new IllegalArgumentException("Total amount must be greater than zero.");
            }

            if (status == null || status.isEmpty()) {
                log.error("Invalid order status: {}", status);
                throw new IllegalArgumentException("Order status cannot be empty.");
            }

            // Create and save the order
            Order order = new Order();
            order.setUser(user);
            order.setBusiness(business);
            order.setTotalAmount(totalAmount);
            order.setStatus(status);
            order.setOrderDate(LocalDateTime.now()); // Assuming order date is the current timestamp
            order = orderRepo.saveAndFlush(order);
            log.info("Order successfully created with ID: {}", order.getId());

            return order;
        } catch (Exception e) {
            log.error("Failed to create order: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Retrieve orders by business ID
    public List<Order> getOrdersByBusinessId(int businessId) {
        log.info("Fetching orders for business ID: {}", businessId);
        return orderRepo.findByBusinessId(businessId);
    }

    // Retrieve orders by user ID
    public List<Order> getOrdersByUserId(int userId) {
        log.info("Fetching orders for user ID: {}", userId);
        return orderRepo.findByUserId(userId);
    }
}

//הערות לקובץ OrderService:

//*Code Readability:

//Variable name 'totalAmount' should be renamed to orderTotalAmount for better readability.
//Variable name 'status' should be renamed to orderStatus for better readability.
//Lack of comments(I added them in the file's code above)

//----------------------------------------------------------------------------------------------------------------------------------------------------------

//*Improving Structure:

//
//Issue: Validation checks are performed directly inside createOrder.
//Fix: It should be moved to a separate utility class,
//This would improve readability and maintain Single Responsibility Principle (SRP).

//----------------------------------------------------------------------------------------------------------------------------------------------------------

//*Bug Detection and Efficiency:

//External data dependencies: There is no validation to check if the User and Business entities exist in the database before creating an order. 
//This can lead to issues if invalid user or business IDs are provided.
//Solution: Add a helper function to check for validity beforehand.

//Possible improvement: IllegalArgumentException is suitable for input validation errors, 
//but using @Transactional on createOrder can ensure that the entire transaction completes without intermediate failures.

//----------------------------------------------------------------------------------------------------------------------------------------------------------

//*Coding Standards:
//Use of Lombok (@RequiredArgsConstructor)
//Reduces boilerplate code(repetitive code that is commonly written in many places but does not add much unique functionality).

//Correct use of Spring (@Service) – The file follows Spring service best practices.
//The OrderService class is annotated with @Service, which tells Spring that this class contains business logic(Order Creation Logic and Retrieving Orders by Business or User).

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//סיכום  הערות על בקרת הקוד:

//Key Comments:

//A) Variable name 'totalAmount' and 'status'  should be renamed to 'orderTotalAmount' and 'orderStatus' respectively,
//throughout all of the code for better readability.

//B) Lack of comments through almost all of the code(I added comments here in this file)
//*The original code without my comments is included in the 'og' file in the folder of the code-review.

//C) A few validation checks issues:
//A seperate class for validation is needed in 'orderController' and 'OrderService'.
//In orderService, inside the function 'createOrder' There is no validation to check if the User and Business entities exist in the database before creating an order.

//D) A few issues with types decisions for variables:
//Inside 'orderController', the DTO assigns 'status' as int and 'totalAmount' as string.
//They should be changed to BigDecimal and Enum repectively(explanation in the file's comments).
//Inside 'order', 'totalAmount' is assigned as double. Should be changed to BigDecimal as well.
//string should also be changed to Enum in 'order'.


//----------------------------------------------------------------------------------------------------------------------------------------------------------

//Example of improvement:

//In the 'orderService' file we can add a helper function to check if the User and Business entities exist in the database before creating an order:

//if (!businessRepo.existsById(business.getId()))
//  {
//      throw new IllegalArgumentException("Business does not exist.");
//  }


