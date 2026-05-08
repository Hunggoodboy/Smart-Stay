package vn.edu.ptit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.ptit.dto.Response.LandlordHouseResponse;
import vn.edu.ptit.entity.Contracts;
import vn.edu.ptit.entity.Customer;
import vn.edu.ptit.entity.LandLord;
import vn.edu.ptit.entity.Rooms;
import vn.edu.ptit.repository.RoomsRepository;
import vn.edu.ptit.service.Authentication.AuthService;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class LandlordHouseService {
    private final RoomsRepository roomsRepository;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<LandlordHouseResponse> getHousesForLandlord() {
        LandLord landLord = authService.getCurrentLandLord();
        List<Rooms> rooms = roomsRepository.findByLandLordIdWithContractAndCustomer(landLord.getId());
        return rooms.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private LandlordHouseResponse toResponse(Rooms room) {
        Contracts contract = room.getContract();
        Customer customer = contract != null ? contract.getCustomer() : null;

        return LandlordHouseResponse.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .address(room.getAddress())
                .ward(room.getWard())
                .district(room.getDistrict())
                .city(room.getCity())
                .fullAddress(buildFullAddress(room))
                .areaM2(room.getAreaM2())
                .maxOccupants(room.getMaxOccupants())
                .rentPrice(room.getRentPrice())
                .status(room.getStatus())
                .description(room.getDescription())
                .createdAt(room.getCreatedAt())
                .tenant(buildTenant(customer))
                .contract(buildContract(contract))
                .build();
    }

    private String buildFullAddress(Rooms room) {
        return Stream.of(room.getAddress(), room.getWard(), room.getDistrict(), room.getCity())
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(", "));
    }

    private LandlordHouseResponse.TenantInfo buildTenant(Customer customer) {
        if (customer == null) {
            return null;
        }
        return LandlordHouseResponse.TenantInfo.builder()
                .id(customer.getId())
                .fullName(customer.getFullName())
                .phoneNumber(customer.getPhoneNumber())
                .email(customer.getEmail())
                .avatarUrl(customer.getAvatarUrl())
                .build();
    }

    private LandlordHouseResponse.ContractInfo buildContract(Contracts contract) {
        if (contract == null) {
            return null;
        }
        return LandlordHouseResponse.ContractInfo.builder()
                .id(contract.getId())
                .contractCode(contract.getContractCode())
                .status(contract.getStatus())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .monthlyRent(contract.getMonthlyRent())
                .depositAmount(contract.getDepositAmount())
                .billingDate(contract.getBillingDate())
                .build();
    }
}
