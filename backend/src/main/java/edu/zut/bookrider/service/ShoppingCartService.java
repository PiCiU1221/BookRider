package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.CoordinateDTO;
import edu.zut.bookrider.dto.CreateAddressDTO;
import edu.zut.bookrider.dto.ShoppingCartResponseDTO;
import edu.zut.bookrider.mapper.shoppingCart.ShoppingCartMapper;
import edu.zut.bookrider.model.*;
import edu.zut.bookrider.repository.ShoppingCartRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShoppingCartService {

    private final QuoteOptionService quoteOptionService;
    private final ShoppingCartRepository shoppingCartRepository;
    private final DeliveryCostCalculatorService deliveryCostCalculatorService;
    private final UserService userService;
    private final AddressService addressService;
    private final DistanceService distanceService;
    private final ShoppingCartMapper shoppingCartMapper;

    @Transactional
    public ShoppingCartResponseDTO addQuoteOptionToCart(Integer quoteOptionId, Authentication authentication) {

        User user = userService.getUser(authentication);

        ShoppingCart cart = user.getShoppingCart();

        QuoteOption quoteOption = quoteOptionService.getQuoteOptionById(quoteOptionId);

        if (quoteOption.getQuote().getValidUntil().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Quote option has expired");
        }

        Optional<ShoppingCartItem> existingCartItem = cart.getItems().stream()
                .filter(item -> item.getLibrary().getId().equals(quoteOption.getLibrary().getId()))
                .findFirst();

        if (existingCartItem.isPresent()) {
            ShoppingCartItem cartItem = existingCartItem.get();

            Optional<ShoppingCartSubItem> existingSubItem = cartItem.getBooks().stream()
                    .filter(subItem -> subItem.getBook().getId().equals(quoteOption.getQuote().getBook().getId()))
                    .findFirst();

            if (existingSubItem.isPresent()) {
                ShoppingCartSubItem subItem = existingSubItem.get();
                subItem.setQuantity(subItem.getQuantity() + quoteOption.getQuote().getQuantity());
                cartItem.setTotalItemDeliveryCost(calculateTotalItemDeliveryCost(cartItem));
            } else {
                ShoppingCartSubItem newSubItem = new ShoppingCartSubItem();
                newSubItem.setBook(quoteOption.getQuote().getBook());
                newSubItem.setQuantity(quoteOption.getQuote().getQuantity());
                newSubItem.setShoppingCartItem(cartItem);
                cartItem.getBooks().add(newSubItem);
                cartItem.setTotalItemDeliveryCost(calculateTotalItemDeliveryCost(cartItem));
            }

        } else {
            ShoppingCartItem newCartItem = new ShoppingCartItem();
            newCartItem.setShoppingCart(cart);
            newCartItem.setLibrary(quoteOption.getLibrary());
            newCartItem.setTotalItemDeliveryCost(quoteOption.getTotalDeliveryCost());

            ShoppingCartSubItem newSubItem = new ShoppingCartSubItem();
            newSubItem.setBook(quoteOption.getQuote().getBook());
            newSubItem.setQuantity(quoteOption.getQuote().getQuantity());
            newSubItem.setShoppingCartItem(newCartItem);

            newCartItem.getBooks().add(newSubItem);
            cart.getItems().add(newCartItem);
        }

        cart.setTotalCartDeliveryCost(calculateTotalCartDeliveryCost(cart));

        ShoppingCart savedShoppingCart = shoppingCartRepository.save(cart);
        return shoppingCartMapper.map(savedShoppingCart);
    }

    @Transactional
    public ShoppingCartResponseDTO deleteShoppingCartSubItem(Integer subItemId, Authentication authentication) {

        User user = userService.getUser(authentication);

        ShoppingCart cart = user.getShoppingCart();

        ShoppingCartItem parentItem = cart.getItems().stream()
                .filter(item -> item.getBooks().stream()
                        .anyMatch(subItem -> subItem.getId().equals(subItemId)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("SubItem not found in the cart"));

        ShoppingCartSubItem subItemToDelete = parentItem.getBooks().stream()
                .filter(subItem -> subItem.getId().equals(subItemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("SubItem not found"));

        parentItem.getBooks().remove(subItemToDelete);

        if (parentItem.getBooks().isEmpty()) {
            cart.getItems().remove(parentItem);
        } else {
            parentItem.setTotalItemDeliveryCost(calculateTotalItemDeliveryCost(parentItem));
        }

        cart.setTotalCartDeliveryCost(calculateTotalCartDeliveryCost(cart));

        ShoppingCart savedShoppingCart = shoppingCartRepository.save(cart);
        return shoppingCartMapper.map(savedShoppingCart);
    }

    private BigDecimal calculateTotalItemDeliveryCost(ShoppingCartItem cartItem) {

        Address itemLibraryAddress = cartItem.getLibrary().getAddress();
        double libraryLatitude = itemLibraryAddress.getLatitude().doubleValue();
        double libraryLongitude = itemLibraryAddress.getLongitude().doubleValue();
        CoordinateDTO startCoordinates = new CoordinateDTO(libraryLatitude, libraryLongitude);

        Address targetAddress = cartItem.getShoppingCart().getDeliveryAddress();
        double targetLatitude = targetAddress.getLatitude().doubleValue();
        double targetLongitude = targetAddress.getLongitude().doubleValue();
        CoordinateDTO endCoordinates = new CoordinateDTO(targetLatitude, targetLongitude);

        BigDecimal distance = distanceService.getDistance(startCoordinates, endCoordinates);

        int quantity = 0;
        for (ShoppingCartSubItem subItem : cartItem.getBooks()) {
            quantity += subItem.getQuantity();
        }

        return deliveryCostCalculatorService.calculateCost(distance, quantity, false);
    }

    private BigDecimal calculateTotalCartDeliveryCost(ShoppingCart cart) {
        BigDecimal totalDeliveryCost = BigDecimal.ZERO;

        for (ShoppingCartItem item : cart.getItems()) {
            totalDeliveryCost = totalDeliveryCost.add(item.getTotalItemDeliveryCost());
        }

        return totalDeliveryCost;
    }

    public ShoppingCartResponseDTO setDeliveryAddress(
            @Valid CreateAddressDTO createAddressDTO,
            Authentication authentication ) {

        User user = userService.getUser(authentication);

        ShoppingCart shoppingCart = user.getShoppingCart();

        Address addressToSet = addressService.findExistingAddress(createAddressDTO)
                .orElseGet(() -> addressService.createAddress(createAddressDTO));

        shoppingCart.setDeliveryAddress(addressToSet);

        ShoppingCart savedShoppingCart = shoppingCartRepository.save(shoppingCart);
        return shoppingCartMapper.map(savedShoppingCart);
    }

    @Transactional
    public ShoppingCartResponseDTO getShoppingCart(Authentication authentication) {

        User user = userService.getUser(authentication);
        ShoppingCart shoppingCart = user.getShoppingCart();

        return shoppingCartMapper.map(shoppingCart);
    }

    @Transactional
    public void removeAllItemsFromCart(Authentication authentication) {

        User user = userService.getUser(authentication);
        ShoppingCart cart = user.getShoppingCart();

        cart.getItems().clear();

        cart.setTotalCartDeliveryCost(BigDecimal.ZERO);

        shoppingCartRepository.save(cart);
    }
}
