package com.fitness.controller;

import com.fitness.dto.ApiResponse;
import com.fitness.dto.UserProfileRequest;
import com.fitness.dto.UserProfileResponse;
import com.fitness.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @RequestParam(defaultValue = "demo") @NotBlank String username
    ) {
        return ResponseEntity.ok(ApiResponse.success(userService.getProfileByUsername(username)));
    }

    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> saveProfile(
            @Valid @RequestBody UserProfileRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(userService.saveProfile(request)));
    }
}
