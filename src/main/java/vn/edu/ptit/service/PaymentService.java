package vn.edu.ptit.service;


import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.edu.ptit.dto.Request.RentPaymentRequest;
import vn.edu.ptit.dto.Response.FeeResponse;
import vn.edu.ptit.dto.Response.UtilityBillsResponse;
import vn.edu.ptit.dto.UserDTO;
import vn.edu.ptit.entity.Contracts;
import vn.edu.ptit.entity.UtilityBills;
import vn.edu.ptit.repository.ContractsRepository;
import vn.edu.ptit.repository.RoomsRepository;
import vn.edu.ptit.repository.UtilityBillsRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class UtilityBillsService {
    private final AuthService authService;
    private final UtilityBillsRepository utilityBillsRepository;
    private final ContractsRepository contractsRepository;
    private final RoomsRepository  roomsRepository;
    public List<UtilityBillsResponse> getBillByCurrentUser(Authentication authentication) {
         UserDTO userDTO = authService.getCurrentUser(authentication);
         List<UtilityBills> utilityBills = utilityBillsRepository.findAllByUserId(userDTO.getId());
         List<UtilityBillsResponse> utilityBillsResponses = utilityBills.stream()
                 .map(UtilityBills -> new UtilityBillsResponse().fromEntity(UtilityBills))
                 .toList();
         return utilityBillsResponses;
    }

//    public List<FeeResponse> setBillForCurrentUser(String roomNumber, RentPaymentRequest rentPaymentRequest) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        UserDTO userDTO = authService.getCurrentUser(authentication);
//        Contracts contracts = roomsRepository.findContractsByRoomNumber(roomNumber).orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng phòng này"));
//
//
//    }
}
