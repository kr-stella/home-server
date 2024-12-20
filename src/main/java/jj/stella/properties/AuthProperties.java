package jj.stella.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {
	
	// auth.jwt.* 매핑
	private Jwt jwt;
	
	@Getter
	@Setter
	public static class Jwt {
		
		private String header;		// auth.jwt.header
		private String key;			// auth.jwt.key
		private String name;		// auth.jwt.name
		private String domain;		// auth.jwt.domain
		private String path;		// auth.jwt.path
		private String expired;		// auth.jwt.expired
		
	};
	
	// auth.csrf.* 매핑
	private Csrf csrf;
	
	@Getter
	@Setter
	public static class Csrf {
		
		private String name;		// auth.csrf.name
		private String parameter;	// auth.csrf.parameter
		private String header;		// auth.csrf.header
		
	};
	
}