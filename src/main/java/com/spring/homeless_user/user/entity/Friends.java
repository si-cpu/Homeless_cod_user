package com.spring.homeless_user.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.processing.Generated;
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Friends {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // User의 id만 저장됨

    @ManyToOne
    @JoinColumn(name = "friend_id")
    private User friend; // 친구 요청을 받은 사용자

    public Friends(String user, String friend) {

    }
}
