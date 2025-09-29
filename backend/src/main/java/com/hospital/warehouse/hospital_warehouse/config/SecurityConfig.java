package com.hospital.warehouse.hospital_warehouse.config;

import com.hospital.warehouse.hospital_warehouse.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;
    private final UserService userService;

    @Value("${VPS_HOST:localhost}")
    private String vpsHost;

    @Autowired
    public SecurityConfig(JwtRequestFilter jwtRequestFilter, @Lazy UserService userService) {
        this.jwtRequestFilter = jwtRequestFilter;
        this.userService = userService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userService).passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // ========== PUBLIC ENDPOINTS ==========
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ========== API ENDPOINTS với /api prefix ==========
                        .requestMatchers("/api/kho/**").hasAnyRole("ADMIN", "QUAN_LY_KHO", "NHAN_VIEN_KHO")
                        .requestMatchers("/api/hang-hoa/**").hasAnyRole("ADMIN", "QUAN_LY_KHO", "NHAN_VIEN_KHO")
                        .requestMatchers("/api/vi-tri-kho/**").hasAnyRole("ADMIN", "QUAN_LY_KHO", "NHAN_VIEN_KHO")
                        .requestMatchers("/api/lo-hang/**").hasAnyRole("ADMIN", "QUAN_LY_KHO", "NHAN_VIEN_KHO")
                        .requestMatchers("/api/phieu-nhap/**").hasAnyRole("ADMIN", "QUAN_LY_KHO", "NHAN_VIEN_KHO")
                        .requestMatchers("/api/phieu-xuat/**").hasAnyRole("ADMIN", "QUAN_LY_KHO", "NHAN_VIEN_KHO")
                        .requestMatchers("/api/nha-cung-cap/**").hasAnyRole("ADMIN", "QUAN_LY_KHO", "NHAN_VIEN_KHO")
                        .requestMatchers("/api/danh-muc/**").hasAnyRole("ADMIN", "QUAN_LY_KHO", "NHAN_VIEN_KHO")
                        .requestMatchers("/api/don-vi-tinh/**").hasAnyRole("ADMIN", "QUAN_LY_KHO", "NHAN_VIEN_KHO")
                        .requestMatchers("/api/khoa-phong/**").hasAnyRole("ADMIN", "QUAN_LY_KHO", "NHAN_VIEN_KHO")

                        // ========== USER MANAGEMENT APIs ==========
                        .requestMatchers("/api/user/current").hasAnyRole("ADMIN", "QUAN_LY_KHO", "NHAN_VIEN_KHO")
                        .requestMatchers("/api/user/current/password").hasAnyRole("ADMIN", "QUAN_LY_KHO", "NHAN_VIEN_KHO")
                        .requestMatchers("/api/user/**").hasRole("ADMIN")

                        // ========== BÁO CÁO APIs ==========
                        .requestMatchers("/api/bao-cao/**").hasAnyRole("ADMIN", "QUAN_LY_KHO")

                        // ========== DEFAULT ==========
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.getWriter().write("{\"error\": \"Truy cập không được phép. Vui lòng đăng nhập.\"}");
                        })
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        List<String> allowedOrigins = Arrays.asList(
                "http://localhost:3000",
                "http://localhost",
                "http://" + vpsHost,
                "https://" + vpsHost,
                "http://" + vpsHost + ":3000",
                "https://" + vpsHost + ":3000"
        );

        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}