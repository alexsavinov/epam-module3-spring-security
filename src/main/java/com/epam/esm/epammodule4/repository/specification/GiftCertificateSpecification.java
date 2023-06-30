package com.epam.esm.epammodule4.repository.specification;

import com.epam.esm.epammodule4.model.dto.request.SearchGiftCertificateRequest;
import com.epam.esm.epammodule4.model.entity.GiftCertificate;
import com.epam.esm.epammodule4.model.entity.Tag;

import javax.persistence.criteria.*;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import static com.epam.esm.epammodule4.model.entity.GiftCertificate.DESCRIPTION;
import static com.epam.esm.epammodule4.model.entity.GiftCertificate.NAME;
import static java.util.Optional.ofNullable;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GiftCertificateSpecification implements Specification<GiftCertificate> {

    private SearchGiftCertificateRequest searchRequest;

    @Override
    public Predicate toPredicate(Root<GiftCertificate> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        Predicate predicateNameDesc = null;
        Predicate predicateTags = null;

        if (ofNullable(searchRequest.getName()).isPresent()) {
            predicateNameDesc = builder.or(
                    builder.like(root.get(NAME), getStringLike(searchRequest.getName())),
                    builder.like(root.get(DESCRIPTION), getStringLike(searchRequest.getName()))
            );
        }

        if (ofNullable(searchRequest.getTags()).isPresent()) {
            Join<GiftCertificate, Tag> certificateTags = root.join("tags");
            Expression<String> expression = certificateTags.get("name");
            predicateTags = builder.and(expression.in(searchRequest.getTags()));
        }

        if (ofNullable(predicateNameDesc).isPresent() && (ofNullable(predicateTags).isPresent())) {
            return builder.and(predicateNameDesc, predicateTags);
        } else if (ofNullable(predicateNameDesc).isPresent() && (ofNullable(predicateTags).isEmpty())) {
            return predicateNameDesc;
        } else if (ofNullable(predicateNameDesc).isEmpty() && (ofNullable(predicateTags).isPresent())) {
            return predicateTags;
        }

        return null;
    }

    private String getStringLike(String name) {
        return "%%%s%%".formatted(name);
    }
}
