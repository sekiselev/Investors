package ru.seliselev.investor.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.seliselev.investor.entity.User;
import ru.seliselev.investor.service.UserService;

@Controller
public class RegistrationController {

    @Autowired
    UserService userService;

    @GetMapping("/register")
    public String showRegistration(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes) throws Exception {
        User registeredUser = userService.registration(user);
        // Передаем 'id' зарегистрированного пользователя как атрибут для перенаправления
        redirectAttributes.addAttribute("id", registeredUser.getId());
        return "redirect:/mainpage";
    }

}