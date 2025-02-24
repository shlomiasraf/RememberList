// code review - שלומי אסרף תז 207970252 עשיתי על טליה פרץ 322353780
//עשיתי את code review
//על שלושת הקבצים הבאים:
//SupplierController, SupplierService, Supplier.
//איחדתי את שלושת הקבצים הנ"ל מאחר והם משמשים כלוגיקה משותפת וביחד הם מרכיבים את הסופלייר



package com.Supplify.Supplify.entities;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "SUPPLIERS")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int supplierId;

    @Column(name = "comapny_name")
    private String companyName;

    @Column(name = "contact_person", nullable = false)
    @NonNull
    private String contactPerson;

    @Column(nullable = false)
    @NonNull
    private String email;

    @Column(nullable = false)
    @NonNull
    private String phone;
}

package com.Supplify.Supplify.services;
import com.Supplify.Supplify.entities.BusinessSupplier;
import com.Supplify.Supplify.repositories.BusinessSupplierRepo;
import com.Supplify.Supplify.repositories.SupplierRepo;
import com.Supplify.Supplify.entities.Supplier;
import lombok.AllArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;


import java.util.List;

@Service
@AllArgsConstructor
public class SupplierService {

    private final SupplierRepo supplierRepo;
    private final BusinessSupplierRepo businessSupplierRepo;

    @Transactional
    public Supplier createSupplier(Supplier supplier, Integer businessId) {
        // Step 1: Save the supplier locally
        Supplier savedSupplier = supplierRepo.save(supplier);

        // Step 2: Link the supplier to the business
        BusinessSupplier businessSupplier = new BusinessSupplier(businessId, savedSupplier.getSupplierId());
        businessSupplierRepo.save(businessSupplier);
        return savedSupplier;
    }

    public List<Supplier> getAllSuppliers() {
        return supplierRepo.findAll();
    }

    //public List<Supplier> getAllSuppliersbyBusinessId(Integer businessId) {
   // }

    public Supplier getSupplierById(Integer id) {
        return supplierRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
    }
    public List<Supplier> getSuppliersByBusinessId(Integer businessId) {
        List<Integer> supplierIds = businessSupplierRepo.findSupplierIdsByBusinessId(businessId);
        return supplierIds.stream()
                .map(supplierId -> supplierRepo.findById(supplierId)
                        .orElseThrow(() -> new RuntimeException("Supplier not found")))
                .collect(Collectors.toList());
    }

}

package com.Supplify.Supplify.controllers;

import com.Supplify.Supplify.services.SupplierService;
import com.Supplify.Supplify.entities.Supplier;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {
    private final Logger logger = LoggerFactory.getLogger(SupplierController.class);

    private final SupplierService supplierService;
    private final RestTemplate restTemplate;

    @Value("${external.api.url}")
    private String externalApiUrl;

    @Value("${external.api.token}")
    private String apiToken;

    @PostMapping("/business/{businessId}")
    public ResponseEntity<?> createSupplier(
            @RequestBody Supplier supplier,
            @PathVariable Integer businessId) {

        logger.info("Received supplier creation request for business ID: {}", businessId);

        try {
            validateSupplier(supplier);
            Supplier createdSupplier = supplierService.createSupplier(supplier, businessId);

            // Only attempt to send to external API if URL is configured
            if (externalApiUrl != null && !externalApiUrl.isEmpty()) {
                sendToExternalApi(createdSupplier);
            } else {
                logger.warn("External API URL not configured. Skipping external API sync.");
            }

            return ResponseEntity.status(201).body(createdSupplier);

        } catch (ValidationException e) {
            logger.warn("Validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing supplier creation", e);
            return ResponseEntity.status(500)
                    .body("Internal Server Error: " + e.getMessage());
        }
    }

    private void validateSupplier(Supplier supplier) throws ValidationException {
        if (supplier.getCompanyName() == null || supplier.getCompanyName().isEmpty()) {
            throw new ValidationException("Supplier company name is required");
        }
        if (supplier.getContactPerson() == null || supplier.getContactPerson().isEmpty()) {
            throw new ValidationException("Contact person is required");
        }
        if (supplier.getEmail() == null || supplier.getEmail().isEmpty()
                || !supplier.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ValidationException("Valid email is required");
        }
        if (supplier.getPhone() == null || supplier.getPhone().isEmpty()) {
            throw new ValidationException("Phone number is required");
        }
    }

    private void sendToExternalApi(Supplier supplier) {
        // Skip if URL is still the placeholder
        if (externalApiUrl.contains("your-real-api-endpoint")) {
            logger.info("Skipping external API call - placeholder URL detected");
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiToken);
        headers.set("Content-Type", "application/json");

        HttpEntity<Supplier> requestEntity = new HttpEntity<>(supplier, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    externalApiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            logger.info("Supplier sent to external API. Response status: {}",
                    response.getStatusCode());
        } catch (Exception e) {
            logger.error("Failed to send supplier to external API: {}", e.getMessage(), e);
        }
    }

    private static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<List<Supplier>> getSuppliersByBusinessId(@PathVariable Integer businessId) {
        List<Supplier> suppliers = supplierService.getSuppliersByBusinessId(businessId);
        return ResponseEntity.ok(suppliers);
    }

}

