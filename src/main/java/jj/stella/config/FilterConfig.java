package jj.stella.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import jj.stella.filter.TrailingSlashFilter;

/** TrailingSlashFilter가 기존방법처럼 하면 서버구동이 안되는 오류가 있음. */
@Configuration
public class FilterConfig {
	
	@Bean
	public FilterRegistrationBean<TrailingSlashFilter> trailingSlashFilterRegistration() {
		
		FilterRegistrationBean<TrailingSlashFilter> registration = new FilterRegistrationBean<>();
		/** 필터 인스턴스 생성 */
		registration.setFilter(new TrailingSlashFilter());
		/** 모든 요청에 대해 필터 적용 */
		registration.addUrlPatterns("/*");
		/** 우선순위 설정 */
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
		
		return registration;
		
	};
	
}