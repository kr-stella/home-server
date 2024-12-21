package jj.stella.filter.auth;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthLogout implements Filter {
	
	private String LOGOUT_SERVER;
	public AuthLogout(String LOGOUT_SERVER) {
		this.LOGOUT_SERVER = LOGOUT_SERVER;
	};
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
		throws IOException, ServletException {
		
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		
		/**
		 * POST요청은 Axios를 통해 요청이 들어오기 때문에
		 * Redirect하는 경로를 Return 해줘야 한다.
		 */
		if("/logout".equals(request.getRequestURI())
			&& "POST".equalsIgnoreCase(request.getMethod())
		) {
			
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_OK);
			
			Map<String, Object> map = new HashMap<>();
			map.put("redirect", LOGOUT_SERVER);
			
			ObjectMapper mapper = new ObjectMapper();
			String result = mapper.writeValueAsString(map);
			response.getWriter().write(result);
			response.getWriter().flush();
			
			return;
			
		}
		
		chain.doFilter(request, response);
		
	};
	
	// 필터 초기화 및 파괴 메서드는 필요에 따라 구현
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {};
	
	@Override
	public void destroy() {};
	
}