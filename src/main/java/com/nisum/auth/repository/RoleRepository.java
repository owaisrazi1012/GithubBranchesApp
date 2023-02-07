package com.nisum.auth.repository;

import com.nisum.auth.domain.entity.Role;
import com.nisum.auth.domain.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
	Optional<Role> findByName(RoleName name);
	Optional<Role> findById(RoleName name);
}
