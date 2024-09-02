package com.example.firebase_crud.service;

import com.example.firebase_crud.dto.UserDTO;
import com.example.firebase_crud.entity.Users;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

public interface UserService {
    String uploadFile(File file, String fileName) throws IOException;


    Users saveUser(UserDTO dto,MultipartFile multipartFile);

    Users updateUser(UserDTO dto,MultipartFile multipartFile);

    Users deleteUser(String email);
    //get firebase image
    byte[] getImageFromUrl(String imageUrl);
    //delete firebase image
    boolean deleteFile(String fileUrl);
}
