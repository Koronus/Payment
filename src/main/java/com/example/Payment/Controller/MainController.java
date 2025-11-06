package com.example.Payment.Controller;

import com.example.Payment.Service.OperationService;
import com.example.Payment.Service.UserService;
import com.example.Payment.Tables.User;
import com.example.Payment.Tables.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MainController {

    @Autowired
    private UserService userService;
    private OperationService operationService;

//    @GetMapping("/")
//    public List<User> getAllUsers(){
//        return userService.getAllUsers();
//    }
//    public List<Operation> getAllOperations(){
//        return operationService.getAllOperations();
//    }

    @GetMapping("/")
    public String index(Model model){
        model.addAttribute("user",userService.getAllUsers());
        return "Main-List";
    }

}
