package vn.edu.ptit.controller;


import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.ptit.dto.Request.UtilityBillsRequest;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.dto.Response.UtilityBillsResponse;
import vn.edu.ptit.service.PaymentService;

import java.util.List;

@RestController
@RequestMapping("/api/utility-bills")
@AllArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/newest")
    public ResponseEntity<UtilityBillsResponse> getNewest() {
        return ResponseEntity.ok(paymentService.getNewestBillsResponse());
    }

    @GetMapping()
    public ResponseEntity<List<UtilityBillsResponse> >getUtilityAllBillsByRoomId() {
        return ResponseEntity.ok(paymentService.getAllBillsByCurrentUser());
    }

    @PostMapping("/api/setBill")
    public ResponseEntity<ApiResponse> setBillForCustomer(@RequestBody UtilityBillsRequest utilityBillsRequest) {
        return ResponseEntity.ok(paymentService.setBillForCurrentUser(utilityBillsRequest));
    }
}
