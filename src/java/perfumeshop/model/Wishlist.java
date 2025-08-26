package perfumeshop.model;

import java.util.ArrayList;
import java.util.List;

public class Wishlist {

    private List<Item> listItems;

    public Wishlist() {
        listItems = new ArrayList<>();
    }

    public Wishlist(List<Item> listItems) {
        this.listItems = listItems;
    }

    public List<Item> getListItems() {
        return listItems;
    }

    public void setListItems(List<Item> listItems) {
        this.listItems = listItems;
    }

    private Item getItemByID(int id) {
        for (Item item : listItems) {
            if (item.getProduct().getId() == id) {
                return item;
            }
        }
        return null;
    }

    public void addItem(Item t) {
        if (getItemByID(t.getProduct().getId()) == null) {
            listItems.add(t);
        }
    }

    public void removeItem(int id) {
        if (getItemByID(id) != null) {
            listItems.remove(getItemByID(id));
        }
    }
    
    public int countItems() {
        return listItems.size();
    }
}
