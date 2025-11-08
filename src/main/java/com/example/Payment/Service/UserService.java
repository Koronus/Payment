package com.example.Payment.Service;


import com.example.Payment.Repository.UserRepository;
import com.example.Payment.Tables.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers()
    {

        return userRepository.findAll();
    }

    public User getUserById(Long id){
       return userRepository.findById(id).orElse(null);
    }

    public void saveUser(User user){
        userRepository.save(user);
    }

    public void deleteUser(Long id){
        userRepository.deleteById(id);
    }
}
