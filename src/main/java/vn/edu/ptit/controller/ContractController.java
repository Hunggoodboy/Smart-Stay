package vn.edu.ptit.controller;


import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.ptit.dto.Request.ContractCreationRequest;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.dto.Response.ContractSuggestionResponse;
import vn.edu.ptit.entity.Contracts;
import vn.edu.ptit.service.ContractService;

import java.util.List;

@RestController
@RequestMapping("/api/contract")
@AllArgsConstructor
public class ContractController {
    private final ContractService contractService;
    @GetMapping("/draft")
    public ResponseEntity<ContractSuggestionResponse> getContractDraft(
            @RequestParam("roomId") Long roomId,
            @RequestParam("userId") Long userId){
        return ResponseEntity.ok(contractService.getContractDraft(roomId, userId));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createContract(@RequestBody ContractCreationRequest contractCreationRequest){
        return ResponseEntity.ok(contractService.createContract(contractCreationRequest));
    }

    @GetMapping("/get-my-contracts")
    public ResponseEntity<?> getMyContracts(){
        return ResponseEntity.ok(contractService.getMyContracts());
    }
}
