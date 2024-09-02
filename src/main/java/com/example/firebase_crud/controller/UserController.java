package com.example.firebase_crud.controller;

import com.example.firebase_crud.dto.UserDTO;
import com.example.firebase_crud.entity.Users;
import com.example.firebase_crud.service.UserService;
import com.example.firebase_crud.util.StandardResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/user")
@CrossOrigin(origins = "*",allowedHeaders = "*")
public class UserController {
    @Autowired
    UserService userService;

    @PostMapping
    public ResponseEntity<StandardResponse> upload(@ModelAttribute UserDTO dto, @RequestParam("file") MultipartFile multipartFile) {
        Users saved = userService.saveUser(dto, multipartFile);
        return new ResponseEntity<>(
                new StandardResponse(200,"success", saved),
                HttpStatus.CREATED
        );
    }
    @PutMapping
    public ResponseEntity<StandardResponse> updateUser(@ModelAttribute UserDTO dto, @RequestParam("file") MultipartFile multipartFile) {
        Users updateUser = userService.updateUser(dto, multipartFile);
        return new ResponseEntity<>(
                new StandardResponse(200,"success", updateUser),
                HttpStatus.CREATED
        );
    }
    @DeleteMapping(params = {"email"})
    public ResponseEntity<StandardResponse> deleteUser(@RequestParam("email") String email) {
        Users users = userService.deleteUser(email);
        return new ResponseEntity<>(
                new StandardResponse(200,"deleted", users),
                HttpStatus.OK
        );
    }
}
