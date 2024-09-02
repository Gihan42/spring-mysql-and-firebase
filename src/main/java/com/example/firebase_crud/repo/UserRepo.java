package com.example.firebase_crud.repo;

import com.example.firebase_crud.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepo extends JpaRepository<Users,Long> {

    @Query(value = "select * from user where user_email=?1",nativeQuery = true)
    Users findByEmail(String email);
}
