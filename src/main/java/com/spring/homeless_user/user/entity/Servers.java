package com.spring.homeless_user.user.entity;

import jakarta.persistence.*;

@Entity
public class Servers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // User의 id만 저장됨

    private int friendsId; // 친구 이름 (예: 'John Doe')

    private String status; // 상태 (예: 요청 대기, 친구 등)


}
