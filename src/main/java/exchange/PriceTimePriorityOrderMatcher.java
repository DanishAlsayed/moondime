package exchange;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static exchange.Side.BUY;
import static exchange.Side.invert;
import static java.lang.Math.min;

/**
 * @author Danish Alsayed
 */
public class PriceTimePriorityOrderMatcher implements OrderMatcher {
    private final OrderBook orderBook;

    public PriceTimePriorityOrderMatcher(OrderBook orderBook) {
        if (orderBook == null) throw new IllegalArgumentException("Order book cannot be null");
        this.orderBook = orderBook;
    }

    @Override
    public List<Trade> matchAndInsertRemaining(Order order) {
        if (orderBook.exists(order.getId())) {
            System.out.println("Duplicate order " + order + " not entertained");
            return new ArrayList<>();
        }

        Side farSide = invert(order.getSide());
        Queue<Integer> farPriceLevels = orderBook.getPriceLevels(farSide);

        return fill(order, farPriceLevels, orderBook.getBook(farSide));
    }

    private List<Trade> fill(final Order order, Queue<Integer> farPriceLevels, final Map<Integer, List<Order>> farBook) {
        List<Trade> trades = new ArrayList<>();
        while (!order.isFilled() && !farPriceLevels.isEmpty()) {
            int farPrice = farPriceLevels.poll();
            if (!checkPrice(farPrice, order.getPrice(), order.getSide())) continue;

            List<Order> farOrders = new ArrayList<>(farBook.get(farPrice));
            for (Order farOrder : farOrders) {
                int execution = min(farOrder.getVolume(), order.getVolume());
                order.fill(execution);
                farOrder.fill(execution);
                Trade trade = new Trade(farOrder.getPrice(), execution, farOrder.getId(), order.getId());
                trades.add(trade);
                if (farOrder.isFilled()) {
                    orderBook.remove(farOrder);
                }
                if (order.isFilled()) break;
            }
        }
        if (!order.isFilled()) orderBook.insert(order);

        return trades;
    }

    private boolean checkPrice(int farPrice, int price, Side side) {
        return side == BUY ? farPrice <= price : farPrice >= price;
    }
}
