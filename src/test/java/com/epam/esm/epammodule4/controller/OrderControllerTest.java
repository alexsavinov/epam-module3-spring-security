package com.epam.esm.epammodule4.controller;

import com.epam.esm.epammodule4.controller.advice.ApplicationControllerAdvice;
import com.epam.esm.epammodule4.exception.OrderAlreadyExistsException;
import com.epam.esm.epammodule4.exception.OrderNotFoundException;
import com.epam.esm.epammodule4.model.dto.*;
import com.epam.esm.epammodule4.model.dto.request.CreateOrderRequest;
import com.epam.esm.epammodule4.model.dto.request.UpdateOrderRequest;
import com.epam.esm.epammodule4.model.dto.response.HighestCostResponse;
import com.epam.esm.epammodule4.model.entity.GiftCertificate;
import com.epam.esm.epammodule4.model.entity.Order;
import com.epam.esm.epammodule4.model.entity.User;
import com.epam.esm.epammodule4.service.OrderService;
import com.epam.esm.epammodule4.service.UserService;
import com.epam.esm.epammodule4.service.mapper.OrderMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private static final Long USER_ID = 1L;
    private static final Long CERTIFICATE_ID = 1L;
    private static final Long ORDER_ID = 1L;
    @Mock
    private OrderService orderService;
    @Mock
    private UserService userService;
    @Mock
    private OrderMapper orderMapper;
    @InjectMocks
    private OrderController subject;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(subject)
                .setControllerAdvice(new ApplicationControllerAdvice())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void getOrderById() throws Exception {
        GiftCertificate certificate = GiftCertificate.builder()
                .id(CERTIFICATE_ID)
                .name("cert")
                .build();
        GiftCertificateDto certificateDto = GiftCertificateDto.builder()
                .id(CERTIFICATE_ID)
                .name("cert")
                .build();

        User expectedUser = User.builder().id(USER_ID).name("User").build();
        UserDto userDto =  UserDto.builder().id(USER_ID).name("User").build();

        Order expectedOrder = Order.builder()
                .id(ORDER_ID)
                .price(10.2)
                .user(expectedUser)
                .giftCertificate(certificate)
                .build();
        OrderDto orderDto = OrderDto.builder()
                .id(ORDER_ID)
                .price(10.2)
                .user(userDto)
                .certificate(certificateDto)
                .build();

        when(orderService.findById(any(Long.class))).thenReturn(expectedOrder);
        when(orderMapper.toDto(any(Order.class))).thenReturn(orderDto);

        mockMvc.perform(
                        get("/orders/{id}", ORDER_ID)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderDto.getId().toString()))
                .andExpect(jsonPath("$.user.id").value(orderDto.getUser().getId()))
                .andExpect(jsonPath("$.user.name").value(orderDto.getUser().getName()))
                .andExpect(jsonPath("$.certificate.id").value(orderDto.getCertificate().getId()))
                .andExpect(jsonPath("$.certificate.name").value(orderDto.getCertificate().getName()))
                .andExpect(jsonPath("$.certificate.description").value(orderDto.getCertificate().getDescription()))
                .andExpect(jsonPath("$.price").value(orderDto.getPrice()));

        verify(orderService).findById(ORDER_ID);
        verify(orderMapper).toDto(expectedOrder);
        verifyNoMoreInteractions(orderService, orderMapper);
    }

    @Test
    void getOrderById_whenOrderNotFoundExceptionIsThrows_returns404() throws Exception {
        String errorMessage = "Order not found";

        when(orderService.findById(any(Long.class)))
                .thenThrow(new OrderNotFoundException(errorMessage));

        mockMvc.perform(
                        get("/orders/{id}", ORDER_ID)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage").value(errorMessage));

        verify(orderService).findById(ORDER_ID);
        verifyNoInteractions(orderMapper);
        verifyNoMoreInteractions(orderService);
    }

    @Test
    void getAllOrders() throws Exception {
        List<Order> expectedOrders = List.of(new Order());

        Pageable pageable = PageRequest.of(0, 5, Sort.by("price"));
        Page<Order> pageableExpectedOrders = new PageImpl(expectedOrders, pageable, expectedOrders.size());

        when(orderService.findAll(any(Pageable.class))).thenReturn(pageableExpectedOrders);

        mockMvc.perform(
                        get("/orders")
                                .param("page", "0")
                                .param("size", "5")
                                .param("sort", "price,asc")
                )
                .andExpect(status().isOk());

        verify(orderService).findAll(pageable);
        verifyNoMoreInteractions(orderService);
    }

    @Test
    void addOrder() throws Exception {
        GiftCertificate expectedCertificate = GiftCertificate.builder()
                .id(CERTIFICATE_ID)
                .name("cert")
                .build();
        GiftCertificateDto certificateDto = GiftCertificateDto.builder()
                .id(CERTIFICATE_ID)
                .name("cert")
                .build();

        User expectedUser = User.builder().id(USER_ID).name("User").build();
        UserDto userDto = UserDto.builder().id(USER_ID).name("User").build();

        Order expectedOrder = Order.builder()
                .id(ORDER_ID)
                .price(10.2)
                .user(expectedUser)
                .giftCertificate(expectedCertificate)
                .build();
        OrderDto orderDto = OrderDto.builder()
                .id(ORDER_ID)
                .price(10.2)
                .user(userDto)
                .certificate(certificateDto)
                .build();

        when(orderService.create(any(CreateOrderRequest.class))).thenReturn(expectedOrder);
        when(orderMapper.toDto(any(Order.class))).thenReturn(orderDto);

        RequestBuilder requestBuilder = post("/orders")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(orderDto))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(orderDto.getId().toString()))
                .andExpect(jsonPath("$.user.id").value(orderDto.getUser().getId()))
                .andExpect(jsonPath("$.user.name").value(orderDto.getUser().getName()))
                .andExpect(jsonPath("$.certificate.id").value(orderDto.getCertificate().getId()))
                .andExpect(jsonPath("$.certificate.name").value(orderDto.getCertificate().getName()))
                .andExpect(jsonPath("$.certificate.description").value(orderDto.getCertificate().getDescription()))
                .andExpect(jsonPath("$.price").value(orderDto.getPrice()));

        verify(userService).checkIdOfCurrentUser(null);
        verify(orderMapper).toDto(expectedOrder);
        verifyNoMoreInteractions(orderService, orderMapper, userService);
    }

    @Test
    void addOrder_whenOrderAlreadyExistsExceptionIsThrows_returns409() throws Exception {
        CreateOrderRequest createOrderRequest = new CreateOrderRequest();

        when(orderService.create(any(CreateOrderRequest.class)))
                .thenThrow(new OrderAlreadyExistsException("Order already exists"));

        RequestBuilder requestBuilder = post("/orders")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(createOrderRequest))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorMessage").value("Order already exists"));

        verify(userService).checkIdOfCurrentUser(null);
        verifyNoInteractions(orderMapper);
        verifyNoMoreInteractions(orderService);
    }

    @Test
    void updateOrder() throws Exception {
        GiftCertificate expectedCertificate = GiftCertificate.builder()
                .id(CERTIFICATE_ID)
                .name("cert")
                .build();
        GiftCertificateDto certificateDto = GiftCertificateDto.builder()
                .id(CERTIFICATE_ID)
                .name("cert")
                .build();

        User expectedUser = User.builder().id(USER_ID).name("User").build();
        UserDto userDto = UserDto.builder().id(USER_ID).name("User").build();

        Order expectedOrder = Order.builder()
                .id(ORDER_ID)
                .price(10.2)
                .user(expectedUser)
                .giftCertificate(expectedCertificate)
                .build();
        OrderDto orderDto = OrderDto.builder()
                .id(ORDER_ID)
                .price(10.2)
                .user(userDto)
                .certificate(certificateDto)
                .build();

        when(orderService.update(any(UpdateOrderRequest.class))).thenReturn(expectedOrder);
        when(orderMapper.toDto(any(Order.class))).thenReturn(orderDto);

        ObjectMapper objectMapper = new ObjectMapper();

        RequestBuilder requestBuilder = patch("/orders")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(orderDto))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderDto.getId().toString()))
                .andExpect(jsonPath("$.user.id").value(orderDto.getUser().getId()))
                .andExpect(jsonPath("$.user.name").value(orderDto.getUser().getName()))
                .andExpect(jsonPath("$.certificate.id").value(orderDto.getCertificate().getId()))
                .andExpect(jsonPath("$.certificate.name").value(orderDto.getCertificate().getName()))
                .andExpect(jsonPath("$.certificate.description").value(orderDto.getCertificate().getDescription()))
                .andExpect(jsonPath("$.price").value(orderDto.getPrice()));

        verify(orderMapper).toDto(expectedOrder);
        verifyNoMoreInteractions(orderService, orderMapper);
    }

    @Test
    void deleteOrderById() throws Exception {
        RequestBuilder requestBuilder = delete("/orders/{id}", ORDER_ID);

        mockMvc.perform(requestBuilder).andExpect(status().isNoContent());

        verify(orderService).delete(ORDER_ID);
        verifyNoMoreInteractions(orderService);
    }

    @Test
    void getOneOrderForUser() throws Exception {
        GiftCertificate certificate = GiftCertificate.builder()
                .id(CERTIFICATE_ID)
                .name("cert")
                .build();
        GiftCertificateDto certificateDtoRequest = GiftCertificateDto.builder()
                .id(CERTIFICATE_ID)
                .name("cert")
                .build();

        User user = User.builder().id(USER_ID).name("User").build();
        UserDto userDto = UserDto.builder().id(USER_ID).name("User").build();

        Order expectedOrder = Order.builder()
                .id(ORDER_ID)
                .price(10.2)
                .user(user)
                .giftCertificate(certificate)
                .build();
        OrderDto orderDto = OrderDto.builder()
                .id(ORDER_ID)
                .price(10.2)
                .user(userDto)
                .certificate(certificateDtoRequest)
                .build();

        when(orderService.findByOrderIdAndUserId(any(Long.class), any(Long.class))).thenReturn(expectedOrder);
        when(orderMapper.toDto(any(Order.class))).thenReturn(orderDto);

        mockMvc.perform(
                        get("/orders/{orderId}/user", USER_ID)
                                .param("userId", String.valueOf(USER_ID))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedOrder.getId().toString()))
                .andExpect(jsonPath("$.price").value(expectedOrder.getPrice()))
                .andExpect(jsonPath("$.user.id").value(expectedOrder.getUser().getId()))
                .andExpect(jsonPath("$.certificate.id").value(expectedOrder.getGiftCertificate().getId()));

        verify(userService).checkIdOfCurrentUser(USER_ID);
        verify(orderService).findByOrderIdAndUserId(USER_ID, ORDER_ID);
        verify(orderMapper).toDto(expectedOrder);
        verifyNoMoreInteractions(orderService, orderMapper, userService);
    }

    @Test
    void getAllOrdersForUser() throws Exception {
        GiftCertificate certificate = GiftCertificate.builder()
                .id(CERTIFICATE_ID)
                .name("cert")
                .build();
        GiftCertificateDto certificateDtoRequest = GiftCertificateDto.builder()
                .id(CERTIFICATE_ID)
                .name("cert")
                .build();

        User user = User.builder().id(USER_ID).name("User").build();
        UserDto userDto = UserDto.builder().id(USER_ID).name("User").build();

        Order expectedOrder = Order.builder()
                .id(ORDER_ID)
                .price(10.2)
                .user(user)
                .giftCertificate(certificate)
                .build();
        OrderDto orderDto = OrderDto.builder()
                .id(ORDER_ID)
                .price(10.2)
                .user(userDto)
                .certificate(certificateDtoRequest)
                .build();


        List<Order> expectedOrders = List.of(expectedOrder);

        Pageable pageable = PageRequest.of(0, 5);
        Page<Order> pageableExpectedOrders = new PageImpl(expectedOrders, pageable, expectedOrders.size());

        when(orderService.findAllByUserId(any(Long.class), any(Pageable.class))).thenReturn(pageableExpectedOrders);
        when(orderMapper.toDto(any(Order.class))).thenReturn(orderDto);

        mockMvc.perform(
                        get("/orders/user")
                                .param("userId", String.valueOf(USER_ID))
                                .param("page", "0")
                                .param("size", "5")

                )
                .andExpect(status().isOk());

        verify(userService).checkIdOfCurrentUser(USER_ID);
        verify(orderService).findAllByUserId(USER_ID, pageable);
        verify(orderMapper).toDto(expectedOrder);
        verifyNoMoreInteractions(orderService, orderMapper, userService);
    }

    @Test
    void getHighestCost() throws Exception {
        HighestCostResponse expectedCostDto = new HighestCostResponse(100.11);

        when(orderService.getHighestCost(any(Long.class))).thenReturn(expectedCostDto.getHighestCost());

        mockMvc.perform(
                        get("/orders/cost")
                                .param("userId", String.valueOf(USER_ID))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.highestCost").value(expectedCostDto.getHighestCost().toString()));

        verify(userService).checkIdOfCurrentUser(USER_ID);
        verify(orderService).getHighestCost(USER_ID);
        verifyNoMoreInteractions(orderService, userService);
    }
}