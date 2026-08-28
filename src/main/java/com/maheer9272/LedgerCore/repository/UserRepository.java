package com.maheer9272.LedgerCore.repository;

import com.maheer9272.LedgerCore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
