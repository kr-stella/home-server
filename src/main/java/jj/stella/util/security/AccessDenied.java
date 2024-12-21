package jj.stella.util.security;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jj.stella.util.html.ForbiddenHTML;

public class AccessDenied implements AccessDeniedHandler {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException {
	
		String accept = request.getHeader("Accept");
		response.setCharacterEncoding("UTF-8");
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		
		if(isApi(accept)) htmlResponse(response);
		else jsonResponse(response, request.getRequestURI());
		
	}
	
	/** Api로 호출한 요청인지 아닌지 구분 */
	private boolean isApi(String header) {
		return header != null && header.contains(MediaType.TEXT_HTML_VALUE);
	}
	
	/** HTML 코드로 응답 - 도메인으로 접근하는 경우 */
	private void htmlResponse(HttpServletResponse response) throws IOException {
		response.setContentType(MediaType.TEXT_HTML_VALUE);
		response.getWriter().write(ForbiddenHTML.getCode());
	}
	
	/** HTML 코드로 응답 - Api로 접근하는 경우 */
	private void jsonResponse(HttpServletResponse response, String url) throws IOException {
		
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		String jsonResponse = String.format(
			"{\"code\": 403, \"type\": \"ACCESS_DENIED\", \"str\": \"%s\"}",
			"'" + url + "' Api 호츨에 필요한 권한이 존재하지 않습니다."
		);
		
		response.getWriter().write(jsonResponse);
		
	}
	
}