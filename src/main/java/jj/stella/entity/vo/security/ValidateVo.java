package jj.stella.entity.vo.security;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import jj.stella.util.security.AuthVoDeserializer;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidateVo {
	
	private boolean verify;
	private boolean waiting;
	private boolean reissue;
	
	private String id;
	private String jti;
	private String token;
	
    @JsonRawValue
    @JsonDeserialize(using = AuthVoDeserializer.class)
	private AuthVo authz;
	
	private String message;
	
}