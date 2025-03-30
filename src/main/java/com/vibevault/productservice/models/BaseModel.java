package com.vibevault.productservice.models;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import java.util.Date;

@Data
@MappedSuperclass
public class BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    @CreatedDate
    private Date createdAt;
    @Column(nullable = false)
    private Date lastModifiedAt;
    @Column(nullable = false)
    private boolean isDeleted;
}
