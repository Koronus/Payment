package com.example.Payment.Service;


//import com.example.Payment.Dto.UserDto;
import com.example.Payment.Repository.UserRepository;
import com.example.Payment.Entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers()
    {

        return userRepository.findAll();
    }

    public User getUserById(UUID id){
       return userRepository.findById(id).orElse(null);
    }

    public void saveUser(User user){
        userRepository.save(user);
    }

    public void deleteUser(UUID id){
        userRepository.deleteById(id);
    }
}
