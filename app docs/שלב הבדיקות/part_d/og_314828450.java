// code review - ירין קשת 314828450 על גלעד אברביה 315284554
// עשיתי את ה code review על 4 הקבצים הבאים:
// OrderController
// Order
// OrderRepo
// OrderService
  
// 4 הקבצים האלה הם קשורים ובעצם מתפקדים כיחידה אחת בקוד ולכן עשיתי עליהם ביחד את ה code review
// מצורפים כאן הקבצים המקוריים:
//

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
    private final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestBody OrderRequest orderRequest) {

        try {
            validateOrderRequest(orderRequest);

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

    @GetMapping("/business/{businessId}")
    public ResponseEntity<List<Order>> getOrdersByBusiness(@PathVariable int businessId) {
        logger.info("Fetching orders for business ID: {}", businessId);
        List<Order> orders = orderService.getOrdersByBusinessId(businessId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUser(@PathVariable int userId) {
        logger.info("Fetching orders for user ID: {}", userId);
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

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

    private static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }

    @Getter
    @Setter
    public static class OrderRequest {
        private User user;
        private Business business;
        private int totalAmount;
        private String status;
    }
}
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

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "user_id_fk"))
    private User user;

    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false, foreignKey = @ForeignKey(name = "business_id_fk"))
    private Business business;

    @Column(name = "total_amount", nullable = false)
    private double totalAmount;

    @Column(name = "status", nullable = false, length = 45)
    private String status;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;
}
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
    public List<Order> getOrdersByBusinessId(int businessId) {
        log.info("Fetching orders for business ID: {}", businessId);
        return orderRepo.findByBusinessId(businessId);
    }

    public List<Order> getOrdersByUserId(int userId) {
        log.info("Fetching orders for user ID: {}", userId);
        return orderRepo.findByUserId(userId);
    }

}
