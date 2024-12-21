package jj.stella.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jj.stella.entity.vo.security.UserVo;
import jj.stella.filter.auth.AuthLogout;
import jj.stella.filter.csrf.Csrf;
import jj.stella.filter.csrf.CsrfHandler;
import jj.stella.filter.csrf.CsrfRepository;
import jj.stella.filter.jwt.JwtValidate;
import jj.stella.properties.AuthProperties;
import jj.stella.properties.ServerProperties;
import jj.stella.util.security.AccessDenied;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig {
	
	private final AuthProperties authProperties;
	private final ServerProperties serverProperties;
	public SecurityConfig(AuthProperties authProperties, ServerProperties serverProperties) {
		this.authProperties = authProperties;
		this.serverProperties = serverProperties;
	}
	
	private static final String[] BLACK_LIST = {
		"/excel"
	};
	private static final String[] WHITE_LIST = {
		"/resources/**", "/favicon.ico", "/", "/excel"
	};
	
	@Autowired
	private RedisTemplate<String, String> redisTemplate;
	
	// Spring Security 접근권한 설정
	@Bean
	protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		
		String JWT_HEADER = authProperties.getJwt().getHeader();
		String JWT_KEY = authProperties.getJwt().getKey();
		String JWT_NAME = authProperties.getJwt().getName();
		String JWT_DOMAIN = authProperties.getJwt().getDomain();
		String JWT_PATH = authProperties.getJwt().getPath();
		String JWT_EXPIRED = authProperties.getJwt().getExpired();
		
		String CSRF_NAME = authProperties.getCsrf().getName();
		String CSRF_PARAMETER = authProperties.getCsrf().getParameter();
		String CSRF_HEADER = authProperties.getCsrf().getHeader();

		String AUTH_SERVER = serverProperties.getAuth();
		String LOGIN_SERVER = serverProperties.getLogin();
		String LOGOUT_SERVER = serverProperties.getLogout();
		
		return http
		.headers(headers ->
			headers
				.frameOptions(frame -> frame.sameOrigin()
		))
		.cors(cors -> corsConfigurationSource())
//		.csrf(AbstractHttpConfigurer::disable)
		.csrf(csrf ->
			csrf
				.ignoringRequestMatchers(getRequestMatchers(BLACK_LIST))
				// XSRF-TOKEN 발급 ( 쿠키저장 )
				.csrfTokenRepository(new CsrfRepository(CSRF_NAME, CSRF_PARAMETER, CSRF_HEADER))
				.csrfTokenRequestHandler(new CsrfHandler(CSRF_PARAMETER))
		)
		.formLogin(form ->
			form
				.loginPage(LOGIN_SERVER)
		)
		// 로그아웃을 localhost:8080에서 동작시키도록 하기 위함.
		.logout(logout -> logout.disable())
		.authorizeHttpRequests(auth ->
			auth
				// CorsUtil PreFlight 요청은 인증처리 하지 않겠다는 의미
				// CorsUtil PreFlight에는 Authorization 헤더를 줄 수 없으므로 401 응답을 해선안된다.
				.requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
				.requestMatchers(getRequestMatchers(WHITE_LIST)).permitAll()
				// /root경로와 /root하위 경로는
				// [ 예제코드 - 단일 그룹 ] "ROOT" 그룹 사용자만 접근 가능 ( 필요에 따라 로직 변경 )
				// [ 예제코드 - 다중 그룹 OR ] "ROOT", "ADMIN" 그룹 중 하나라도 포함되어 있는 사용자라면 접근 가능
				// [ 예제코드 - 다중 그룹 AND ] "ROOT", "ADMIN" 그룹을 모두 포함하고 있는 사용자라면 접근 가능
				.requestMatchers("/root/**").access(hasGroup("ROOT"))
				.requestMatchers("/admin/**").access(hasAnyGroup("ROOT", "ADMIN"))
//				.requestMatchers("/root/**").access(hasAllGroups("ㅎㅇㅎ", "ROOT", "ADMIN"))
				.anyRequest().authenticated()
		)
		.addFilterBefore(new JwtValidate(
			JWT_HEADER, JWT_KEY, JWT_NAME,
			JWT_DOMAIN, JWT_PATH, JWT_EXPIRED,
			AUTH_SERVER, LOGOUT_SERVER, redisTemplate
		), UsernamePasswordAuthenticationFilter.class)
		// 로그아웃을 localhost:8080에서 동작시키도록 하기 위함.
		.addFilterBefore(new AuthLogout(LOGOUT_SERVER), LogoutFilter.class)
		// 로그인, 로그아웃 이후 응답헤더에 XSRF-TOKEN을 보내기 위함. ( 갱신 )
		.addFilterAfter(new Csrf(), CsrfFilter.class)
		/** 403 에러페이지 - Api가 아닌, 페이지 접근에서 오류가 발생하는 경우 */
        .exceptionHandling(handler -> handler
            .accessDeniedHandler(new AccessDenied())
        )
		.sessionManagement(session -> session 
			.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
			.sessionFixation().changeSessionId()
			.maximumSessions(7)
			.maxSessionsPreventsLogin(false)
			.expiredUrl("/")
		).build();
	};
	
	// 비밀번호 암호화 ( 단방향 복호화 불가능 )
	@Bean
	public PasswordEncoder encoder() {
		
		PasswordEncoder encoder = new BCryptPasswordEncoder();
		return encoder;
		
	};
	
	// CORS 정책 수립
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		
		CorsConfiguration corsConfig = new CorsConfiguration();
		corsConfig.addAllowedOriginPattern("*://*.dev.st2lla.co.kr");
		corsConfig.addAllowedOriginPattern("*://*.intra.st2lla.co.kr");
		corsConfig.setAllowCredentials(true);
		corsConfig.setMaxAge(3600L);
		corsConfig.setAllowedMethods(Arrays.asList("*")); // GET, POST 같은 것
		corsConfig.setAllowedHeaders(
			Arrays.asList(
				"Content-Type",
				"X-XSRF-TOKEN",
				"Authorization",
				"User-Agent",
				"Accept",
				"Content-Length",
				"X-Requested-With"
			)
		);
		
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfig);
		
		return source;
		
	};
	
	// session control
	@Bean
	public SessionRegistry sessionRegistry() {
		return new SessionRegistryImpl();
	};
	
	private RequestMatcher[] getRequestMatchers(String... str) {
		return Arrays.stream(str)
			.map(AntPathRequestMatcher::new)
			.toArray(RequestMatcher[]::new);
	};
	
	/** 사용자 그룹 확인 - 단일 */
	private AuthorizationManager<RequestAuthorizationContext> hasGroup(String group) {
		return (auth, object) -> {
			
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if(authentication != null && authentication.getPrincipal() instanceof UserVo) {
				
				UserVo vo = (UserVo) authentication.getPrincipal();
				String REMOVAL_PREFIX_GROUP = group.replace("GROUP_", "");
				String REMOVAL_PREFIX_GROUP_FROM_VO = vo.getGroup().replace("GROUP_", "");
				
				return new AuthorizationDecision(REMOVAL_PREFIX_GROUP.equals(REMOVAL_PREFIX_GROUP_FROM_VO));
				
			}
			
			return new AuthorizationDecision(false);
			
		};
	}
	
	/** 사용자 그룹 확인 - 하나에 속하는 경우 */
	private AuthorizationManager<RequestAuthorizationContext> hasAnyGroup(String... groups) {
		return (auth, object) -> {
			
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if(authentication != null && authentication.getPrincipal() instanceof UserVo) {
				UserVo vo = (UserVo) authentication.getPrincipal();
				for(String group : groups) {
					
					String REMOVAL_PREFIX_GROUP = group.replace("GROUP_", "");
					String REMOVAL_PREFIX_GROUP_FROM_VO = vo.getGroup().replace("GROUP_", "");
					if(REMOVAL_PREFIX_GROUP.equals(REMOVAL_PREFIX_GROUP_FROM_VO))
						return new AuthorizationDecision(true);
					
				}
			}
			
			return new AuthorizationDecision(false);
			
		};
	}
	
//	/** 사용자 그룹 확인 - 모두 속하는 경우 */
//	private AuthorizationManager<RequestAuthorizationContext> hasAllGroups(String... groups) {
//		return (auth, object) -> {
//			
//			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//			if(authentication != null && authentication.getPrincipal() instanceof UserVo) {
//				
//				UserVo vo = (UserVo) authentication.getPrincipal();
//				List<String> userGroups = vo.getAuthGroups();
//				
//				return new AuthorizationDecision(userGroups.containsAll(Arrays.asList(groups)));
//				
//			}
//			
//			return new AuthorizationDecision(false);
//			
//		};
//	}
	
}