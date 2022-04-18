package exchange;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Queue;

import static exchange.Side.BUY;
import static exchange.Side.SELL;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Danish Alsayed
 */
public class TradingOrderBookTest {
    private OrderBook orderBook;
    private final Order order1 = new Order("1", 1, 1, BUY);

    @BeforeEach
    void setup() {
        orderBook = new TradingOrderBook();
    }

    @Test
    void insertion() {
        assertThat(orderBook.insert(order1)).isTrue();
        assertThat(orderBook.insert(order1)).isFalse();
        assertThat(orderBook.exists(order1.getId())).isTrue();
        assertThat(orderBook.insert(null)).isFalse();
        assertThat(orderBook.getBook(BUY).size()).isEqualTo(1);
    }

    @Test
    void deletion() {
        assertThat(orderBook.insert(order1)).isTrue();
        assertThat(orderBook.exists(order1.getId())).isTrue();
        assertThat(orderBook.remove(order1)).isTrue();
        assertThat(orderBook.exists(order1.getId())).isFalse();
        assertThat(orderBook.remove(order1)).isFalse();
        assertThat(orderBook.remove(null)).isFalse();
        assertThat(orderBook.getBook(BUY).size()).isEqualTo(0);
    }

    @Test
    void priceLevelsBuy() {
        Order order2 = new Order("2", 1, 2, BUY);
        Order order3 = new Order("3", 1, 3, BUY);
        assertThat(orderBook.insert(order1)).isTrue();
        assertThat(orderBook.insert(order2)).isTrue();
        assertThat(orderBook.insert(order3)).isTrue();

        Queue<Integer> result = orderBook.getPriceLevels(BUY);
        assertThat(result.size()).isEqualTo(3);

        assertThat(result.poll()).isEqualTo(3);
        assertThat(result.poll()).isEqualTo(2);
        assertThat(result.poll()).isEqualTo(1);
        assertThat(orderBook.getBook(BUY).size()).isEqualTo(3);
    }

    @Test
    void priceLevelsSell() {
        Order order1 = new Order("1", 1, 1, SELL);
        Order order2 = new Order("2", 1, 2, SELL);
        Order order3 = new Order("3", 1, 3, SELL);
        assertThat(orderBook.insert(order1)).isTrue();
        assertThat(orderBook.insert(order2)).isTrue();
        assertThat(orderBook.insert(order3)).isTrue();

        Queue<Integer> result = orderBook.getPriceLevels(SELL);
        assertThat(result.size()).isEqualTo(3);

        assertThat(result.poll()).isEqualTo(1);
        assertThat(result.poll()).isEqualTo(2);
        assertThat(result.poll()).isEqualTo(3);
        assertThat(orderBook.getBook(SELL).size()).isEqualTo(3);
    }

    @Test
    void icebergOrders() {
        for (Side side : asList(BUY, SELL)) {
            int displaySize = 140;
            int price = 2;
            Order order = new Order(side.toString(), 300, price, side, displaySize);
            assertThat(orderBook.insert(order)).isTrue();
            Map<Integer, List<Order>> book = orderBook.getBook(side);
            assertThat(book.size()).isEqualTo(1);
            List<Order> splits = book.get(price);
            assertThat(splits.size()).isEqualTo(1);
            Order split = splits.get(0);
            assertThat(split.getVolume()).isEqualTo(displaySize);
            assertThat(split.getPrice()).isEqualTo(price);
            assertThat(split.getSide()).isEqualTo(side);
            assertThat(split.isIceberg()).isFalse();

            assertThat(orderBook.remove(split)).isTrue();
            Order nextSplit = validateIcebergSplit(side, price, 140);
            assertThat(orderBook.remove(nextSplit)).isTrue();
            nextSplit = validateIcebergSplit(side, price, 20);
            assertThat(orderBook.remove(nextSplit)).isTrue();
            splits = book.get(price);
            assertThat(splits).isNull();
        }
    }

    private Order validateIcebergSplit(Side side, int price, int displaySize) {
        Map<Integer, List<Order>> book = orderBook.getBook(side);
        assertThat(book.size()).isEqualTo(1);
        List<Order> splits = book.get(price);
        assertThat(splits.size()).isEqualTo(1);
        Order split = splits.get(0);
        assertThat(split.getVolume()).isEqualTo(displaySize);
        assertThat(split.getPrice()).isEqualTo(price);
        assertThat(split.getSide()).isEqualTo(side);
        assertThat(split.isIceberg()).isFalse();
        return split;
    }

}
