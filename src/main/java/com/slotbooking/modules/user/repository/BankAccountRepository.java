package com.slotbooking.modules.user.repository;

import com.slotbooking.modules.user.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByUserId(Long userId);
    Optional<BankAccount> findByUpiId(String upiId);
    Optional<BankAccount> findByAccountNumber(String accountNumber);
}
