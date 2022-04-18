package exchange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import static exchange.Side.BUY;
import static java.util.Collections.reverseOrder;

/**
 * @author Danish Alsayed
 */
public class TradingOrderBook implements OrderBook {
    private final Set<String> lookBook;
    private final Map<Integer, List<Order>> buyBook;
    private final Map<Integer, List<Order>> sellBook;
    private final Map<String, List<Order>> icebergOrders;

    public TradingOrderBook() {
        this.lookBook = new HashSet<>();
        this.buyBook = new HashMap<>();
        this.sellBook = new HashMap<>();
        this.icebergOrders = new HashMap<>();
    }

    @Override
    public boolean insert(Order order) {
        if (order == null) {
            System.out.println("Received null order, will not insert");
            return false;
        }
        String id = order.getId();
        if (lookBook.add(id)) {
            if (order.isIceberg()) {
                List<Order> splits = splitOrder(order);
                pollAndInsertSplit(splits);
                if (!splits.isEmpty()) {
                    icebergOrders.put(id, splits);
                }
                return true;
            }
            return put(order, getBook(order.getSide()));
        }
        System.out.println("Duplicate order not inserted:" + id);
        return false;
    }

    @Override
    public boolean remove(Order order) {
        if (order == null) {
            System.out.println("Received order is either null or not filled, will not remove");
            return false;
        }

        String id = order.getId();
        if (lookBook.remove(id)) {
            boolean result = delete(order, getBook(order.getSide()));
            List<Order> splits = icebergOrders.get(id);
            if (splits != null) {
                if (!splits.isEmpty()) {
                    pollAndInsertSplit(splits);
                    lookBook.add(order.getId());
                } else {
                    icebergOrders.remove(id);
                }
            }

            return result;
        }
        System.out.println("Order with id " + id + " not found, removal failed.");
        return false;
    }

    @Override
    public Queue<Integer> getPriceLevels(Side side) {
        Map<Integer, List<Order>> book = getBook(side);

        Set<Integer> prices = book.keySet();
        Queue<Integer> levels;
        int size = prices.size();
        if (size == 0) {
            return new PriorityQueue<>();
        }
        if (side == BUY) {
            levels = new PriorityQueue<>(size, reverseOrder());
        } else {
            levels = new PriorityQueue<>(size);
        }

        levels.addAll(prices);
        return levels;
    }

    @Override
    public Map<Integer, List<Order>> getBook(Side side) {
        return side == BUY ? buyBook : sellBook;
    }

    @Override
    public boolean exists(String orderId) {
        return lookBook.contains(orderId);
    }

    private List<Order> splitOrder(Order order) {
        int displaySize = order.getDisplaySize();
        int volume = order.getVolume();
        int numOfSplits = volume / displaySize;
        int excessVolume = volume % displaySize;

        List<Order> splits = new ArrayList<>(numOfSplits);

        String id = order.getId();
        int price = order.getPrice();
        Side side = order.getSide();
        for (int i = 0; i < numOfSplits; i++) {
            splits.add(new Order(id, displaySize, price, side));
        }

        if (excessVolume > 0) {
            splits.add(new Order(id, excessVolume, price, side));
        }

        return splits;
    }

    private void pollAndInsertSplit(List<Order> splits) {
        Order order = splits.get(0);
        put(order, getBook(order.getSide()));
        splits.remove(order);
    }

    private boolean put(Order order, Map<Integer, List<Order>> book) {
        List<Order> orders = book.computeIfAbsent(order.getPrice(), k -> new ArrayList<>());
        return orders.add(order);
    }

    private boolean delete(Order order, Map<Integer, List<Order>> book) {
        boolean result = false;
        List<Order> orders = book.get(order.getPrice());
        if (orders != null) {
            result = orders.remove(order);
            if (orders.isEmpty()) book.remove(order.getPrice());
        }
        return result;
    }
}
