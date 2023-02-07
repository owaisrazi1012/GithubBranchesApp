package com.nisum.auth.config;

import com.nisum.auth.domain.enums.RoleName;
import com.nisum.auth.repository.UserRepository;
import com.nisum.auth.security.JwtAuthenticationEntryPoint;
import com.nisum.auth.security.JwtAuthenticationFilter;
import com.nisum.auth.service.impl.CustomUserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
		securedEnabled = true,
		jsr250Enabled = true,
		prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	private final CustomUserDetailsServiceImpl customUserDetailsService;
	private final JwtAuthenticationEntryPoint unauthorizedHandler;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Autowired
	public SecurityConfig(UserRepository userRepository, CustomUserDetailsServiceImpl customUserDetailsService,
						  JwtAuthenticationEntryPoint unauthorizedHandler, JwtAuthenticationFilter jwtAuthenticationFilter) {
		this.customUserDetailsService = customUserDetailsService;
		this.unauthorizedHandler = unauthorizedHandler;
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http.cors().and().csrf().disable()
				.exceptionHandling()
				.authenticationEntryPoint(unauthorizedHandler)
				.and()
				.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and()
				.authorizeRequests()
				.antMatchers(HttpMethod.GET, "/api/*").permitAll()
				.antMatchers(HttpMethod.POST, "/api/auth/*").permitAll()
				.antMatchers(HttpMethod.GET,"/api/users/*").hasAnyAuthority(RoleName.ROLE_ADMIN.name()).and().authorizeRequests()
				.antMatchers("/swagger-ui/*").permitAll()
				.antMatchers("/swagger-resources/**").permitAll()
                .antMatchers("/**").permitAll()
				.anyRequest().authenticated();

		http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

	}
   @Override
	public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
		authenticationManagerBuilder.userDetailsService(customUserDetailsService)
				.passwordEncoder(passwordEncoder());
	}

	@Override
	@Bean(BeanIds.AUTHENTICATION_MANAGER)
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
