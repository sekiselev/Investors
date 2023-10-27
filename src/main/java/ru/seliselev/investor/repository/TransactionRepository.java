package ru.seliselev.investor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.seliselev.investor.entity.Transaction;
import ru.seliselev.investor.entity.User;

import java.util.List;
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserId(Long userId);

    List<Transaction> findByStockId(Long stockId);

    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.stock.name = :stockName")
    List<Transaction> findByUserAndStock(@Param("user") User user, @Param("stockName") String stockName);

}