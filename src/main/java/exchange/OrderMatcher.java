package exchange;

import java.util.List;

/**
 * @author Danish Alsayed
 */
public interface OrderMatcher {
    List<Trade> matchAndInsertRemaining(Order order);
}
