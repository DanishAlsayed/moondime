package exchange;

/**
 * @author Danish Alsayed
 */
public class Trade {
    private static final String COMMA = ",";

    private final int price;
    private final int volume;
    private final String restingOrderId;
    private final String crossingOrderId;

    public Trade(int price, int volume, String restingOrderId, String crossingOrderId) {
        validateInput(restingOrderId, crossingOrderId, volume, price);

        this.price = price;
        this.volume = volume;
        this.restingOrderId = restingOrderId;
        this.crossingOrderId = crossingOrderId;
    }

    public int getPrice() {
        return price;
    }

    public int getVolume() {
        return volume;
    }

    public String getRestingOrderId() {
        return restingOrderId;
    }

    public String getCrossingOrderId() {
        return crossingOrderId;
    }

    private void validateInput(final String restingOrderId, final String crossingOrderId, int volume, int price) {
        if (restingOrderId == null || restingOrderId.isEmpty() || crossingOrderId == null || crossingOrderId.isEmpty()) {
            throw new IllegalArgumentException("Matching order ids cannot be null or empty");
        }

        if (volume <= 0 || price <= 0) {
            throw new IllegalArgumentException("Price or Volume cannot be null for a trade");
        }
    }

    @Override
    public String toString() {
        return "trade " +
                crossingOrderId +
                COMMA +
                restingOrderId +
                COMMA +
                price +
                COMMA +
                volume;
    }
}
