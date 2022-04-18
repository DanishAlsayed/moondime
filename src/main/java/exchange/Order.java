package exchange;

import static java.util.Objects.hash;

/**
 * @author Danish Alsayed
 */
public class Order {
    private final String id;
    private int volume;
    private final int price;
    private final Side side;
    private final boolean isIceberg;
    private final int displaySize;

    public Order(final String id, int volume, int price, Side side) {
        validateInput(id, volume, price);
        this.id = id;
        this.volume = volume;
        this.price = price;
        this.side = side;
        this.isIceberg = false;
        this.displaySize = -1;
    }

    public Order(final String id, int volume, int price, Side side, int displaySize) {
        validateInput(id, volume, price, displaySize);
        this.id = id;
        this.volume = volume;
        this.price = price;
        this.side = side;
        this.isIceberg = true;
        this.displaySize = displaySize;
    }

    private void validateInput(final String id, int volume, int price, int displaySize) {
        validateInput(id, volume, price);
        if (displaySize <= 0) {
            throw new IllegalArgumentException("displaySize must be >= 0");
        }
    }

    private void validateInput(final String id, int volume, int price) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Order id cannot be null or empty");
        }

        if (volume <= 0 || price <= 0) {
            throw new IllegalArgumentException("Price or Volume cannot be null for an order");
        }
    }

    public void fill(int volume) {
        if (volume <= 0 || volume > this.volume) {
            System.out.println("Volume to be filled cannot be <=0 or more than the existing volume: " + volume + " vs. " + this.volume);
            return;
        }
        this.volume -= volume;
    }

    public boolean isFilled() {
        return this.volume == 0;
    }

    public String getId() {
        return id;
    }

    public int getVolume() {
        return volume;
    }

    public int getPrice() {
        return price;
    }

    public Side getSide() {
        return side;
    }

    public boolean isIceberg() {
        return isIceberg;
    }

    public int getDisplaySize() {
        return displaySize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id.equals(order.id) &&
                volume == order.volume &&
                price == order.price &&
                side == order.side;
    }

    @Override
    public int hashCode() {
        return hash(id, volume, price, side);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", volume=" + volume +
                ", price=" + price +
                ", side=" + side +
                '}';
    }
}
