package vn.edu.ptit.controller;


import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.ptit.dto.Request.UtilityBillsRequest;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.dto.Response.UtilityBillsResponse;
import vn.edu.ptit.service.PaymentService;

import java.util.List;

@RestController
@AllArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/api/utility-bills")
    public ResponseEntity<List<UtilityBillsResponse> >getUtilityBillsByRoomId(Authentication authentication) {
        return ResponseEntity.ok(paymentService.getBillByCurrentUser(authentication));
    }

    @PostMapping("/api/setBill")
    public ResponseEntity<ApiResponse> setBillForCustomer(@RequestBody UtilityBillsRequest utilityBillsRequest) {
        return ResponseEntity.ok(paymentService.setBillForCurrentUser(utilityBillsRequest));
    }
}
