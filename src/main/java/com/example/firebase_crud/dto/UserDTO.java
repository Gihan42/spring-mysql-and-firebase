package com.example.firebase_crud.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDTO {
    private Long user;
    private String userName;
    private String userEmail;
    private String imageUrl;
}
