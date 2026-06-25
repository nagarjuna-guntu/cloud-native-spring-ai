package com.bookshop.order.order.domain;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends ListCrudRepository<Order, Long> {

    List<Order> findAllByCreatedBy(String userName);
}
