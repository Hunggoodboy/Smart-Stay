package vn.edu.ptit.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileService {
    public String saveImg(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String newFilename = UUID.randomUUID().toString() +  originalFilename.substring(originalFilename.lastIndexOf("."));
        Path upLoadPath = Paths.get("src/main/resources/static/images/"+newFilename);
        Files.copy(file.getInputStream(), upLoadPath, StandardCopyOption.REPLACE_EXISTING);
        return "images/"+newFilename;
    }
}
