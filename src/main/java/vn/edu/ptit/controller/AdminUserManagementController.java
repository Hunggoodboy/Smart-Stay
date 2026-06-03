package vn.edu.ptit.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.ptit.dto.Request.AdminUserRoleRequest;
import vn.edu.ptit.dto.Request.AdminUserStatusRequest;
import vn.edu.ptit.dto.Response.AdminDashboardCountResponse;
import vn.edu.ptit.dto.Response.AdminUserResponse;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.entity.User;
import vn.edu.ptit.service.admin.AdminUserManagementService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserManagementController {

    private final AdminUserManagementService adminUserService;

    @GetMapping("/all")
    public ResponseEntity<List<AdminUserResponse>> getAllUsers(
            @RequestParam(defaultValue = "false") boolean includeDeleted
    ) {
        return ResponseEntity.ok(adminUserService.getAllUsers(includeDeleted));
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<AdminUserResponse>> getUsersByRole(
            @PathVariable User.Role role,
            @RequestParam(defaultValue = "false") boolean includeDeleted
    ) {
        return ResponseEntity.ok(adminUserService.getUsersByRole(role, includeDeleted));
    }

    @GetMapping("/status")
    public ResponseEntity<List<AdminUserResponse>> getUsersByStatus(
            @RequestParam Boolean active,
            @RequestParam(defaultValue = "false") boolean includeDeleted
    ) {
        return ResponseEntity.ok(adminUserService.getUsersByStatus(active, includeDeleted));
    }

    @GetMapping("/keyword")
    public ResponseEntity<List<AdminUserResponse>> getUsersByKeyword(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "false") boolean includeDeleted
    ) {
        return ResponseEntity.ok(adminUserService.getUsersByKeyword(keyword, includeDeleted));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminUserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.getUser(id));
    }

    @GetMapping("/locked/total")
    public ResponseEntity<AdminDashboardCountResponse> countLockedUsers() {
        return ResponseEntity.ok(new AdminDashboardCountResponse("lockedUsers", adminUserService.countLockedUsers()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody AdminUserStatusRequest request
    ) {
        return ResponseEntity.ok(adminUserService.updateStatus(id, request.getActive()));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<ApiResponse> updateRole(
            @PathVariable Long id,
            @RequestBody AdminUserRoleRequest request
    ) {
        return ResponseEntity.ok(adminUserService.updateRole(id, request.getRole()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> softDelete(@PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.softDelete(id));
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<ApiResponse> restore(@PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.restore(id));
    }
}
