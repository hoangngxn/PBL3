package com.example.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Size(min = 3, max = 50, message = "Fullname must be between 3 and 50 characters")
    private String fullname;

    @Email(message = "Email should be valid")
    private String email;

    @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 characters")
    private String phone;

    private String address;
    
    private String avatar;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;
} 