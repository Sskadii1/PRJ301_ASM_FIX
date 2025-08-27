package perfumeshop.service;

import perfumeshop.exception.ValidationException;
import perfumeshop.model.Cart;
import perfumeshop.model.Item;
import perfumeshop.model.Product;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service layer for cart business logic
 * @author PerfumeShop Team
 */
public class CartService {

    private static final Logger LOGGER = Logger.getLogger(CartService.class.getName());

    private static final double SHIPPING_FEE = 3.0;
    private static final double TAX_RATE = 0.0; // No tax for now

    /**
     * Add item to cart with validation
     * @param cart Shopping cart
     * @param item Item to add
     * @throws ValidationException if validation fails
     */
    public void addItemToCart(Cart cart, Item item) {
        validateCart(cart);
        validateItem(item);

        cart.addItem(item);
        LOGGER.log(Level.INFO, "Added item to cart: {0} x {1}",
                  new Object[]{item.getProduct().getName(), item.getQuantity()});
    }

    /**
     * Remove item from cart
     * @param cart Shopping cart
     * @param productId Product ID to remove
     * @return true if item was removed
     * @throws ValidationException if cart or productId is invalid
     */
    public boolean removeItemFromCart(Cart cart, int productId) {
        validateCart(cart);

        if (productId <= 0) {
            throw new ValidationException("Invalid product ID: " + productId, "productId");
        }

        boolean removed = cart.removeItem(productId);
        if (removed) {
            LOGGER.log(Level.INFO, "Removed product {0} from cart", productId);
        } else {
            LOGGER.log(Level.WARNING, "Product {0} not found in cart", productId);
        }

        return removed;
    }

    /**
     * Update item quantity in cart
     * @param cart Shopping cart
     * @param productId Product ID
     * @param newQuantity New quantity
     * @return true if updated successfully
     * @throws ValidationException if parameters are invalid
     */
    public boolean updateItemQuantity(Cart cart, int productId, int newQuantity) {
        validateCart(cart);

        if (productId <= 0) {
            throw new ValidationException("Invalid product ID: " + productId, "productId");
        }

        if (newQuantity < 0) {
            throw new ValidationException("Quantity cannot be negative", "quantity");
        }

        boolean updated = cart.updateQuantity(productId, newQuantity);
        if (updated) {
            LOGGER.log(Level.INFO, "Updated quantity for product {0} to {1}",
                      new Object[]{productId, newQuantity});
        } else {
            LOGGER.log(Level.WARNING, "Product {0} not found in cart for quantity update", productId);
        }

        return updated;
    }

    /**
     * Clear all items from cart
     * @param cart Shopping cart
     * @throws ValidationException if cart is null
     */
    public void clearCart(Cart cart) {
        validateCart(cart);
        cart.clear();
        LOGGER.log(Level.INFO, "Cart cleared");
    }

    /**
     * Validate cart for checkout
     * @param cart Shopping cart
     * @throws ValidationException if cart is invalid for checkout
     */
    public void validateCartForCheckout(Cart cart) {
        validateCart(cart);

        if (cart.isEmpty()) {
            throw new ValidationException("Cannot checkout empty cart", "cart");
        }

        // Validate all items in cart
        try {
            cart.validate();
        } catch (IllegalStateException e) {
            throw new ValidationException("Cart contains invalid items: " + e.getMessage(), "cart");
        }

        LOGGER.log(Level.INFO, "Cart validation passed for checkout");
    }

    /**
     * Calculate final total including shipping and tax
     * @param cart Shopping cart
     * @return Final total amount
     */
    public double calculateFinalTotal(Cart cart) {
        validateCart(cart);
        return cart.getFinalTotal();
    }

    /**
     * Check if cart has sufficient stock (placeholder for future implementation)
     * @param cart Shopping cart
     * @return true if all items have sufficient stock
     */
    public boolean hasSufficientStock(Cart cart) {
        validateCart(cart);

        // This would check against inventory in a real implementation
        // For now, assume all items have sufficient stock
        for (Item item : cart.getListItems()) {
            if (item.getQuantity() > 100) { // Arbitrary limit for demo
                LOGGER.log(Level.WARNING, "Large quantity detected for product: {0}",
                          item.getProduct().getName());
                return false;
            }
        }

        return true;
    }

    /**
     * Get cart summary information
     * @param cart Shopping cart
     * @return Cart summary as string
     */
    public String getCartSummary(Cart cart) {
        validateCart(cart);

        StringBuilder summary = new StringBuilder();
        summary.append(String.format("Cart Summary:\n"));
        summary.append(String.format("- Total Items: %d\n", cart.getProductCount()));
        summary.append(String.format("- Total Quantity: %d\n", cart.getTotalQuantity()));
        summary.append(String.format("- Subtotal: $%.2f\n", cart.getTotalMoney()));
        summary.append(String.format("- Shipping: $%.2f\n", SHIPPING_FEE));
        summary.append(String.format("- Final Total: $%.2f\n", cart.getFinalTotal()));

        return summary.toString();
    }

    /**
     * Merge two carts (useful for logged-in users)
     * @param targetCart Cart to merge into
     * @param sourceCart Cart to merge from
     */
    public void mergeCarts(Cart targetCart, Cart sourceCart) {
        if (targetCart == null) {
            throw new ValidationException("Target cart cannot be null", "targetCart");
        }
        if (sourceCart == null) {
            return; // Nothing to merge
        }

        for (Item item : sourceCart.getListItems()) {
            targetCart.addItem(item);
        }

        LOGGER.log(Level.INFO, "Merged {0} items from source cart to target cart",
                  sourceCart.getProductCount());
    }

    /**
     * Check if cart contains a specific product
     * @param cart Shopping cart
     * @param productId Product ID
     * @return true if cart contains the product
     */
    public boolean containsProduct(Cart cart, int productId) {
        validateCart(cart);

        if (productId <= 0) {
            throw new ValidationException("Invalid product ID: " + productId, "productId");
        }

        return cart.containsProduct(productId);
    }

    /**
     * Get items that are on sale
     * @param cart Shopping cart
     * @return List of items that have discount
     */
    public List<Item> getSaleItems(Cart cart) {
        validateCart(cart);

        return cart.getListItems().stream()
                   .filter(item -> item.getProduct().getDiscount() > 0)
                   .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Calculate total savings from discounts
     * @param cart Shopping cart
     * @return Total savings amount
     */
    public double calculateTotalSavings(Cart cart) {
        validateCart(cart);
        return cart.getTotalDiscount();
    }

    // Private validation methods
    private void validateCart(Cart cart) {
        if (cart == null) {
            throw new ValidationException("Cart cannot be null", "cart");
        }
    }

    private void validateItem(Item item) {
        if (item == null) {
            throw new ValidationException("Item cannot be null", "item");
        }
        if (item.getProduct() == null) {
            throw new ValidationException("Item product cannot be null", "item");
        }
        if (item.getQuantity() <= 0) {
            throw new ValidationException("Item quantity must be positive", "item");
        }
        if (item.getPrice() < 0) {
            throw new ValidationException("Item price cannot be negative", "item");
        }
    }
}
