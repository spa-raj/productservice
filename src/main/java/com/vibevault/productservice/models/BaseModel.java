package com.vibevault.productservice.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Getter
@Setter
@MappedSuperclass
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Long id;

    @CreatedDate
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Date createdAt;

    @LastModifiedDate
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Date lastModifiedAt;

    @Column(name = "is_deleted")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private boolean isDeleted;
}
