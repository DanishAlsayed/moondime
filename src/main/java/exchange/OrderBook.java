package exchange;

import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * @author Danish Alsayed
 */
public interface OrderBook {
    boolean insert(Order order);

    boolean remove(Order order);

    Queue<Integer> getPriceLevels(Side side);

    Map<Integer, List<Order>> getBook(Side side);

    boolean exists(String orderId);
}
