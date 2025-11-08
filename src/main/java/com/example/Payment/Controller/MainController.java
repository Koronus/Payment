package com.example.Payment.Controller;

import com.example.Payment.Service.UserService;
import com.example.Payment.Tables.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MainController {

    @Autowired
    private UserService userService;

    @GetMapping("/view")
    public String index(Model model){
        model.addAttribute("users", userService.getAllUsers());
        return "Main-List";
    }

    @GetMapping("/registration")
    public String viewProfile(Model model){
        model.addAttribute("user", new User());
        return "View-Form";
    }


    @PostMapping("/save")
    public String saveUser(@ModelAttribute("user") User user){
        userService.saveUser(user);
        return "redirect:/view"; // Перенаправляем на главную страницу
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return "redirect:/view";
    }


    @GetMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable("id") Long id){
        User user = userService.getUserById(id);
        user.setCompleted(!user.isCompleted());
        userService.saveUser(user);
        return "redirect:/";
    }

    @GetMapping("/edit/{id}")
    public String editUser(@PathVariable("id") Long id, Model model){
        model.addAttribute("user", userService.getUserById(id));
        return "View-Form";
    }
}