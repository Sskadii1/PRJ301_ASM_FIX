package perfumeshop.model;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Shopping cart entity managing items and calculations
 * @author PerfumeShop Team
 */
public class Cart {

    private List<Item> listItems;

    // Constructors
    public Cart() {
        this.listItems = new ArrayList<>();
    }

    public Cart(List<Item> listItems) {
        this.listItems = listItems != null ? new ArrayList<>(listItems) : new ArrayList<>();
    }

    // Getters and Setters
    public List<Item> getListItems() {
        return new ArrayList<>(listItems); // Return defensive copy
    }

    public void setListItems(List<Item> listItems) {
        this.listItems = listItems != null ? new ArrayList<>(listItems) : new ArrayList<>();
    }

    // Core business methods
    /**
     * Find item by product ID
     * @param productId Product ID to search
     * @return Item if found, null otherwise
     */
    private Item getItemByID(int productId) {
        if (productId <= 0) {
            throw new IllegalArgumentException("Product ID must be positive");
        }

        return listItems.stream()
                       .filter(item -> item.getProduct().getId() == productId)
                       .findFirst()
                       .orElse(null);
    }

    /**
     * Get quantity of a product in cart
     * @param productId Product ID
     * @return Quantity or 0 if not found
     */
    public int getQuantityByID(int productId) {
        Item item = getItemByID(productId);
        return item != null ? item.getQuantity() : 0;
    }

    /**
     * Add item to cart
     * @param item Item to add
     * @throws IllegalArgumentException if item is null or invalid
     */
    public void addItem(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        if (item.getProduct() == null) {
            throw new IllegalArgumentException("Item product cannot be null");
        }
        if (item.getQuantity() <= 0) {
            throw new IllegalArgumentException("Item quantity must be positive");
        }

        Item existingItem = getItemByID(item.getProduct().getId());
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
        } else {
            listItems.add(item);
        }
    }

    /**
     * Remove item from cart
     * @param productId Product ID to remove
     * @return true if item was removed, false if not found
     */
    public boolean removeItem(int productId) {
        return listItems.removeIf(item -> item.getProduct().getId() == productId);
    }

    /**
     * Update quantity of a product
     * @param productId Product ID
     * @param newQuantity New quantity
     * @return true if updated successfully, false if product not found
     */
    public boolean updateQuantity(int productId, int newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        Item item = getItemByID(productId);
        if (item != null) {
            if (newQuantity == 0) {
                return removeItem(productId);
            } else {
                item.setQuantity(newQuantity);
                return true;
            }
        }
        return false;
    }

    /**
     * Clear all items from cart
     */
    public void clear() {
        listItems.clear();
    }

    // Price calculation methods
    /**
     * Get total price without discount
     * @return Total price
     */
    public double getTotalPriceWithoutDiscount() {
        return listItems.stream()
                       .mapToDouble(item -> item.getQuantity() * item.getProduct().getPrice())
                       .sum();
    }

    /**
     * Get total money with discount applied
     * @return Total money after discount
     */
    public double getTotalMoney() {
        return listItems.stream()
                       .mapToDouble(item -> item.getQuantity() * item.getPrice())
                       .sum();
    }

    /**
     * Get total discount amount
     * @return Total discount
     */
    public double getTotalDiscount() {
        return getTotalPriceWithoutDiscount() - getTotalMoney();
    }

    /**
     * Get shipping fee (fixed at $3)
     * @return Shipping fee
     */
    public double getShippingFee() {
        return isEmpty() ? 0 : 3.0;
    }

    /**
     * Get final total including shipping
     * @return Final total
     */
    public double getFinalTotal() {
        return getTotalMoney() + getShippingFee();
    }

    // Product list methods
    /**
     * Get list of products in cart
     * @return List of products
     */
    public List<Product> getProducts() {
        return listItems.stream()
                       .map(Item::getProduct)
                       .collect(Collectors.toList());
    }

    // Utility methods
    /**
     * Check if cart is empty
     * @return true if empty
     */
    public boolean isEmpty() {
        return listItems.isEmpty();
    }

    /**
     * Get total number of items
     * @return Total quantity of all items
     */
    public int getTotalQuantity() {
        return listItems.stream()
                       .mapToInt(Item::getQuantity)
                       .sum();
    }

    /**
     * Get number of different products
     * @return Number of unique products
     */
    public int getProductCount() {
        return listItems.size();
    }

    /**
     * Check if cart contains a specific product
     * @param productId Product ID
     * @return true if contains
     */
    public boolean containsProduct(int productId) {
        return getItemByID(productId) != null;
    }

    // Formatting methods
    public String getFormattedTotalMoney() {
        return String.format("$%.2f", getTotalMoney());
    }

    public String getFormattedFinalTotal() {
        return String.format("$%.2f", getFinalTotal());
    }

    public String getFormattedDiscount() {
        return String.format("$%.2f", getTotalDiscount());
    }

    // Validation methods
    /**
     * Validate cart contents
     * @throws IllegalStateException if cart contains invalid items
     */
    public void validate() {
        for (Item item : listItems) {
            if (item.getQuantity() <= 0) {
                throw new IllegalStateException("Cart contains item with invalid quantity: " + item.getProduct().getName());
            }
            if (item.getPrice() < 0) {
                throw new IllegalStateException("Cart contains item with negative price: " + item.getProduct().getName());
            }
        }
    }

    @Override
    public String toString() {
        return String.format("Cart{items=%d, totalQuantity=%d, totalMoney=%.2f, finalTotal=%.2f}",
                           listItems.size(), getTotalQuantity(), getTotalMoney(), getFinalTotal());
    }
}
