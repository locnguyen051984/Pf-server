package com.Pf.auth_service.repository;

import com.Pf.auth_service.entity.User;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @Cacheable(value = "users", key = "#username")
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Override
    @CacheEvict(value = "users", key = "#entity.username")
    <S extends User> S save(S entity);
}
