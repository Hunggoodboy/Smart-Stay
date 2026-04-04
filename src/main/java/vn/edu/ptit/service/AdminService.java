package vn.edu.ptit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.ptit.entity.Admins;
import vn.edu.ptit.repository.AdminsRepository;


@Service
public class AdminService {
    @Autowired
    private AdminsRepository adminsRepository;
    public Admins findByEmail(String email) {
        return adminsRepository.findByEmail(email);
    }
}
