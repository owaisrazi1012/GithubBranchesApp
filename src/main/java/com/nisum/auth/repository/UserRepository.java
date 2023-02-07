package com.nisum.auth.repository;

import com.nisum.auth.domain.entity.User;
import com.nisum.auth.security.UserPrincipal;
import com.nisum.exception.custom.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByUsername(@NotBlank String username);

	Optional<User> findById(@NotBlank Long id);
	Optional<User> findByEmail(@NotBlank String email);

	Boolean existsByUsername(@NotBlank String username);

	Boolean existsByEmail(@NotBlank String email);

	Optional<User> findByUsernameOrEmail(String username, String email);

	List<User> findByCreatedBy(String createdBy);

	default User getUser(UserPrincipal currentUser) {
		return getUserByName(currentUser.getUsername());
	}

	default User getUserByName(String username) {
		return findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
	}
	default User getUserById(Long id) {
		return findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
	}
}
