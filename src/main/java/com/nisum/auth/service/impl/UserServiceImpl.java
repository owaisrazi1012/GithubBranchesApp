package com.nisum.auth.service.impl;

import static com.nisum.util.Constants.INVALID_TOKEN;
import static com.nisum.util.Constants.NO_RECORD_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import com.nisum.api.preset.domain.dto.EmailDetails;
import com.nisum.api.preset.domain.entity.PasswordResetToken;
import com.nisum.api.preset.repository.PasswordResetTokenRepository;
import com.nisum.auth.domain.dto.ForgotRequest;
import com.nisum.auth.domain.entity.User;
import com.nisum.auth.exception.InvalidUserException;
import com.nisum.auth.repository.UserRepository;
import com.nisum.exception.custom.UserAccessDeniedException;
import com.nisum.exception.custom.BadRequestException;
import com.nisum.exception.custom.ResourceNotFoundException;
import com.nisum.exception.custom.UnauthorizedException;
import com.nisum.auth.domain.entity.Role;
import com.nisum.auth.domain.enums.RoleName;
import com.nisum.auth.domain.dto.ApiResponse;
import com.nisum.auth.repository.RoleRepository;
import com.nisum.auth.security.UserPrincipal;
import com.nisum.auth.service.UserService;
import com.nisum.service.PostJenkinsActionService;
import com.nisum.util.GenericUtils;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private PasswordResetTokenRepository passwordResetTokenRepository;

	@Autowired
	private PostJenkinsActionService postJenkinsActionService;

	@Override
	public User addUser(User user, UserPrincipal currentUser) {
		if (userRepository.existsByUsername(user.getUsername())) {
			ApiResponse apiResponse = new ApiResponse(Boolean.FALSE, "Username is already taken");
			throw new DataIntegrityViolationException(apiResponse.getMessage());
		}

		if (userRepository.existsByEmail(user.getEmail())) {
			ApiResponse apiResponse = new ApiResponse(Boolean.FALSE, "Email is already taken");
			throw new DataIntegrityViolationException(apiResponse.getMessage());
		}
		
		if (validatePassword(user.getPassword())) {
			user.setPassword(passwordEncoder.encode(user.getPassword()));
		}
		user.setCreatedBy(currentUser.getUsername());
		return userRepository.save(user);
	}

	@Override
	public User updateUser(User newUser, Long id, UserPrincipal currentUser) {
		User user = userRepository.getUserById(id);
		if ((user.getId().equals(currentUser.getId())
				|| currentUser.getAuthorities().contains(new SimpleGrantedAuthority(RoleName.ROLE_ADMIN.toString())))
				&& validatePassword(newUser.getPassword()) && user.getCreatedBy().equals(currentUser.getUsername())) {
			user.setUsername(newUser.getUsername());
			user.setEmail(newUser.getEmail());
			user.setCanBuild(newUser.getCanBuild());
			user.setCanCreate(newUser.getCanCreate());
			user.setIsActive(newUser.getIsActive());
			user.setCanView(newUser.getCanView());
			user.setCanDelete(newUser.getCanDelete());
			user.setCanEdit(newUser.getCanEdit());
			user.setRoles(newUser.getRoles());
			user.setPassword(passwordEncoder.encode(newUser.getPassword()));

			return userRepository.save(user);
		}

		ApiResponse apiResponse = new ApiResponse(Boolean.FALSE, "You don't have permission to update profile of: " + id);
		throw new UnauthorizedException(apiResponse);

	}

	@Override
	public User changePassword(User newUser, ForgotRequest forgotRequest) {

			if (passwordEncoder.matches(forgotRequest.getPassword(), newUser.getPassword())) {
				throw new BadRequestException(new ApiResponse(Boolean.FALSE,"New password cannot be same as old password"));
			}

			if (validatePassword(forgotRequest.getPassword())) {
				newUser.setPassword(passwordEncoder.encode(forgotRequest.getPassword()));
			}
			return userRepository.save(newUser);

	}

	@Override
	public ApiResponse deleteUser(Long id, UserPrincipal currentUser) {
		User user = userRepository.getUserById(id);
		if ((user.getId().equals(currentUser.getId()) || !currentUser.getAuthorities()
				.contains(new SimpleGrantedAuthority(RoleName.ROLE_ADMIN.toString())))
				&& !user.getCreatedBy().equals(currentUser.getUsername())) {
			ApiResponse apiResponse = new ApiResponse(Boolean.FALSE, "You don't have permission to delete profile of: " + id);
			throw new UserAccessDeniedException(apiResponse);
		}

		userRepository.deleteById(user.getId());

		return new ApiResponse(Boolean.TRUE, "You successfully deleted profile of: " + id);
	}

	@Override
	public User getUserByName(String username) {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("User", "id", username));
	}

	@Override
	public User getUserByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new InvalidUserException(UNAUTHORIZED.value(),NO_RECORD_FOUND,new Throwable()));
	}

	@Override
	public User forgotPasswordToken(User user) {

		try {
			String generatedToken = RandomStringUtils.random(6, true, true);

			PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByEmailId(user.getEmail());

			if(null == passwordResetToken) {
				passwordResetToken = new PasswordResetToken();
			}

			passwordResetToken.setToken(generatedToken);
			passwordResetToken.setEmailId(user.getEmail());
			passwordResetToken.setExpiredAt(LocalDateTime.now().plusMinutes(5));

			passwordResetTokenRepository.save(passwordResetToken);

			EmailDetails emailDetails = new EmailDetails();
			emailDetails.setRecipients(user.getEmail());
			emailDetails.setSubject("Password Reset");
			emailDetails.setBody("Verification code: " + generatedToken);

			postJenkinsActionService.sendEmail(emailDetails);
		} catch (Exception exception) {
			exception.printStackTrace();
			throw new RuntimeException(exception.getLocalizedMessage());
		}
		return user;
	}

	@Override
	public boolean verifyToken(ForgotRequest request) {
		PasswordResetToken passwordResetToken =  passwordResetTokenRepository.findByToken(request.getToken())
				.orElseThrow(() -> new InvalidUserException(BAD_REQUEST.value(),INVALID_TOKEN,new Throwable()));

		if (passwordResetToken.getToken().equals(request.getToken()) && passwordResetToken.getEmailId().equalsIgnoreCase(request.getEmailId())) {

			if (LocalDateTime.now().isBefore(passwordResetToken.getExpiredAt())) {
				log.info("reset password token validated for email: {}", request.getEmailId());
				return true;
			}
		}
		return false;
	}

	@Override
	public List<Role> getRoles() {
		return roleRepository.findAll();
	}

	@Override
	public Map<String, Object> getUsers(UserPrincipal currentUser, int page, int size) {

		List<User> userList=userRepository.findByCreatedBy(currentUser.getUsername());
		userList = userList.stream().filter(user -> !user.getUsername().equals(currentUser.getUsername())).collect(Collectors.toList());

		return GenericUtils.getPaginatedResponse(GenericUtils.getPages(userList,page,size), page, size,userList.size());

	}

	private boolean validatePassword(String password) {
		if (password.length() < 8) {
			ApiResponse apiResponse = new ApiResponse(Boolean.FALSE, "Password must be at least 8 characters long");
			throw new DataIntegrityViolationException(apiResponse.getMessage());
		}
		return true;
	}

}
