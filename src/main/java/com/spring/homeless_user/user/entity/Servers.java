package com.spring.homeless_user.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "server",
        indexes = {
                @Index(name = "idx_serverId", columnList = "serverId"),
                @Index(name = "idx_userId", columnList = "user_id") // user_id에 대한 인덱스 추가
        }
)
public class Servers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long serverId;

    @Enumerated(EnumType.STRING)
    private AddStatus addStatus;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}

