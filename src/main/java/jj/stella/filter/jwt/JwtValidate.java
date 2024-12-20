package jj.stella.filter.jwt;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jj.stella.entity.AuthVo;

public class JwtValidate extends OncePerRequestFilter {
	
	private static final String USER_AGENT = "User-Agent";
//	private static final String USER_DEVICE = "User-Device";
	private static final String XHR_HEADER = "X-Requested-With";
	private static final String XHR_HEADER_VALUE = "XMLHttpRequest";
	private static final String ACCEPT_HEADER = "Accept";
	private static final String UNAUTHORIZED_MESSAGE = "Authentication Token does not exists.";
	private static final String AUTH_SERVER_ERROR_MESSAGE = "There is an issue with the Authentication Server.\nPlease try again later.";
	
	private String JWT_HEADER;
	private String JWT_KEY;
	private String JWT_NAME;
	private String JWT_DOMAIN;
	private String JWT_PATH;
	private long JWT_EXPIRED;
	private String AUTH_SERVER;
	private String LOGOUT_SERVER;
	private RedisTemplate<String, String> redisTemplate;
	private final AntPathMatcher pathMatcher = new AntPathMatcher();
	public JwtValidate(
		String JWT_HEADER, String JWT_KEY, String JWT_NAME,
		String JWT_DOMAIN, String JWT_PATH, String JWT_EXPIRED,
		String AUTH_SERVER, String LOGOUT_SERVER, RedisTemplate<String, String> redisTemplate
	) {
		this.JWT_HEADER = JWT_HEADER;
		this.JWT_KEY = JWT_KEY;
		this.JWT_NAME = JWT_NAME;
		this.JWT_DOMAIN = JWT_DOMAIN;
		this.JWT_PATH = JWT_PATH;
		this.JWT_EXPIRED = Long.parseLong(JWT_EXPIRED);
		this.AUTH_SERVER = AUTH_SERVER;
		this.LOGOUT_SERVER = LOGOUT_SERVER;
		this.redisTemplate = redisTemplate;
	};
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
		throws ServletException, IOException {
		
		String path = request.getRequestURI();
		if(isSkipPath(path)) {
			chain.doFilter(request, response);
			return;
		}
		
		/** XHR에서 토큰설정하는 것은 무시해야 하므로 Cookie에서만 토큰 추출 */
		String token = extractToken(request);
		/** 토큰이 비어있으면 무조건 잘못된 것임 */
		if(token == null) {
			/**
			 * 요청의 유형에 따라서 응답하는 방법이 달라짐.
			 * (1) 사용자가 Ajax나 Axios등을 활용해서 Request가 발생하는 경우
			 * 응답은 문제가 발생했다는 응답을 Response에 맘아서 반환해야 함.
			 * 즉, 반환받은 경고창을 보여주고 존재하는 모든 검증을 파기하도록 로그아웃을 실행 > 모든 검증 파기
			 * 
			 * (2) 사용자가 인터넷 주소창을 활용해서 Request가 발생하는 경우
			 * 즉시 존재하는 모든 검증을 파기하고 로그인페이지로 Redirect해야 함.
			 */
			unauthorizedResponse(response, isXHRRequest(request), UNAUTHORIZED_MESSAGE);
			return;
		};
		
		/** 토큰이 존재하면 검증로직 실행 */
		AuthVo auth = validateToken(request, token);
		token = auth.getToken();
		
		if(auth.isVerify()) {
			
			/** 검증이 완료되면 인증정보 Security 설정 */
			authenticated(token, auth);
			
			/** Cookie, Redis 갱신 */
			setAuth(response, token, auth.getJti(), auth.isReissue());
			
			/** Spring Security 로직 실행 */
			chain.doFilter(request, response);
			
		} else invalidTokenResponse(request, response, auth);
		
	};
	
	/** 검증서버에 접근했을 때 검증로직을 건너뛰는 경로 */
	private boolean isSkipPath(String path) {
		return pathMatcher.match("/resources/**", path)
			|| pathMatcher.match("/favicon.ico", path)
			|| pathMatcher.match("/excel", path);
	};
	
	/**
	 * Token 추출
	 * Request Header에서 추출하지 않는 이유는
	 * JS나 스크립트로 요청할 수 없게 설정했기 때문.
	 * */
	private String extractToken(HttpServletRequest request) {
		
//		String token = request.getHeader(JWT_HEADER);
//		if(token != null && token.startsWith(JWT_KEY))
//			return token.substring(JWT_KEY.length());
		if(request.getCookies() != null) {
			return Arrays.stream(request.getCookies())
					.filter(cookie -> JWT_NAME.equals(cookie.getName()))
					.findFirst()
					.map(Cookie::getValue)
					.orElse(null);
		}
		
		return null;
		
	};
	
