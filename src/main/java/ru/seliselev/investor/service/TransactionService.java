package ru.seliselev.investor.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.seliselev.investor.entity.Account;
import ru.seliselev.investor.entity.Stock;
import ru.seliselev.investor.entity.Transaction;
import ru.seliselev.investor.entity.User;
import ru.seliselev.investor.enums.TransactionType;
import ru.seliselev.investor.logging.MyLogger;
import ru.seliselev.investor.repository.AccountRepository;
import ru.seliselev.investor.repository.StockRepository;
import ru.seliselev.investor.repository.TransactionRepository;
import ru.seliselev.investor.repository.UserRepository;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(MyLogger.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StockRepository stockRepository;
    public List<Transaction> getTransactionsByUser(Long userId) {

        List<Transaction> transactions = transactionRepository.findByUserId(userId);

        if(transactions == null || transactions.isEmpty()) {
            // обработка, если список пустой
            return Collections.emptyList();
        }

        // сортировка по дате
        transactions.sort(Comparator.comparing(Transaction::getDate));

        return transactions;

    }


    public boolean processTransaction(int quantity, BigDecimal stockPrice, Long userId, String stockName, String operation) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            logger.info("пользователь не найден");
            return false; // Пользователь не найден
        }

        Stock stock = stockRepository.findByName(stockName);
        if (stock == null) {
            stock = new Stock();
            stock.setName(stockName); // Присваиваем имя акции
            stock.setSymbol(stockName); // Присваиваем символ акции (можно уточнить, как нужно формировать символ)
            stock.setPrice(stockPrice); // Устанавливаем цену акции
            stockRepository.save(stock);
        }

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setStock(stock);
        transaction.setQuantity(quantity);
        transaction.setPurchasePrice(stockPrice);
       // transaction.setPurchasePrice(stockPrice.multiply(BigDecimal.valueOf(quantity))); цена всех акций за транзакцию
        transaction.setDate(new Date());
        logger.info("цена акции сохранненной в Transaction составила "+ transaction.getPurchasePrice());

        if (operation.equals("buy")) {
            BigDecimal totalCost = stockPrice.multiply(BigDecimal.valueOf(quantity));
            Account userAccount = user.getAccount();
            if (userAccount == null) {
                // Создаем сущность Account и устанавливаем начальный баланс
                userAccount = new Account();
                userAccount.setUser(user);
                userAccount.setInitialBalance();
                logger.info("создан user с балансом "+ user.getAccount().getBalance());
            }

            if (userAccount.getBalance().compareTo(totalCost) < 0) {
                return false; // Недостаточно средств для покупки
            }

            transaction.setType(TransactionType.BUY); // Устанавливаем тип операции

            // Обновляем баланс пользователя и сохраняем транзакцию
            userAccount.setBalance(userAccount.getBalance().subtract(totalCost));
            accountRepository.save(userAccount);
            transactionRepository.save(transaction);
            logger.info("обновляем баланс пользователя "+ user.getAccount().getBalance());

            return true;
        } else if (operation.equals("sell")) {
            // Логика для продажи акций
            // Проверить наличие акций у пользователя и достаточное количество для продажи
            List<Transaction> userStocks = transactionRepository.findByUserAndStock(user, stockName);
            int ownedQuantity = userStocks.stream().mapToInt(Transaction::getQuantity).sum();
            if (ownedQuantity < quantity) {
                log.info("недостаточно акций");
                return false; // У пользователя недостаточно акций для продажи
            }

            transaction.setType(TransactionType.SELL);

            // Обновляем баланс пользователя и сохраняем транзакцию
            Account userAccount = user.getAccount();
            BigDecimal totalEarnings = stockPrice.multiply(BigDecimal.valueOf(quantity));
            userAccount.setBalance(userAccount.getBalance().add(totalEarnings));
            accountRepository.save(userAccount);
            transactionRepository.save(transaction);

            return true;
        } else {
            return false; // Недопустимая операция
        }
    }


}