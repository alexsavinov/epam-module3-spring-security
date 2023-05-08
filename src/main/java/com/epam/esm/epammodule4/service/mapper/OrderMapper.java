package com.epam.esm.epammodule4.service.mapper;

import com.epam.esm.epammodule4.model.dto.OrderDto;
import com.epam.esm.epammodule4.model.dto.UserDto;
import com.epam.esm.epammodule4.model.dto.request.CreateOrderRequest;
import com.epam.esm.epammodule4.model.entity.GiftCertificate;
import com.epam.esm.epammodule4.model.entity.Order;
import com.epam.esm.epammodule4.model.entity.User;
import com.epam.esm.epammodule4.util.DateUtil;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class OrderMapper {

    private final DateUtil dateUtil;
    private final ModelMapper modelMapper;
    private final GiftCertificateMapper certificateMapper;

    public Order toOrder(CreateOrderRequest createOrderRequest) {
        GiftCertificate certificate = GiftCertificate.builder()
                .id(createOrderRequest.getCertificateId())
                .build();

        User user = User.builder()
                .id(createOrderRequest.getUserId())
                .build();

        Order order = Order.builder()
                .user(user)
                .giftCertificate(certificate)
                .build();

        return order;
    }

    public OrderDto toDto(Order order) {
        OrderDto orderDto = OrderDto.builder()
                .id(order.getId())
                .price(order.getPrice())
                .user(modelMapper.map(order.getUser(), UserDto.class))
                .certificate(certificateMapper.toDto(order.getGiftCertificate()))
                .createDate(dateUtil.toIso8601Format(order.getCreateDate()))
                .lastUpdateDate(dateUtil.toIso8601Format(order.getLastUpdateDate()))
                .build();

        return orderDto;
    }
}
