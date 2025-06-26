package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;


import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:schema.sql")
                .addScript("classpath:data.sql")
                .build();
    }

    @Bean
    public UserDetailsService userDetailsService(DataSource dataSource) {
        JdbcUserDetailsManager users = new JdbcUserDetailsManager(dataSource);
        users.setUsersByUsernameQuery("select username, password, enabled from app_users where username = ?");
        users.setAuthoritiesByUsernameQuery("select username, authority from authorities where username = ?");
        return users;
    }

    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowSemicolon(true);
        return firewall;
    }

    // Primul SecurityFilterChain pentru căile Actuator, cu cea mai mare prioritate
    @Bean
    @Order(1)
    public SecurityFilterChain actuatorFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/actuator/**")
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable()); // NOU: Dezactivează explicit CSRF pentru căile Actuator
        return http.build();
    }

    // Al doilea SecurityFilterChain pentru restul aplicației
    @Bean
    @Order(2)
    public SecurityFilterChain applicationFilterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/").permitAll()
                        .requestMatchers("/login").permitAll()
                        // ATENȚIE: regula pentru /actuator/** a fost mutată în actuatorFilterChain

                        // Reguli pentru Participanți
                        .requestMatchers("/participants/new", "/participants/edit/**", "/participants/delete/**").hasRole("ADMIN")
                        .requestMatchers("/participants/**").hasAnyRole("USER", "ADMIN")

                        // Reguli pentru Locații
                        .requestMatchers("/locations/new", "/locations/edit/**", "/locations/delete/**").hasRole("ADMIN")
                        .requestMatchers("/locations/**").hasAnyRole("USER", "ADMIN")

                        // Reguli pentru Evenimente
                        .requestMatchers("/events/new", "/events/edit/**", "/events/delete/**").hasRole("ADMIN")
                        .requestMatchers("/events/**").hasAnyRole("USER", "ADMIN")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/events", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**")); // Aceasta rămâne pentru H2 Console
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }
}
