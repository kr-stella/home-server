package jj.stella.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthVo {
	
	private boolean verify;
	private boolean waiting;
	private boolean reissue;
	
	private String token;
	private String id;
	private String jti;
	private String authz;
	
	private String message;
	
}