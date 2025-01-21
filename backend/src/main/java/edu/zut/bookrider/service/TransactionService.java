package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.CreateTransactionResponseDTO;
import edu.zut.bookrider.mapper.transaction.TransactionMapper;
import edu.zut.bookrider.model.Order;
import edu.zut.bookrider.model.Transaction;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.model.enums.OrderStatus;
import edu.zut.bookrider.model.enums.TransactionType;
import edu.zut.bookrider.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
public class TransactionService {

    public static final BigDecimal SERVICE_FEE_PERCENTAGE = new BigDecimal("0.20");

    private final UserService userService;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    // Mocking real payment (now, you can just deposit how much you want)
    @Transactional
    public CreateTransactionResponseDTO createUserDepositTransaction(
            BigDecimal amount,
            Authentication authentication) {

        User user = userService.getUser(authentication);

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionType.USER_DEPOSIT);

        Transaction savedTransaction = transactionRepository.save(transaction);

        userService.adjustBalance(
                savedTransaction.getUser(),
                savedTransaction.getAmount(),
                true);

        return transactionMapper.map(savedTransaction);
    }

    @Transactional
    public void createUserPaymentTransaction(
            User user,
            Order order) {

        BigDecimal amount = order.getAmount();

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionType.USER_PAYMENT);

        Transaction savedTransaction = transactionRepository.save(transaction);

        userService.adjustBalance(
                savedTransaction.getUser(),
                savedTransaction.getAmount(),
                false);

        transactionMapper.map(savedTransaction);
    }

    @Transactional
    public CreateTransactionResponseDTO createDriverPayoutTransaction(
            User driver,
            Order order) {
        if (order == null || order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("The order must be delivered to process the payout.");
        }

        BigDecimal totalAmount = order.getAmount();
        BigDecimal serviceFee = totalAmount.multiply(SERVICE_FEE_PERCENTAGE);
        BigDecimal driverPayout = totalAmount.subtract(serviceFee);

        Transaction transaction = new Transaction();
        transaction.setUser(driver);
        transaction.setOrder(order);
        transaction.setAmount(driverPayout);
        transaction.setTransactionType(TransactionType.DRIVER_PAYOUT);

        Transaction savedTransaction = transactionRepository.save(transaction);

        userService.adjustBalance(
                driver,
                savedTransaction.getAmount(),
                true);

        return transactionMapper.map(savedTransaction);
    }

    public BigDecimal getTransactionAmountByOrderIdAndType(Integer orderId, TransactionType transactionType) {
        return transactionRepository.findAmountByOrderIdAndTransactionType(orderId, transactionType)
                .orElse(BigDecimal.ZERO);
    }
}
