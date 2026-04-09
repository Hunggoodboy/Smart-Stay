package vn.edu.ptit.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.ptit.dto.Request.LandLordRegisterRequest;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.dto.Response.RequestLandLordResponse;
import vn.edu.ptit.entity.LandLord;
import vn.edu.ptit.service.Authentication.LandLordService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/landlord")
public class LandLordController {
    private LandLordService landLordService;
    @PostMapping("/requestToLandLord")
    public ResponseEntity<ApiResponse> requestToLandLord(@RequestBody LandLordRegisterRequest LandLordRegisterRequest) {
        return ResponseEntity.ok(landLordService.createLandLord(LandLordRegisterRequest));
    }

}
