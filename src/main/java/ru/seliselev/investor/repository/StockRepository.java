package ru.seliselev.investor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.seliselev.investor.entity.Stock;
@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    Stock findBySymbol(String symbol);

    Stock findByName(String stockName);
}