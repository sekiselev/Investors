package ru.seliselev.investor.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.seliselev.investor.entity.Transaction;
import ru.seliselev.investor.entity.User;
import ru.seliselev.investor.enums.TransactionType;
import ru.seliselev.investor.repository.UserRepository;
import ru.seliselev.investor.service.StockService;
import ru.seliselev.investor.service.TransactionService;
import ru.seliselev.investor.service.UserService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class StockController {

    @Autowired
    private StockService stockService;
    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;
    @Autowired
    UserRepository userRepository;

    @GetMapping("/get-price-stock")
    public String getStockPrice(@RequestParam("stockName") String stockName, @RequestParam("id") Long id, Model model) throws IOException {


        User user = userService.findUserById(id);
        if (user != null) {

           stockService.prepareModelAttributes(model,user,stockName);


        }
        return "mainpage";
    }


    @PostMapping("/process-transaction")
    public String processTransaction(@RequestParam int quantity, @RequestParam BigDecimal stockPrice,
                                     @RequestParam Long id, @RequestParam String stockName, @RequestParam String operation, Model model) throws IOException {

        boolean result = transactionService.processTransaction(quantity, stockPrice, id, stockName, operation);
        if (result) {
            User user = userService.findUserById(id);
            if (user != null) {
                stockService.prepareModelAttributes(model,user,null);
            }
        }
        return "mainpage";
    }


}



