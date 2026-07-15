package com.bookshop.order.order.event;

import com.bookshop.order.order.domain.OrderRepository;
import com.bookshop.order.order.domain.OrderStatus;
import com.bookshop.order.order.web.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventConsumer {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public OrderEventConsumer(OrderRepository orderRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    public void consumeOrderDispatchedEvent(OrderDispatchedEvent orderDispatched) {
        orderRepository.findById(orderDispatched.orderId())
                .filter(order -> order.status() != OrderStatus.DISPATCHED) // save DISPATCHED order again has no implication as it is idempotent op, need not to chek the filter
                .map(order -> orderMapper.toDispatchedOrder(order, orderDispatched.occurredOn()))
                .map(orderRepository::save)
                .orElseThrow();
    }
}
