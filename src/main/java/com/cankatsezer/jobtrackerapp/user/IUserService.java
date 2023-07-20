package com.cankatsezer.jobtrackerapp.user;

import com.cankatsezer.jobtrackerapp.registiration.RegistrationRequest;

import java.util.List;

public interface IUserService {

    List<User> getAllUsers();

    User registerUser(RegistrationRequest registrationRequest);

    User findByEmail(String email);

}
