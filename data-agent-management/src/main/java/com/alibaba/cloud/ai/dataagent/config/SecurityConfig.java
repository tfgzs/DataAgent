/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.ai.dataagent.config;

import com.alibaba.cloud.ai.dataagent.properties.BasicAuthProperties;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;

import java.util.List;

/**
 * Basic Auth 安全配置
 */
@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(BasicAuthProperties.class)
@AllArgsConstructor
public class SecurityConfig {

	private final BasicAuthProperties authProperties;

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.authorizeExchange(exchanges -> exchanges.pathMatchers("/api/auth/health")
					.permitAll()
					.anyExchange()
					.authenticated())
			.httpBasic(Customizer.withDefaults())
			.build();
	}

	@Bean
	public MapReactiveUserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
		var user = User.builder()
			.username(authProperties.getUsername())
			.password(passwordEncoder.encode(authProperties.getPassword()))
			.roles(authProperties.getRole())
			.build();
		return new MapReactiveUserDetailsService(user);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		return exchange -> {
			var config = new CorsConfiguration();
			config.setAllowedOrigins(authProperties.getAllowedOrigins());
			config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
			config.setAllowedHeaders(List.of("*"));
			config.setAllowCredentials(true);
			config.setMaxAge(3600L);
			return config;
		};
	}

}
