package com.bookshop.order.order.domain;


import com.bookshop.order.book.Book;
import com.bookshop.order.book.BookClient;
import com.bookshop.order.book.Failure;
import com.bookshop.order.book.Success;
import com.bookshop.order.order.event.OrderEventPublisher;
import com.bookshop.order.order.web.OrderMapper;
import com.bookshop.order.order.web.OrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final BookClient bookClient;
    private final OrderMapper orderMapper;
    private final OrderEventPublisher orderEventPublisher;

    public OrderService(OrderRepository orderRepository, BookClient bookClient, OrderMapper orderMapper, OrderEventPublisher orderEventPublisher) {
        this.orderRepository = orderRepository;
        this.bookClient = bookClient;
        this.orderMapper = orderMapper;
        this.orderEventPublisher = orderEventPublisher;
    }

    public List<OrderResponse> findAll() {
        var orders = orderRepository.findAll();
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    public List<OrderResponse> findOrdersByUser(String userName) {
        var orders = orderRepository.findAllByCreatedBy(userName);
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    @Transactional
    public Order saveAndPublishEvent(Order order) {
        Order savedOrder = orderRepository.save(order);
        if (savedOrder.status() == OrderStatus.ACCEPTED) {
            Instant eventTimestamp = Instant.now();
            orderEventPublisher.publishOrderAcceptedEvent(savedOrder, eventTimestamp);
        }
        return savedOrder;
    }

    public OrderResponse submitOrder(String isbn, int quantity) {
        var order = switch (bookClient.getBook(isbn)) {
            case Success(Book book) -> orderMapper.toAcceptedOrder(book, quantity);
            case Failure(String reason, _) -> orderMapper.toRejectedOrder(isbn, quantity, reason);
        };
        Order savedOrder = saveAndPublishEvent(order);
        return orderMapper.toOrderResponse(savedOrder);
    }

    public Map<OrderStatus, List<OrderResponse>> findOrdersByStatus() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.groupingBy(OrderResponse::status));
    }

    public OrderResponse findOrderById(Long id) {
        return orderRepository.findById(id)
                .map(orderMapper::toOrderResponse)
                .orElseThrow(() -> new OrderNotFoundException("The order with ID %d not found".formatted(id)));
    }

}
