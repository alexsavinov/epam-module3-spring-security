package com.epam.esm.epammodule4.service;

import com.epam.esm.epammodule4.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PageableOrderService {

    Page<Order> findAll(Pageable pageable);

    Page<Order> findAllByUserId(Long id, Pageable pageable);
}
