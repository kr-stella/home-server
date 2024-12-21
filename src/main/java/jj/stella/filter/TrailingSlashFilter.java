package jj.stella.filter;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class TrailingSlashFilter extends OncePerRequestFilter {
	
	@Override
	protected void doFilterInternal(
		HttpServletRequest req, 
		HttpServletResponse res, 
		FilterChain chain
	) throws ServletException, IOException {
		
		String uri = req.getRequestURI();
		String url = ServletUriComponentsBuilder.fromRequest(req).build().toString();
		if(url.endsWith("/") && !uri.equals("/")) {
			
			url = url.substring(0, url.length() - 1);
			
			res.setHeader(HttpHeaders.LOCATION, url);
			res.setStatus(HttpStatus.MOVED_PERMANENTLY.value());
			
		} else if(uri.equals("")) {
			res.setHeader(HttpHeaders.LOCATION, url + "/");
			res.setStatus(HttpStatus.MOVED_PERMANENTLY.value());
		} else chain.doFilter(req, res);
		
	};
	
}