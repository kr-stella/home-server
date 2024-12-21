package jj.stella.util.security;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import jj.stella.entity.vo.security.AuthVo;

public class AuthVoDeserializer extends JsonDeserializer<AuthVo> {
	
	@Override
	public AuthVo deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		
		String json = p.getText();
		ObjectMapper mapper = new ObjectMapper();
		
		return mapper.readValue(json, AuthVo.class);
		
	}
	
}