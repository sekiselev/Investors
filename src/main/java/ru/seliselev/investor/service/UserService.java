package ru.seliselev.investor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.seliselev.investor.entity.Account;
import ru.seliselev.investor.entity.Stock;
import ru.seliselev.investor.entity.Transaction;
import ru.seliselev.investor.entity.User;
import ru.seliselev.investor.enums.TransactionType;
import ru.seliselev.investor.repository.UserRepository;

import java.math.BigDecimal;
import java.util.*;


@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Autowired
    TransactionService transactionService;

@Transactional
public User registration(User user) throws Exception {
    User existingUser = userRepository.findByName(user.getName());
    if (existingUser != null) {
        throw new Exception("Пользователь с таким именем уже существует");
    }


    Account userAccount = new Account();
    userAccount.setUser(user);
    userAccount.setInitialBalance();
    user.setAccount(userAccount);


    return userRepository.save(user);
}


    public boolean authenticate(String username, String password) {

        User user = userRepository.findByName(username);

        if (user != null && user.getPassword().equals(password)) {
            return true;
        }

        return false;

    }

    public User findByName(String username){
    return userRepository.findByName(username);
    }


    public User findUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }



}


