package com.example.datnsd26.config;

import com.example.datnsd26.repository.TaiKhoanRepository;
import com.example.datnsd26.services.CustomUserDetailsServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/shop/**", "/api/**")) // Disable CSRF if using REST API
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/shop/**", "/error/**", "/**", "/api/**").permitAll()  // Public access
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "EMPLOYEE")  // Requires authentication
                        .requestMatchers("/admin/ban-hang").hasRole("EMPLOYEE") // Only Employee role
                        .requestMatchers("/doi-mat-khau").permitAll() // Cho phép đổi mật khẩu mà không cần đăng nhập
                        .anyRequest().authenticated() // All other pages require login
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)) // Luôn tạo session khi truy cập ứng dụng
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/admin/get-all-user", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .build();
    }

    @Bean
    public UserDetailsService userDetailsService(TaiKhoanRepository taiKhoanRepository) {
        return new CustomUserDetailsServices(taiKhoanRepository);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Encrypt passwords
    }

    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            BCryptPasswordEncoder passwordEncoder) throws Exception {

        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(authProvider);
    }
}
