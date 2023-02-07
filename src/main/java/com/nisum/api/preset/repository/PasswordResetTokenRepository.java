package com.nisum.api.preset.repository;

import com.nisum.api.preset.domain.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional <PasswordResetToken> findByToken(String name);

    PasswordResetToken findByEmailId(String name);

}
