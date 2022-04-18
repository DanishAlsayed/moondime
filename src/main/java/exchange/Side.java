package exchange;

/**
 * @author Danish Alsayed
 */
public enum Side {
    BUY("B"),
    SELL("S");

    private final String side;

    Side(String s) {
        this.side = s;
    }

    public String getValue() {
        return side;
    }

    @Override
    public String toString() {
        return this.getValue();
    }

    public static Side getSide(String value) {
        for (Side v : values())
            if (v.getValue().equalsIgnoreCase(value)) return v;
        throw new IllegalArgumentException("No such side");
    }

    public static Side invert(Side side) {
        return side == BUY ? SELL : BUY;
    }
}

