package ru.seliselev.investor.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.seliselev.investor.entity.User;
import ru.seliselev.investor.service.UserService;

@Controller
@RequestMapping("/")
public class MainPageController {

    @Autowired
    private UserService userService;

    @GetMapping("/mainpage")
    public String showMainPage(Model model, @RequestParam("id") Long id) {
        if (id != null) {
            // Ищем пользователя по его id

            User user = userService.findUserById(id);
            if (user != null) {
                // Передаем пользователя в модель
                model.addAttribute("user", user);
            }
        }

        return "mainpage"; // Возвращаем страницу главной страницы
    }

}
