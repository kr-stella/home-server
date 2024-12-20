package jj.stella.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "server.url")
public class ServerProperties {
	
	private String auth;	// server.url.auth
	private String login;	// server.url.login
	private String logout;	// server.url.logout
	
}