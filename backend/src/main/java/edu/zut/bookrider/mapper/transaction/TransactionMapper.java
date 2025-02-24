package edu.zut.bookrider.mapper.transaction;

import edu.zut.bookrider.dto.CreateTransactionResponseDTO;
import edu.zut.bookrider.mapper.Mapper;
import edu.zut.bookrider.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper implements Mapper<Transaction, CreateTransactionResponseDTO> {

    @Override
    public CreateTransactionResponseDTO map(Transaction transaction) {

        return new CreateTransactionResponseDTO(
                transaction.getUser().getId(),
                transaction.getOrder() != null ? transaction.getOrder().getId() : null,
                transaction.getAmount(),
                transaction.getTransactionType().toString(),
                transaction.getTransactionDate()
        );
    }
}
