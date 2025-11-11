package com.axelbon.pos.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.axelbon.pos.entity.enums.AuditAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private Long auditorId;

    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    private AuditAction action;

    @Column(length= 100)
    private String reason;

    @CreationTimestamp
    @Column(nullable=false)
    private LocalDateTime createdAt;

    // TODO: Refactor to @ManyToOne when Category entity exists
    private Long categoryId;
    
    // TODO: Refactor to @ManyToOne when Product entity exists
    private Long productId;
    
    // TODO: Refactor to @ManyToOne when Supplier entity exists
    private Long supplierId;
    
    // TODO: Refactor to @ManyToOne when User entity exists
    private Long userId;
    
    // TODO: Refactor to @ManyToOne when Role entity exists
    private Long roleId;

}
