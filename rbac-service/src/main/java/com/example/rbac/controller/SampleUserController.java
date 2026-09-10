package com.example.rbac.controller;

import com.example.rbac.config.RequirePermission;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Demo controller matching the "HR Manager creates an employee" walkthrough,
 * plus requireAll/requireAny examples. Illustrative - swap in the real
 * user-management controller/business logic for the actual project; only
 * the {@code @RequirePermission} annotations are the part Part 8 owns.
 */
@RestController
@RequestMapping("/api/v1/users")
public class SampleUserController {

    @RequirePermission("USER_CREATE")
    @PostMapping
    public Map<String, Object> createUser(@RequestBody Map<String, Object> body) {
        return Map.of("status", "created", "user", body);
    }

    @RequirePermission("USER_READ")
    @GetMapping("/{id}")
    public Map<String, Object> getUser(@PathVariable String id) {
        return Map.of("id", id, "name", "Sample User");
    }

    // requireAll example: both permissions needed to edit a user's profile.
    @RequirePermission(requireAll = { "USER_READ", "USER_UPDATE" })
    @PutMapping("/{id}")
    public Map<String, Object> updateUser(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return Map.of("status", "updated", "id", id);
    }

    @RequirePermission("USER_DELETE")
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteUser(@PathVariable String id) {
        return Map.of("status", "deleted", "id", id);
    }

    // requireAny example: either permission is enough to pull a report.
    @RequirePermission(requireAny = { "REPORT_VIEW", "REPORT_EXPORT" })
    @GetMapping("/{id}/report")
    public Map<String, Object> getUserReport(@PathVariable String id) {
        return Map.of("id", id, "report", "…report contents…");
    }
}
