package com.epam.esm.epammodule4.model.entity;

import javax.persistence.*;

import javax.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class AuditableEntity {

    @Column(name = "created_date", nullable = false, updatable = false)
    @CreatedDate
    private Instant createDate;

    @Column(name = "last_update_date")
    @LastModifiedDate
    private Instant lastUpdateDate;
}
