package com.epam.esm.epammodule4.controller;

import com.epam.esm.epammodule4.model.dto.*;
import com.epam.esm.epammodule4.model.dto.request.CreateOrderRequest;
import com.epam.esm.epammodule4.model.dto.request.UpdateOrderRequest;
import com.epam.esm.epammodule4.model.dto.response.HighestCostResponse;
import com.epam.esm.epammodule4.model.entity.Order;
import com.epam.esm.epammodule4.service.OrderService;
import com.epam.esm.epammodule4.service.UserService;
import com.epam.esm.epammodule4.service.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/orders"})
public class OrderController {

    private final OrderMapper orderMapper;
    private final OrderService orderService;
    private final UserService userService;

    @GetMapping(value = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public OrderDto getOrderById(@PathVariable Long id) {
        Order foundOrder = orderService.findById(id);
        OrderDto orderDto = orderMapper.toDto(foundOrder);

        orderDto.add(linkTo(methodOn(OrderController.class).getOrderById(orderDto.getId()))
                .withSelfRel());
        return orderDto;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<OrderDto> getAllOrders(Pageable pageable) {
        Page<Order> foundOrders = orderService.findAll(pageable);

        return foundOrders.map(orderMapper::toDto);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public OrderDto addOrder(@RequestBody CreateOrderRequest createOrderRequest) {
        userService.checkIdOfCurrentUser(createOrderRequest.getUserId());

        Order createdOrder = orderService.create(createOrderRequest);
        OrderDto orderDto = orderMapper.toDto(createdOrder);

        orderDto.add(linkTo(methodOn(OrderController.class).getOrderById(orderDto.getId()))
                .withSelfRel());
        return orderDto;
    }

    @PatchMapping
    @PreAuthorize("hasRole('ADMIN')")
    public OrderDto updateOrder(@RequestBody UpdateOrderRequest updateRequest) {
        Order updatedOrder = orderService.update(updateRequest);
        OrderDto orderDto = orderMapper.toDto(updatedOrder);

        orderDto.add(linkTo(methodOn(OrderController.class).getOrderById(orderDto.getId()))
                .withSelfRel());
        return orderDto;
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteOrderById(@PathVariable Long id) {
        orderService.delete(id);
    }

    @GetMapping(value = "/{orderId}/user")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public OrderDto getOrderByIdAndUserId(@PathVariable Long orderId, @RequestParam Long userId) {
        userService.checkIdOfCurrentUser(userId);

        Order foundOrder = orderService.findByOrderIdAndUserId(orderId, userId);
        OrderDto orderDto = orderMapper.toDto(foundOrder);

        orderDto.add(linkTo(methodOn(OrderController.class).getOrderById(orderDto.getId()))
                .withSelfRel());
        return orderDto;
    }

    @GetMapping(value = "/user")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Page<OrderDto> getAllOrdersForUser(@RequestParam Long userId, Pageable pageable) {
        userService.checkIdOfCurrentUser(userId);

        Page<Order> foundOrders = orderService.findAllByUserId(userId, pageable);

        return foundOrders.map(orderMapper::toDto);
    }

    @GetMapping(value = "/cost")
    @PreAuthorize("hasRole('ADMIN')")
    public HighestCostResponse getHighestCost(@RequestParam Long userId) {
        Double cost = orderService.getHighestCost(userId);

        HighestCostResponse highestCostDto = new HighestCostResponse(cost);

        return highestCostDto;
    }
}