package ru.seliselev.investor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.seliselev.investor.enums.TransactionType;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "stock_id")
    private Stock stock;

    private int quantity;
    private BigDecimal purchasePrice;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private Date date;


    public Date getDate() {
        return date;
    }

}
