package vn.edu.ptit.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import vn.edu.ptit.service.Authentication.AuthService;

@Controller
@AllArgsConstructor
public class WebController {
    private final AuthService authService;

    @GetMapping("login")
    public String login() {
        return "auth";
    }

    @GetMapping("register")
    public String register() {
        return "auth";
    }

    @GetMapping("/myHome")
    public String myHome() {
        return "index";
    }

    @GetMapping("/payment")
    public String payment() {
        return "payment";
    }

    @GetMapping("/chatMessage")
    public String chatMessage() {
        return "chatMessage";
    }

    @GetMapping("/postRooms")
    public String postNewRoom() {
        return "postNewRoom";
    }

    @GetMapping("/")
    public String roomsSummary() {
        return "roomsSummary";
    }

    @GetMapping("/rooms/{id}")
    public String roomDetail(@PathVariable Long id) {
        return "roomDetail";
    }
    @GetMapping("/room-detail-management/{id}")
    public String roomDetailManagement(@PathVariable Long id) {
        return "roomDetailManagement";
    }
    @GetMapping("/MyRentalRequest")
    public String rentalRequest() {
        return "rentalRequests";
    }

    @GetMapping("/adminVerify")
    public String adminVerify() {
        return "adminVerify";
    }

    @GetMapping("/registerLandLord")
    public String registerLandLord() {
        return "registerLandlord";
    }

    @GetMapping("/landlord-view")
    public String landlordView() {
        return "landlord_index";
    }
    @GetMapping("/contract/create")
    public String createContract() {
        return "contractCreation";
    }
    @GetMapping("/myContracts")
    public String myContracts() {
        return "myContracts";
    }
    @GetMapping("/contractDetail/{id}")
    public String contractDetail(@PathVariable Long id){
        return "contractDetail";
    }
    @GetMapping("/createRoomManage")
    public String createRoomManage() {
        return "createRoomManage";
    }

    @GetMapping("/revenue-management")
    public String revenueManagement() {
        return "revenue_management";
    }
}
