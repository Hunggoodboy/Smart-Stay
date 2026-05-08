package vn.edu.ptit.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.ptit.dto.Response.LandlordHouseResponse;
import vn.edu.ptit.service.LandlordHouseService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/landlord/houses")
public class LandlordHouseController {
    private final LandlordHouseService landlordHouseService;

    @GetMapping
    public ResponseEntity<List<LandlordHouseResponse>> getLandlordHouses() {
        return ResponseEntity.ok(landlordHouseService.getHousesForLandlord());
    }
}