// אבחר שלוש פונקציות מרכזיות ואבצע סקירה עליהן. הפונקציות שנבחרו הן:

// createSupplier בשירות (SupplierService).
// validateSupplier בבקר (SupplierController).
// sendToExternalApi בבקר (SupplierController).
// Code Review:
// 1. createSupplier בשירות:
// יתרונות:

// הפונקציה מבצעת חלוקה ברורה בין שמירת הספק לבסיס הנתונים לבין קישורו לעסק, מה שמאפשר קריאה נוחה.
// הפונקציה משתמשת באובייקטים של JPA בצורה יעילה (לדוגמה, save).
// שיפורים אפשריים:

// יש לבדוק אם businessId קיים לפני קישור הספק לעסק. כרגע, לא מתבצעת ולידציה כלשהי על מזהה העסק.
// רצוי להוסיף טיפול חריגות במקרה שבו פעולת השמירה נכשלת, כדי למנוע מצב שבו נתונים נשארים לא עקביים.
// כדאי לשקול שימוש בלוגים (logging) כדי לעקוב אחר תהליך יצירת הספק, במיוחד כאשר יש צורך לדבג בעיות.
// 2. validateSupplier בבקר:
// יתרונות:

// הפונקציה מכילה ולידציה ברורה לכל שדות הספק החיוניים (שם חברה, איש קשר, אימייל, טלפון).
// נעשה שימוש בחריגות מותאמות אישית (ValidationException), מה שמשפר את קריאות הקוד.
// שיפורים אפשריים:

// הבדיקה למייל חוקי יכולה להיות ממוקדת יותר (הביטוי הרגולרי בסיסי מאוד ועלול לפספס תבניות חוקיות מסוימות).
// אפשר להעביר את הלוגיקה של ולידציה לשירות ייעודי או למחלקה נפרדת, כדי להפחית עומס בבקר.
// כדאי להוסיף ולידציה לפורמט מספר הטלפון, בהתאם לפורמטים הרצויים במדינה.
// 3. sendToExternalApi בבקר:
// יתרונות:

// נעשה שימוש נכון ב-HttpHeaders לצורך הוספת Authorization ו-Content-Type.
// הפונקציה מטפלת במקרים שבהם ה-URL הוא רק placeholder, מה שמונע קריאות מיותרות ל-API.
// שיפורים אפשריים:

// כדאי ליידע את המשתמש אם קריאה ל-API החיצוני נכשלת, כדי לאפשר לו לנקוט בפעולות מתקנות.
// אפשר לשקול להעביר את כתובת ה-URL וה-Token למחלקה נפרדת שמרכזת את הקונפיגורציות.
// מומלץ להוסיף בדיקות אוטומטיות לפונקציה זו, כדי לוודא שכל שינוי עתידי בקוד לא יגרום לבעיות.