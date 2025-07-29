package com.ecommerce.domain.order.entity;

import com.ecommerce.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Delivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private String trackingNumber; // 운송 번호

    private String courierCompany; // 택배 회사 명

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    private LocalDateTime shippedAt;

    private LocalDateTime deliveredAt;

    public void updateStatus(DeliveryStatus status) {
        this.status = status;
        if (status == DeliveryStatus.SHIPPED) {
            this.shippedAt = LocalDateTime.now();
        } else if (status == DeliveryStatus.DELIVERED) {
            this.deliveredAt = LocalDateTime.now();
        }
    }
}
