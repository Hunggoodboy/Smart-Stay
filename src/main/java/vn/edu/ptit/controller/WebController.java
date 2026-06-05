package vn.edu.ptit.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import vn.edu.ptit.service.Authentication.AuthService;
import vn.edu.ptit.repository.RoomsRepository;
import org.springframework.ui.Model;

@Controller
@AllArgsConstructor
public class WebController {
    private final AuthService authService;
    private final RoomsRepository roomsRepository;

    @GetMapping("login")
    public String login() {
        return "auth";
    }

    @GetMapping("register")
    public String register() {
        return "auth";
    }

    @GetMapping("/myHome")
    public String myHome(Model model) {
        try {
            Long userId = authService.getCurrentUserId();
            boolean isRenting = roomsRepository.existsActiveRentalByCustomerId(userId);
            if (!isRenting) {
                System.out.println("Người dùng với id " + userId + " chưa thuê phòng");
                model.addAttribute("noRoomRented", true);
            }
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "index";
    }

    @GetMapping("/payment")
    public String payment() {
        try {
            authService.getCurrentUserId();
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "payment";
    }

    @GetMapping("/chatMessage")
    public String chatMessage() {
        try {
            authService.getCurrentUserId();
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "chatMessage";
    }

    @GetMapping("/postRooms")
    public String postNewRoom() {
        try {
            authService.getCurrentUserId();
        } catch (Exception e) {
            return "redirect:/login";
        }
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

    @GetMapping("/users/{id}")
    public String userDetail(@PathVariable Long id) {
        try {
            authService.getCurrentUserId();
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "userDetail";
    }

    @GetMapping("/room-detail-management/{id}")
    public String roomDetailManagement(@PathVariable Long id) {
        return "roomDetailManagement";
    }

    @GetMapping("/MyRentalRequest")
    public String rentalRequest() {
        try {
            authService.getCurrentUserId();
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "rentalRequests";
    }

    @GetMapping("/adminVerify")
    public String adminVerify() {
        return "adminVerify";
    }

    @GetMapping("/adminDashboard")
    public String adminDashboard() {
        return "adminDashboard";
    }

    @GetMapping("/adminUsers")
    public String adminUsers() {
        return "adminUsers";
    }

    @GetMapping("/adminPosts")
    public String adminPosts() {
        return "adminPosts";
    }

    @GetMapping("/registerLandLord")
    public String registerLandLord() {
        try {
            authService.getCurrentUserId();
        } catch (Exception e) {
            return "redirect:/login";
        }
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
        try {
            authService.getCurrentUserId();
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "myContracts";
    }

    @GetMapping("/contractDetail/{id}")
    public String contractDetail(@PathVariable Long id) {
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

    @GetMapping("/landlord/rent-payments")
    public String landlordRentPayments() {
        return "landlord_rent_payments";
    }

    @GetMapping("/my-appointments")
    public String myAppointments() {
        try {
            authService.getCurrentUserId();
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "appointments";
    }
}
