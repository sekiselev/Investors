package ru.seliselev.investor.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.seliselev.investor.entity.User;
import ru.seliselev.investor.service.UserService;

import java.math.BigDecimal;

@Controller
public class AuthorizationController {

    @Autowired
    private UserService userService;



    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }


    @PostMapping("/login")
    public String login(@RequestParam  String username, @RequestParam  String password, Model model, RedirectAttributes redirectAttributes) {

        if (userService.authenticate(username, password)) {
            User user = userService.findByName(username);
            redirectAttributes.addAttribute("id", user.getId());
            return "redirect:/mainpage";
        } else {
            model.addAttribute("errorMsg", "Неверный логин или пароль");
            return "login";
        }

    }


}
