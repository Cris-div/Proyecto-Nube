package com.Lab06.demo.service;

import com.Lab06.demo.dto.LoginRequest;
import com.Lab06.demo.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}