package com.bookshop.order.order.domain;


import com.bookshop.order.book.Book;
import com.bookshop.order.book.BookClient;
import com.bookshop.order.book.Failure;
import com.bookshop.order.book.Success;
import com.bookshop.order.order.event.OrderAccepted;
import com.bookshop.order.order.event.OrderDispatched;
import com.bookshop.order.order.web.OrderMapper;
import com.bookshop.order.order.web.OrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
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
    private final StreamBridge streamBridge;
    private final OrderMapper orderMapper;

    public OrderService(OrderRepository orderRepository, BookClient bookClient, StreamBridge streamBridge, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.bookClient = bookClient;
        this.streamBridge = streamBridge;
        this.orderMapper = orderMapper;
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
    public OrderResponse placeOrder(String title, int quantity) {
        var order = switch (bookClient.searchBook(title)) {
            case Success(Book book) -> orderMapper.toAcceptedOrder(book, quantity);
            case Failure(String reason, _) -> orderMapper.createRejectedOrder(title, quantity, reason);
        };
        Order savedOrder = orderRepository.save(order);
        if (savedOrder.status() == OrderStatus.ACCEPTED) {
            publishOrderAcceptedEvent(savedOrder);
        }
        return orderMapper.toOrderResponse(savedOrder);
    }

    @Transactional
    public OrderResponse submitOrder(String isbn, int quantity) {
        var order = switch (bookClient.getBook(isbn)) {
            case Success(Book book) -> orderMapper.toAcceptedOrder(book, quantity);
            case Failure(String reason, _) -> orderMapper.toRejectedOrder(isbn, quantity, reason);
        };
        Order savedOrder = orderRepository.save(order);
        if (savedOrder.status() == OrderStatus.ACCEPTED) {
            publishOrderAcceptedEvent(savedOrder);
        }
        return orderMapper.toOrderResponse(savedOrder);
    }

    private void publishOrderAcceptedEvent(Order order) {
        if (order.status() == OrderStatus.ACCEPTED) {
            var orderAcceptedEvent = new OrderAccepted(order.id(), Instant.now());
            log.info("Sending order accepted event with id {}", order.id());
            var isSent = streamBridge.send("orderAccepted-out-0", orderAcceptedEvent);
            log.info("Sending data for order with id {} successful ? {}", order.id(), isSent);
        }
    }

    public void consumeOrderDispatchedEvent(OrderDispatched orderDispatched) {
        orderRepository.findById(orderDispatched.orderId())
                .filter(order -> order.status() != OrderStatus.DISPATCHED) // save DISPATCHED order again has no implication as it is idempotent op, need not to chek the filter
                .map(order -> orderMapper.toDispatchedOrder(order, orderDispatched.dispatchedDate()))
                .map(orderRepository::save)
                .orElseThrow();
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
                .orElseThrow(() -> new OrderNotFoundException("The order with ID " + id + "not found"));
    }

}
