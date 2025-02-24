package edu.zut.bookrider.mapper.quote;

import edu.zut.bookrider.dto.BookResponseDto;
import edu.zut.bookrider.dto.QuoteOptionResponseDTO;
import edu.zut.bookrider.dto.QuoteResponseDTO;
import edu.zut.bookrider.mapper.Mapper;
import edu.zut.bookrider.mapper.book.BookReadMapper;
import edu.zut.bookrider.model.Quote;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class QuoteMapper implements Mapper<Quote, QuoteResponseDTO> {

    private final BookReadMapper bookReadMapper;
    private final QuoteOptionMapper quoteOptionMapper;

    @Override
    public QuoteResponseDTO map(Quote quote) {

        BookResponseDto bookResponseDto = bookReadMapper.map(quote.getBook());

        List<QuoteOptionResponseDTO> optionDtos = quote.getOptions().stream()
                .map(quoteOptionMapper::map)
                .toList();

        return new QuoteResponseDTO(
                quote.getValidUntil(),
                bookResponseDto,
                quote.getQuantity(),
                optionDtos
        );
    }
}
