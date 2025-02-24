package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.QuoteResponseDTO;
import edu.zut.bookrider.exception.LibraryNotFoundException;
import edu.zut.bookrider.exception.MissingAddressException;
import edu.zut.bookrider.mapper.quote.QuoteMapper;
import edu.zut.bookrider.model.*;
import edu.zut.bookrider.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final LibraryService libraryService;
    private final BookService bookService;
    private final UserService userService;
    private final QuoteOptionService quoteOptionService;
    private final QuoteMapper quoteMapper;

    public QuoteResponseDTO generateQuote(Integer bookId,
                                          int quantity,
                                          Authentication authentication) {

        User user = userService.getUser(authentication);

        if (bookId == null || quantity <= 0) {
            throw new IllegalArgumentException("Invalid book ID or quantity");
        }

        Book book = bookService.getBookById(bookId);

        ShoppingCart cart = user.getShoppingCart();

        Address deliveryAddress = cart.getDeliveryAddress();
        if (deliveryAddress == null) {
            throw new MissingAddressException("No delivery address set for the user");
        }

        BigDecimal userLatitude = deliveryAddress.getLatitude();
        BigDecimal userLongitude = deliveryAddress.getLongitude();

        List<Library> libraries = libraryService.getNearestLibrariesWithBookLimit5(bookId, userLatitude, userLongitude);
        if (libraries.isEmpty()) {
            throw new LibraryNotFoundException("No libraries found with the requested book");
        }

        List<QuoteOption> options = new ArrayList<>();

        for (Library library : libraries) {
            boolean isLibraryInCart = isLibraryInCart(library, cart, bookId);

            QuoteOption option = quoteOptionService.createQuoteOption(
                    library,
                    quantity,
                    userLatitude.doubleValue(),
                    userLongitude.doubleValue(),
                    isLibraryInCart
            );

            options.add(option);
        }

        options.sort(Comparator.comparing(QuoteOption::getTotalDeliveryCost));

        Quote quote = new Quote(
                LocalDateTime.now().plusMinutes(15),
                book,
                quantity,
                options
        );

        for (QuoteOption option : options) {
            option.setQuote(quote);
        }

        Quote savedQuote = quoteRepository.save(quote);

        return quoteMapper.map(savedQuote);
    }

    private boolean isLibraryInCart(Library library, ShoppingCart cart, Integer bookId) {
        return cart.getItems().stream()
                .anyMatch(item -> item.getLibrary().getId().equals(library.getId()) &&
                        item.getBooks().stream().anyMatch(subItem -> subItem.getBook().getId().equals(bookId)));
    }
}
