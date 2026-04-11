package com.f1pulse.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.f1pulse.backend.model.User;

    public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}