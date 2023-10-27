package ru.seliselev.investor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String password;

    @OneToOne(mappedBy = "user", cascade = CascadeType.PERSIST)
    private Account account;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Transaction> transactions;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_stocks",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "stock_id"))
    private List<Stock> stocks;

    // Конструкторы, геттеры и сеттеры
}