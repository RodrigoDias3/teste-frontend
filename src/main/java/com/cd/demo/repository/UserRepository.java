package com.cd.demo.repository;

import com.cd.demo.dao.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByNameAndEmail(String name, String email);
}
