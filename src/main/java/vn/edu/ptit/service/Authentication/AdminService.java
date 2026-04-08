package vn.edu.ptit.service.Authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.ptit.entity.Admin;
import vn.edu.ptit.repository.AdminRepository;


@Service
public class AdminService {
    @Autowired
    private AdminRepository adminRepository;
    public Admin findByEmail(String email) {
        return adminRepository.findByEmail(email);
    }
}
