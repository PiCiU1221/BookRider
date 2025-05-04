package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.Transaction;
import edu.zut.bookrider.model.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    @Query("SELECT t.amount FROM Transaction t WHERE t.order.id = :orderId AND t.transactionType = :transactionType")
    Optional<BigDecimal> findAmountByOrderIdAndTransactionType(@Param("orderId") Integer orderId,
                                                               @Param("transactionType") TransactionType transactionType);

    @Query("""
        SELECT t.amount
        FROM Transaction t
        WHERE t.rentalReturn.id = (
            SELECT r.id
            FROM RentalReturn r
            WHERE r.returnOrder.id = :orderId
        )
        AND t.transactionType = 'LATE_FEE_PAYMENT'
    """)
    Optional<BigDecimal> findLateFeeByOrderId(@Param("orderId") Integer orderId);
}
