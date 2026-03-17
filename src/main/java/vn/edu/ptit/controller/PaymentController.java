package vn.edu.ptit.controller;


import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.ptit.dto.Response.UtilityBillsResponse;
import vn.edu.ptit.entity.UtilityBills;
import vn.edu.ptit.service.UtilityBillsService;

import java.util.List;

@RestController
@AllArgsConstructor
public class PaymentController {

    private final UtilityBillsService utilityBillsService;

    @GetMapping("/api/utility-bills")
    public ResponseEntity<List<UtilityBillsResponse> >getUtilityBillsByRoomId(Authentication authentication) {
        return ResponseEntity.ok(utilityBillsService.getBillByCurrentUser(authentication));
    }
}
