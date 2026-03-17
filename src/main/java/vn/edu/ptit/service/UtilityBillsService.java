package vn.edu.ptit.service;


import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import vn.edu.ptit.dto.Response.UtilityBillsResponse;
import vn.edu.ptit.dto.UserDTO;
import vn.edu.ptit.entity.UtilityBills;
import vn.edu.ptit.repository.UtilityBillsRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class UtilityBillsService {
    private final AuthService authService;
    private final UtilityBillsRepository utilityBillsRepository;
    public List<UtilityBillsResponse> getBillByCurrentUser(Authentication authentication) {
         UserDTO userDTO = authService.getCurrentUser(authentication);
         List<UtilityBills> utilityBills = utilityBillsRepository.findAllByUserId(userDTO.getId());
         List<UtilityBillsResponse> utilityBillsResponses = utilityBills.stream()
                 .map(UtilityBills -> new UtilityBillsResponse().fromEntity(UtilityBills))
                 .toList();
         return utilityBillsResponses;
    }
}
