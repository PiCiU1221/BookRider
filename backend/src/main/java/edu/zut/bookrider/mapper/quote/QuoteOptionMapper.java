package edu.zut.bookrider.mapper.quote;

import edu.zut.bookrider.dto.QuoteOptionResponseDTO;
import edu.zut.bookrider.mapper.Mapper;
import edu.zut.bookrider.model.QuoteOption;
import org.springframework.stereotype.Component;

@Component
public class QuoteOptionMapper implements Mapper<QuoteOption, QuoteOptionResponseDTO> {

    @Override
    public QuoteOptionResponseDTO map(QuoteOption quoteOption) {

        return new QuoteOptionResponseDTO(
                quoteOption.getId(),
                quoteOption.getLibraryName(),
                quoteOption.getDistanceKm(),
                quoteOption.getTotalDeliveryCost()
        );
    }
}
