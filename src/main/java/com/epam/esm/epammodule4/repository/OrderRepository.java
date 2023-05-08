package com.epam.esm.epammodule4.repository;

import com.epam.esm.epammodule4.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findAllByUserId(Long userId, Pageable pageable);

    Optional<Order> findByIdAndUserId(Long orderId, Long userId);

    Optional<Order> findByGiftCertificateId(Long certId);

    Optional<Order> findFirstByUserIdOrderByPriceDesc(Long userId);
}
