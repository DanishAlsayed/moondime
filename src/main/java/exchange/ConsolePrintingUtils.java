package exchange;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;

import static exchange.Side.BUY;
import static exchange.Side.SELL;

/**
 * @author Danish Alsayed
 */
public class ConsolePrintingUtils {
    private static final String DELIMITER = "|";
    private static final String FORMAT = "%1d%10d%20s%20d%20d%n";
    private static final String BUY_FORMAT = "%1d%10d%20s%n";
    private static final String SELL_FORMAT = "%20s%20d%20d%n";

    public void print(final Map<Integer, List<Order>> buyBook,
                      final Map<Integer, List<Order>> sellBook,
                      Function<Side, Queue<Integer>> getPriceLevels) {
        Queue<Integer> buyLevels = getPriceLevels.apply(BUY);
        Queue<Integer> sellLevels = getPriceLevels.apply(SELL);

        while (!buyLevels.isEmpty() && !sellLevels.isEmpty()) {
            int sellPrice = sellLevels.poll();
            int buyPrice = buyLevels.poll();
            List<Order> buyOrders = buyBook.get(buyPrice);
            List<Order> sellOrders = sellBook.get(sellPrice);
            Iterator<Order> buyIt = buyOrders.iterator();
            Iterator<Order> sellIt = sellOrders.iterator();
            while (buyIt.hasNext() && sellIt.hasNext()) {
                Order buy = buyIt.next();
                Order sell = sellIt.next();
                System.out.format(FORMAT, buy.getVolume(), buy.getPrice(), DELIMITER, sell.getPrice(), sell.getVolume());
            }
            if (buyIt.hasNext()) {
                printBook(buyIt, BUY);
            } else {
                printBook(sellIt, SELL);
            }
        }
        if (buyLevels.isEmpty() && sellLevels.isEmpty()) {
            return;
        }
        if (!buyLevels.isEmpty()) {
            while (!buyLevels.isEmpty()) {
                int buyPrice = buyLevels.poll();
                List<Order> buyOrders = buyBook.get(buyPrice);
                for (Order buy : buyOrders) {
                    System.out.format(BUY_FORMAT, buy.getVolume(), buy.getPrice(), DELIMITER);
                }
            }
        }
        if (!sellLevels.isEmpty()) {
            int sellPrice = sellLevels.poll();
            List<Order> sellOrders = sellBook.get(sellPrice);
            for (Order sell : sellOrders) {
                System.out.format(SELL_FORMAT, DELIMITER, sell.getVolume(), sell.getPrice());
            }
        }
    }

    private void printBook(Iterator<Order> iterator, Side side) {
        while (iterator.hasNext()) {
            Order order = iterator.next();
            if (side == BUY) {
                System.out.format(BUY_FORMAT, order.getVolume(), order.getPrice(), DELIMITER);
            } else {
                System.out.format(SELL_FORMAT, DELIMITER, order.getVolume(), order.getPrice());
            }
        }
    }


}
