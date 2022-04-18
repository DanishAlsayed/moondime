package exchange;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import static exchange.Side.BUY;
import static exchange.Side.SELL;
import static java.util.Collections.reverseOrder;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author Danish Alsayed
 */
@ExtendWith(MockitoExtension.class)
public class PriceTimePriorityOrderMatcherTest {
    @Mock
    private TradingOrderBook orderBook;
    private OrderMatcher orderMatcher;

    private HashMap<Integer, List<Order>> farBook = new HashMap<>();
    private PriorityQueue<Integer> priceLevels = new PriorityQueue<>();

    @BeforeEach
    void setup() {
        when(orderBook.exists(anyString())).thenReturn(false);
        when(orderBook.remove(any())).thenReturn(true);
        when(orderBook.insert(any())).thenReturn(true);
        when(orderBook.getPriceLevels(any())).thenReturn(priceLevels);
        when(orderBook.getBook(any())).thenReturn(farBook);

        orderMatcher = new PriceTimePriorityOrderMatcher(orderBook);
    }

    @Test
    void singleMatch() {
        Order order1 = new Order("1", 1000, 10, BUY);

        List<Trade> trades = orderMatcher.matchAndInsertRemaining(order1);
        assertThat(trades.isEmpty()).isTrue();

        Order order2 = new Order("2", 1200, 9, SELL);
        priceLevels = new PriorityQueue<>();
        priceLevels.offer(10);
        farBook = new HashMap<>();
        farBook.put(10, singletonList(order1));
        when(orderBook.getPriceLevels(any())).thenReturn(priceLevels);
        when(orderBook.getBook(any())).thenReturn(farBook);
        trades = orderMatcher.matchAndInsertRemaining(order2);
        assertThat(trades.size()).isEqualTo(1);
        validateTrade(trades.get(0), "2", "1", 10, 1000);

        priceLevels = new PriorityQueue<>();
        priceLevels.offer(9);
        farBook = new HashMap<>();
        farBook.put(9, singletonList(order2));
        when(orderBook.getPriceLevels(any())).thenReturn(priceLevels);
        when(orderBook.getBook(any())).thenReturn(farBook);
        trades = orderMatcher.matchAndInsertRemaining(new Order("3", 100, 11, BUY));
        assertThat(trades.size()).isEqualTo(1);
        validateTrade(trades.get(0), "3", "2", 9, 100);
    }

    @Test
    void multipleMatches() {
        Order buyOrder1 = new Order("1", 50000, 99, BUY);
        List<Trade> trades = orderMatcher.matchAndInsertRemaining(buyOrder1);
        assertThat(trades.isEmpty()).isTrue();

        Order buyOrder2 = new Order("2", 25500, 98, BUY);
        orderMatcher.matchAndInsertRemaining(buyOrder2);
        assertThat(trades.isEmpty()).isTrue();

        Order sellOrder1 = new Order("4", 500, 100, SELL);
        priceLevels = new PriorityQueue<>(reverseOrder());
        priceLevels.offer(99);
        priceLevels.offer(98);
        farBook = new HashMap<>();
        farBook.put(99, singletonList(buyOrder1));
        farBook.put(98, singletonList(buyOrder2));
        when(orderBook.getPriceLevels(any())).thenReturn(priceLevels);
        when(orderBook.getBook(any())).thenReturn(farBook);
        trades = orderMatcher.matchAndInsertRemaining(sellOrder1);
        assertThat(trades.isEmpty()).isTrue();

        Order sellOrder2 = new Order("5", 10000, 100, SELL);
        when(orderBook.getPriceLevels(any())).thenReturn(priceLevels);
        when(orderBook.getBook(any())).thenReturn(farBook);
        trades = orderMatcher.matchAndInsertRemaining(sellOrder2);
        assertThat(trades.isEmpty()).isTrue();

        Order sellOrder3 = new Order("6", 100, 103, SELL);
        when(orderBook.getPriceLevels(any())).thenReturn(priceLevels);
        when(orderBook.getBook(any())).thenReturn(farBook);
        trades = orderMatcher.matchAndInsertRemaining(sellOrder3);
        assertThat(trades.isEmpty()).isTrue();

        Order sellOrder4 = new Order("7", 20000, 105, SELL);
        when(orderBook.getPriceLevels(any())).thenReturn(priceLevels);
        when(orderBook.getBook(any())).thenReturn(farBook);
        trades = orderMatcher.matchAndInsertRemaining(sellOrder4);
        assertThat(trades.isEmpty()).isTrue();

        Order buyOrder3 = new Order("3", 16000, 105, BUY);
        priceLevels = new PriorityQueue<>();
        priceLevels.offer(100);
        priceLevels.offer(103);
        priceLevels.offer(105);
        farBook = new HashMap<>();
        List<Order> sell100Orders = new ArrayList<>();
        sell100Orders.add(sellOrder1);
        sell100Orders.add(sellOrder2);
        farBook.put(100, sell100Orders);
        farBook.put(103, singletonList(sellOrder3));
        farBook.put(105, singletonList(sellOrder4));
        when(orderBook.getPriceLevels(any())).thenReturn(priceLevels);
        when(orderBook.getBook(any())).thenReturn(farBook);
        trades = orderMatcher.matchAndInsertRemaining(buyOrder3);
        assertThat(trades.size()).isEqualTo(4);
        validateTrade(trades.get(0), "3", "4", 100, 500);
        validateTrade(trades.get(1), "3", "5", 100, 10000);
        validateTrade(trades.get(2), "3", "6", 103, 100);
        validateTrade(trades.get(3), "3", "7", 105, 5400);
        assertThat(sellOrder1.isFilled()).isTrue();
        assertThat(sellOrder2.isFilled()).isTrue();
        assertThat(sellOrder3.isFilled()).isTrue();
        assertThat(sellOrder4.getVolume()).isEqualTo(14600);
    }

    private void validateTrade(Trade trade, String crossingOrderId, String restingOrderId, int price, int volume) {
        assertThat(trade.getCrossingOrderId()).isEqualTo(crossingOrderId);
        assertThat(trade.getRestingOrderId()).isEqualTo(restingOrderId);
        assertThat(trade.getPrice()).isEqualTo(price);
        assertThat(trade.getVolume()).isEqualTo(volume);
    }
}