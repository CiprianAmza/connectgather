package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// ELIMINĂ ACESTE IMPORTURI DACĂ LE AI:
// import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
// import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;


// ELIMINĂ ACEST IMPORT DACĂ ÎL AI:
// import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // PasswordEncoder-ul poate rămâne aici pentru moment, dar ideal ar fi să fie gestionat de AuthService.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // ELIMINAT: Bean-ul DataSource, deoarece securitatea utilizatorilor este externalizată
    // @Bean
    // public DataSource dataSource() {
    //     return new EmbeddedDatabaseBuilder()
    //             .setType(EmbeddedDatabaseType.H2)
    //             .addScript("classpath:schema.sql")
    //             .addScript("classpath:data.sql")
    //             .build();
    // }

    // ELIMINAT: Bean-ul UserDetailsService, deoarece autentificarea este externalizată către AuthService
    // @Bean
    // public UserDetailsService userDetailsService(DataSource dataSource) {
    //     JdbcUserDetailsManager users = new JdbcUserDetailsManager(dataSource);
    //     users.setUsersByUsernameQuery("select username, password, enabled from app_users where username = ?");
    //     users.setAuthoritiesByUsernameQuery("select username, authority from authorities where username = ?");
    //     return users;
    // }

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
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    // Al doilea SecurityFilterChain pentru restul aplicației
    @Bean
    @Order(2)
    public SecurityFilterChain applicationFilterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/").permitAll()
                        .requestMatchers("/login").permitAll() // Permite acces la GET și POST /login
                        .requestMatchers("/register").permitAll() // Permite acces la /register dacă ai un controller de înregistrare
                        .anyRequest().authenticated() // Toate celelalte cereri necesită autentificare
                )
                // ELIMINAT: formLogin-ul default al Spring Security
                // deoarece LoginController-ul nostru va gestiona POST /login
                // .formLogin(form -> form
                //         .loginPage("/login")
                //         .defaultSuccessUrl("/events", true)
                //         .permitAll()
                // )
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
