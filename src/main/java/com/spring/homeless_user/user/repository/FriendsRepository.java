package com.spring.homeless_user.user.repository;

import com.spring.homeless_user.user.entity.Friends;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendsRepository extends JpaRepository<Friends, Long> {
    List<Friends> findAllById(Long userId);
}
