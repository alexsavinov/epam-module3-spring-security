package com.epam.esm.epammodule4.repository;

import com.epam.esm.epammodule4.model.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findFirstByName(String name);

    List<Tag> findTagsByCertificatesId(Long id);

    @Query(
            value = "SELECT t.id, t.name FROM certificate_tag ct " +
                    "INNER JOIN gift_certificate c on ct.cert_id = c.id " +
                    "INNER JOIN cert_order co on c.id = co.cert_id " +
                    "INNER JOIN tag t on t.id = ct.tag_id " +
                    "WHERE co.customer_id=:id GROUP BY t.id ORDER BY count(t.id) DESC LIMIT 1",
            nativeQuery = true
    )
    Optional<Tag> getTopUsedTag(@Param("id") Long userId);
}