	/**
	 * 요청유형 구분
	 * (1) 사용자가 Ajax나 Axios등을 활용해서 Request가 발생하는 경우
	 * (2) 사용자가 인터넷 주소창을 활용해서 Request가 발생하는 경우
	 * 
	 * = 아래의 조건이 모두 충족하면 XHR 요청으로 판단함.
	 * */
	private boolean isXHRRequest(HttpServletRequest request) {
		String xhr = request.getHeader(XHR_HEADER);
		String accept = request.getHeader(ACCEPT_HEADER);
		return ((xhr != null && XHR_HEADER_VALUE.equals(xhr))
				&& (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)));
	};
	
	/**
	 * 요청의 유형에 따라서 응답하는 방법이 달라짐.
	 * (1) 사용자가 Ajax나 Axios등을 활용해서 Request가 발생하는 경우
	 * 응답은 문제가 발생했다는 응답을 Response에 맘아서 반환해야 함.
	 * 즉, 반환받은 경고창을 보여주고 존재하는 모든 검증을 파기하도록 로그아웃을 실행 > 모든 검증 파기
	 * 
	 * (2) 사용자가 인터넷 주소창을 활용해서 Request가 발생하는 경우
	 * 즉시 존재하는 모든 검증을 파기하고 로그인페이지로 Redirect해야 함.
	 */
	private void unauthorizedResponse(HttpServletResponse response, boolean isXHR, String str) throws IOException {
		
		if(!isXHR)
			response.sendRedirect(LOGOUT_SERVER);
		
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		
		Map<String, Object> map = new HashMap<>();
		
		map.put("code", HttpServletResponse.SC_UNAUTHORIZED);
		map.put("type", "UNAUTHORIZED");
		map.put("str", str);
		
		ObjectMapper mapper = new ObjectMapper();
		String result = mapper.writeValueAsString(map);
		response.getWriter().write(result);
		response.getWriter().flush();
		
	};
	
	/** 검증로직 실행 */
	private AuthVo validateToken(HttpServletRequest request, String token) {
		
		AuthVo auth = new AuthVo();
		RestTemplate template = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(JWT_HEADER, JWT_KEY + token);
		headers.set(USER_AGENT, request.getHeader(USER_AGENT));
//		headers.set(USER_DEVICE, request.getHeader(USER_DEVICE));
//		headers.set("isXHR", isXHRRequest(request)? "true":"false");
		
		try {
			
			HttpEntity<String> entity = new HttpEntity<>("", headers);
			ResponseEntity<AuthVo> response = template.exchange(
				AUTH_SERVER, HttpMethod.GET, entity, AuthVo.class
			);
			
			AuthVo body = response.getBody();
			
			auth.setReissue(body.isReissue());
			auth.setId(body.getId());
			auth.setJti(body.getJti());
			auth.setAuthz(body.getAuthz());
			auth.setToken(body.getToken());
			auth.setVerify(true);
			
		} catch(HttpClientErrorException e) {
			auth.setVerify(false);
			auth.setWaiting(false);
			auth.setMessage(e.getResponseBodyAsString());
		} catch(RestClientException e) {
			auth.setVerify(false);
			auth.setWaiting(true);
			auth.setMessage(AUTH_SERVER_ERROR_MESSAGE);
		}
		
		return auth;
		
	};
	
	/** 검증로직이 통과된 경우 인증정보를 만들어서 적용 */
	private void authenticated(String token, AuthVo auth) {
		
		List<SimpleGrantedAuthority> authz = Arrays.stream(auth.getAuthz().split(", "))
				.map(String::trim) // 공백 제거
				.map(role -> new SimpleGrantedAuthority(role)) // 각 역할을 SimpleGrantedAuthority 객체로 변환
				.collect(Collectors.toList());
		
		User principal = new User(auth.getId(), token, authz);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
			principal, token, principal.getAuthorities()
		);
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
	};
	
	/** Cookie, Redis 갱신 */
	private void setAuth(HttpServletResponse response, String token, String jti, boolean isReissue) {
		setCookie(token, response);
		if(isReissue)
			setRedis(jti);
	};
	
	/** Redis 갱신 로직 */
	private void setRedis(String jti) {
		ValueOperations<String, String> ops = redisTemplate.opsForValue();
		ops.set(jti, "true", JWT_EXPIRED, TimeUnit.MILLISECONDS);
	};
	
	/** Cookie 갱신 로직 */
	private void setCookie(String token, HttpServletResponse response) {
		
		Cookie cookie = new Cookie(JWT_NAME, token);
		
		/**
		 * 쿠키의 유효기간은 반년으로 설정하고
		 * "검증서버의 '/validate'"가 성공하거나
		 * "검증서버에서 로그인서버로의 재발급요청 '/refresh'"이 성공하면
		 * "검증서버에서" 쿠키의 생명을 연장.
		 * */
		cookie.setDomain(JWT_DOMAIN);
		cookie.setMaxAge((int) (((JWT_EXPIRED * 8 * 365) / 2) / 1000));
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath(JWT_PATH);
		
		response.addCookie(cookie);
		
	};
	
	/** 검증로직이 실패한 경우 응답 반환 */
	private void invalidTokenResponse(HttpServletRequest request, HttpServletResponse response, AuthVo auth) throws IOException {
		/** 검증실패 */
		if(!auth.isWaiting()) unauthorizedResponse(response, isXHRRequest(request), auth.getMessage());
		/** 서버연결 실패 */
		else disconnectAuthServerResponse(response, isXHRRequest(request), auth.getMessage());
	};
	
	private void disconnectAuthServerResponse(HttpServletResponse response, boolean isXHR, String str) throws IOException {
		
		if(!isXHR)
			response.sendRedirect(LOGOUT_SERVER);
		
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		
		Map<String, Object> map = new HashMap<>();
		
		map.put("code", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		map.put("type", "AUTH_SERVER_CANNOT_CONNECT");
		map.put("str", str);
		
		ObjectMapper mapper = new ObjectMapper();
		String result = mapper.writeValueAsString(map);
		response.getWriter().write(result);
		response.getWriter().flush();
		
	};
	
}