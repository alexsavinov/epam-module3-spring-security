package com.epam.esm.epammodule4.service;

import com.epam.esm.epammodule4.exception.OrderAlreadyExistsException;
import com.epam.esm.epammodule4.exception.OrderNotFoundException;
import com.epam.esm.epammodule4.model.dto.*;
import com.epam.esm.epammodule4.model.dto.request.CreateOrderRequest;
import com.epam.esm.epammodule4.model.dto.request.UpdateOrderRequest;
import com.epam.esm.epammodule4.model.entity.GiftCertificate;
import com.epam.esm.epammodule4.model.entity.Order;
import com.epam.esm.epammodule4.model.entity.User;
import com.epam.esm.epammodule4.repository.OrderRepository;
import com.epam.esm.epammodule4.service.implementation.GiftCertificateServiceImpl;
import com.epam.esm.epammodule4.service.implementation.OrderServiceImpl;
import com.epam.esm.epammodule4.service.implementation.UserServiceImpl;
import com.epam.esm.epammodule4.service.mapper.OrderMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    private static final Long ORDER_ID = 1L;
    private static final Long CERTIFICATE_ID = 1L;
    private static final Long USER_ID = 1L;
    @InjectMocks
    private OrderServiceImpl subject;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserServiceImpl userService;
    @Mock
    private GiftCertificateServiceImpl certificateService;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private CreateOrderRequest createRequest;
    @Mock
    private UpdateOrderRequest updateRequest;


    @Test
    void findById() {
        Order expectedOrder = new Order();

        when(orderRepository.findById(any(Long.class))).thenReturn(Optional.of(expectedOrder));

        Order actualOrder = subject.findById(ORDER_ID);

        verify(orderRepository).findById(ORDER_ID);
        verifyNoMoreInteractions(orderRepository);

        assertThat(actualOrder).isEqualTo(expectedOrder);
    }

    @Test
    void findById_whenOrderIsNotFoundById_throwsOrderNotFoundException() {
        when(orderRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        OrderNotFoundException exception = assertThrows(OrderNotFoundException.class,
                () -> subject.findById(ORDER_ID));

        verify(orderRepository).findById(ORDER_ID);
        verifyNoMoreInteractions(orderRepository);

        String expectedMessage = "Requested resource not found (id = %s)".formatted(ORDER_ID);
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void findAll() {
        List<Order> expectedOrders = new ArrayList<>();
        expectedOrders.add(new Order());

        Pageable pageable = PageRequest.of(0, 5, Sort.by("name"));
        Page<Order> allExpectedCertificates =
                new PageImpl(expectedOrders, pageable, expectedOrders.size());

        when(orderRepository.findAll(any(Pageable.class))).thenReturn(allExpectedCertificates);

        Page<Order> actualOrders = subject.findAll(pageable);

        verify(orderRepository).findAll(pageable);
        verifyNoMoreInteractions(orderRepository);

        assertThat(actualOrders).isEqualTo(allExpectedCertificates);
    }

    @Test
    void create() {
        Order newOrder = new Order();
        Order expectedOrder = new Order();
        User user = User.builder().id(USER_ID).build();
        GiftCertificate certificate = GiftCertificate.builder().id(CERTIFICATE_ID).build();

        when(userService.findById(any(Long.class))).thenReturn(user);
        when(certificateService.findById(any(Long.class))).thenReturn(certificate);
        when(orderMapper.toOrder(any(CreateOrderRequest.class))).thenReturn(newOrder);
        when(orderRepository.save(any(Order.class))).thenReturn(expectedOrder);

        Order actualOrder = subject.create(createRequest);

        verify(userService).findById(0L);
        verify(certificateService).findById(0L);
        verify(orderRepository).save(newOrder);
        verify(orderMapper).toOrder(createRequest);
        verifyNoMoreInteractions(orderRepository);

        assertThat(actualOrder).isEqualTo(expectedOrder);
    }

    @Test
    void update() {
        User user = new User();
        UserDto userDto = new UserDto();
        GiftCertificateDto certificateDto = new GiftCertificateDto();

        Order updateOrder = Order.builder()
                .id(ORDER_ID)
                .user(user)
                .giftCertificate(new GiftCertificate())
                .price(11.22)
                .build();
        Order expectedOrder = Order.builder()
                .id(ORDER_ID)
                .user(user)
                .giftCertificate(new GiftCertificate())
                .price(11.22)
                .build();

        when(updateRequest.getUser()).thenReturn(userDto);
        when(updateRequest.getCertificate()).thenReturn(certificateDto);
        when(updateRequest.getPrice()).thenReturn(11.22);
        when(orderRepository.findById(any(Long.class))).thenReturn(Optional.of(updateOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(expectedOrder);

        Order actualOrder = subject.update(updateRequest);

        verify(orderRepository).save(updateOrder);
        verifyNoMoreInteractions(orderRepository);

        assertThat(actualOrder).isEqualTo(expectedOrder);
    }

    @Test
    void delete() {
        Order deleteOrder = Order.builder()
                .id(ORDER_ID)
                .build();

        when(orderRepository.findById(any(Long.class))).thenReturn(Optional.of(deleteOrder));

        subject.delete(ORDER_ID);

        verify(orderRepository).delete(deleteOrder);
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    void findAllByUserId() {
        List<Order> expectedOrders = new ArrayList<>();
        expectedOrders.add(new Order());

        Pageable pageable = PageRequest.of(0, 5, Sort.by("name"));
        Page<Order> allExpectedCertificates =
                new PageImpl(expectedOrders, pageable, expectedOrders.size());

        when(orderRepository.findAllByUserId(any(Long.class), any(Pageable.class)))
                .thenReturn(allExpectedCertificates);

        Page<Order> actualOrders = subject.findAllByUserId(USER_ID, pageable);

        verify(orderRepository).findAllByUserId(USER_ID, pageable);
        verifyNoMoreInteractions(orderRepository);

        assertThat(actualOrders).isEqualTo(allExpectedCertificates);
    }

    @Test
    void findByOrderIdAndUserId() {
        Optional<Order> expectedOrder = Optional.of(new Order());

        when(orderRepository.findByIdAndUserId(any(Long.class), any(Long.class)))
                .thenReturn(expectedOrder);

        Order actualOrder = subject.findByOrderIdAndUserId(ORDER_ID, USER_ID);

        verify(orderRepository).findByIdAndUserId(ORDER_ID, USER_ID);
        verifyNoMoreInteractions(orderRepository);

        assertThat(actualOrder).isEqualTo(expectedOrder.get());
    }

    @Test
    void findByOrderIdAndUserId_whenOrderNotFound_throwOrderNotFoundException() {
        when(orderRepository.findByIdAndUserId(any(Long.class), any(Long.class)))
                .thenReturn(Optional.empty());

        OrderNotFoundException exception = assertThrows(OrderNotFoundException.class,
                () -> subject.findByOrderIdAndUserId(ORDER_ID, USER_ID));

        verify(orderRepository).findByIdAndUserId(ORDER_ID, USER_ID);
        verifyNoMoreInteractions(orderRepository);

        assertThat(exception.getMessage()).isEqualTo("Requested resource not found (id = 1)");
    }

    @Test
    void findByGiftCertificateId() {
        Optional<Order> expectedOrder = Optional.of(new Order());

        when(orderRepository.findByGiftCertificateId(any(Long.class)))
                .thenReturn(expectedOrder);

        Optional<Order> actualOrder = subject.findByGiftCertificateId(CERTIFICATE_ID);

        verify(orderRepository).findByGiftCertificateId(CERTIFICATE_ID);
        verifyNoMoreInteractions(orderRepository);

        assertThat(actualOrder).isEqualTo(expectedOrder);
    }

    @Test
    void createForUser() {
        Order newOrder = new Order();
        Order expectedOrder = new Order();
        User user = User.builder().id(USER_ID).build();
        GiftCertificate certificate = GiftCertificate.builder().id(CERTIFICATE_ID).build();

        when(userService.findById(any(Long.class))).thenReturn(user);
        when(certificateService.findById(any(Long.class))).thenReturn(certificate);
        when(subject.findByGiftCertificateId(any(Long.class))).thenReturn(Optional.empty());
        when(orderMapper.toOrder(any(CreateOrderRequest.class))).thenReturn(newOrder);
        when(orderRepository.save(any(Order.class))).thenReturn(expectedOrder);

        Order actualOrder = subject.createForUser(createRequest);

        verify(userService).findById(0L);
        verify(certificateService).findById(0L);
        verify(orderRepository).save(newOrder);
        verify(orderMapper).toOrder(createRequest);
        verifyNoMoreInteractions(orderRepository);

        assertThat(actualOrder).isEqualTo(expectedOrder);
    }

    @Test
    void createForUser_whenOrderAlreadyExists_throwOrderAlreadyExistsException() {
        Order expectedOrder = new Order();

        String expectedMessage = "Order (id = null) with gift certificate (id = 0) is already exists";

        when(subject.findByGiftCertificateId(any(Long.class))).thenReturn(Optional.ofNullable(expectedOrder));

        OrderAlreadyExistsException exception = assertThrows(OrderAlreadyExistsException.class,
                () -> subject.createForUser(createRequest));

        verifyNoMoreInteractions(orderRepository);

        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void getHighestCost() {
        Order expectedOrder = Order.builder()
                .id(ORDER_ID)
                .price(11.22)
                .build();

        Double expectedCost = 11.22;

        when(orderRepository.findFirstByUserIdOrderByPriceDesc(any(Long.class)))
                .thenReturn(Optional.ofNullable(expectedOrder));

        Double actualCost = subject.getHighestCost(USER_ID);

        verify(orderRepository).findFirstByUserIdOrderByPriceDesc(USER_ID);
        verifyNoMoreInteractions(orderRepository);

        assertThat(actualCost).isEqualTo(expectedCost);
    }

    @Test
    void getHighestCost_whenOrderNotFound_thenReturnZeroValue() {
        Double expectedCost = 0.0;

        when(orderRepository.findFirstByUserIdOrderByPriceDesc(any(Long.class)))
                .thenReturn(Optional.empty());

        Double actualCost = subject.getHighestCost(USER_ID);

        verify(orderRepository).findFirstByUserIdOrderByPriceDesc(USER_ID);
        verifyNoMoreInteractions(orderRepository);

        assertThat(actualCost).isEqualTo(expectedCost);
    }
}