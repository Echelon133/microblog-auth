package ml.echelon133.microblogauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

@SpringBootApplication
public class MicroblogauthApplication {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedHeaders(Collections.singletonList("*"));
		configuration.setAllowedOrigins(Collections.singletonList("*"));
		configuration.setAllowedMethods(Arrays.asList("GET", "OPTIONS", "POST", "PUT"));
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Configuration
	public static class RedisConfig {

		private final Environment env;

		@Autowired
		public RedisConfig(Environment env) {
			this.env = env;
		}

		@Bean
		public JedisConnectionFactory jedisConnectionFactory() {
			String password = Objects.requireNonNull(env.getProperty("spring.redis.password"));

			RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
			config.setPassword(RedisPassword.of(password));
			return new JedisConnectionFactory(config);
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(MicroblogauthApplication.class, args);
	}

}
