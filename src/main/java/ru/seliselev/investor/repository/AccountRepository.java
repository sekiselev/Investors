package ru.seliselev.investor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.seliselev.investor.entity.Account;

import java.util.List;
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByUserId(Long userId);

}