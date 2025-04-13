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
                        .ignoringRequestMatchers("/shop/**", "/api/**", "/san-pham/**", "/addProduct")) // Disable CSRF cho REST/API
                .authorizeHttpRequests(auth -> auth
                        // ✅ Public access (ai cũng xem được)
                        .requestMatchers("/login", "/dang-ky", "/quen-mat-khau","/dat-lai-mat-khau","/doi-mat-khau","/trangchu/**", "/css/**","/uploads/**","/upload/**", "/js/**", "/carousel/**").permitAll()
                        .requestMatchers("/shop/**", "/api/**").permitAll()

                        // ✅ Admin-only pages
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // ✅ Admin và Nhân viên đều truy cập
                        .requestMatchers("/quan-ly/**").hasAnyRole("ADMIN", "EMPLOYEE")

                        // ✅ Khách hàng truy cập
                        .requestMatchers("/khach-hang/**").hasRole("CUSTOMER")

                        // ✅ Các trang còn lại yêu cầu đăng nhập
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)) // Luôn tạo session
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler((request, response, authentication) -> {
                            String role = authentication.getAuthorities().iterator().next().getAuthority();
                            if (role.equals("ROLE_ADMIN")) {
                                response.sendRedirect("/admin/thong-ke");
                            } else if (role.equals("ROLE_EMPLOYEE")) {
                                response.sendRedirect("/quan-ly/ban-hang");
                            } else {
                                response.sendRedirect("/shop/homepage");
                            }
                        })
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                // 🟡 Thêm đoạn này để xử lý khi không đủ quyền
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendRedirect("/403?unauthorized=true");
                        })
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
