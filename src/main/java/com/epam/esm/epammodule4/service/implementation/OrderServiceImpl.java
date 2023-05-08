package com.epam.esm.epammodule4.service.implementation;

import com.epam.esm.epammodule4.exception.OrderAlreadyExistsException;
import com.epam.esm.epammodule4.exception.OrderNotFoundException;
import com.epam.esm.epammodule4.model.dto.request.CreateOrderRequest;
import com.epam.esm.epammodule4.model.dto.request.UpdateOrderRequest;
import com.epam.esm.epammodule4.model.entity.GiftCertificate;
import com.epam.esm.epammodule4.model.entity.Order;
import com.epam.esm.epammodule4.model.entity.User;
import com.epam.esm.epammodule4.repository.*;
import com.epam.esm.epammodule4.service.GiftCertificateService;
import com.epam.esm.epammodule4.service.OrderService;
import com.epam.esm.epammodule4.service.UserService;
import com.epam.esm.epammodule4.service.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static java.util.Optional.ofNullable;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final GiftCertificateService certificateService;
    private final UserService userService;
    private final OrderMapper orderMapper;

    @Override
    public Order findById(Long id) {
        log.debug("Looking for an order with id {}", id);

        Order foundOrder = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Requested resource not found (id = %s)".formatted(id)
                ));

        log.info("Received an order with id {}", id);
        return foundOrder;
    }

    @Override
    public Page<Order> findAll(Pageable pageable) {
        log.debug("Retrieving orders. Page request: {}", pageable);

        Page<Order> foundOrders = orderRepository.findAll(pageable);

        log.info("Retrieved {} orders of {} total", foundOrders.getSize(), foundOrders.getTotalElements());
        return foundOrders;
    }

    @Override
    @Transactional
    public Order create(CreateOrderRequest createOrderRequest) {
        log.debug("Creating a new order");

        User foundUser = userService.findById(createOrderRequest.getUserId());

        GiftCertificate foundCertificate = certificateService.findById(createOrderRequest.getCertificateId());

        Order newOrder = orderMapper.toOrder(createOrderRequest);
        newOrder.setPrice(foundCertificate.getPrice());

        Order createdOrder = orderRepository.save(newOrder);
        createdOrder.setUser(foundUser);
        createdOrder.setGiftCertificate(foundCertificate);

        log.info("Created a new order with id {}", createdOrder.getId());
        return createdOrder;
    }

    @Override
    @Transactional
    public Order update(UpdateOrderRequest updateOrderRequest) {
        log.debug("Updating an order with id {}", updateOrderRequest.getId());

        Order foundOrder = findById(updateOrderRequest.getId());

        ofNullable(updateOrderRequest.getPrice()).ifPresent(foundOrder::setPrice);

        ofNullable(updateOrderRequest.getUser()).ifPresent(userDto -> {
            User foundUser = userService.findById(userDto.getId());
            foundOrder.setUser(foundUser);
        });

        ofNullable(updateOrderRequest.getCertificate()).ifPresent(certificateDto -> {
            GiftCertificate foundCertificate = certificateService.findById(certificateDto.getId());
            foundOrder.setGiftCertificate(foundCertificate);
        });

        Order updatedOrder = orderRepository.save(foundOrder);

        log.info("Updated an order with id {}", updatedOrder.getId());
        return updatedOrder;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("Deleting order with id {}", id);

        Order foundOrder = findById(id);

        orderRepository.delete(foundOrder);
        log.info("Order with id {} is deleted", foundOrder.getId());
    }

    @Override
    public Page<Order> findAllByUserId(Long userId, Pageable pageable) {
        log.debug("Retrieving orders by user id {}. Page request: {}", userId, pageable);

        Page<Order> foundOrders = orderRepository.findAllByUserId(userId, pageable);

        log.info("Retrieved {} orders of {} total", foundOrders.getSize(), foundOrders.getTotalElements());
        return foundOrders;
    }


    @Override
    public Order findByOrderIdAndUserId(Long orderId, Long userId) {
        log.debug("Looking for an order with id {} by user id {}", orderId, userId);

        Order foundOrder = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Requested resource not found (id = %s)".formatted(orderId)
                ));

        log.info("Received an order with id {} by user id", orderId, userId);
        return foundOrder;
    }

    @Override
    public Optional<Order> findByGiftCertificateId(Long certificateId) {
        log.debug("Looking for an order by certificate id {}", certificateId);

        Optional<Order> foundOrder = orderRepository.findByGiftCertificateId(certificateId);

        foundOrder.ifPresentOrElse(
                order -> log.info("Received an order with id {} by certificate id {}", order.getId(), certificateId),
                () -> log.info("Order by certificate id {} not found", certificateId)
        );

        return foundOrder;
    }

    @Override
    @Transactional
    public Order createForUser(CreateOrderRequest createOrderRequest) {
        log.debug("Creating a new order");

        Long certificateId = createOrderRequest.getCertificateId();

        Optional<Order> foundOrder = this.findByGiftCertificateId(certificateId);
        foundOrder.ifPresent(order -> {
            throw new OrderAlreadyExistsException("Order (id = %s) with gift certificate (id = %s) is already exists"
                    .formatted(order.getId(), certificateId));
        });

        Order createdOrder = this.create(createOrderRequest);

        log.info("Created a new order with id {}", createdOrder.getId());
        return createdOrder;
    }

    @Override
    public Double getHighestCost(Long userId) {
        log.debug("Looking for an order with highest cost by user id {}", userId);

        Double cost = 0.0;

        Optional<Order> foundOrder = orderRepository.findFirstByUserIdOrderByPriceDesc(userId);

        if (foundOrder.isPresent()) {
            log.info("Received an order with id {} by user id {}", foundOrder.get().getId(), userId);
            cost = foundOrder.get().getPrice();
        } else {
            log.info("No orders by user's id {} found", userId);
        }

        return cost;
    }
}
