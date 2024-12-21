package jj.stella.entity.vo.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class UserVo extends User {
	
	private static final long serialVersionUID = 1L;

	private String group;
	private List<String> roleGroups;
	private Collection<? extends GrantedAuthority> roles;
	public UserVo(
		String username, String password, boolean enabled,
		boolean accountNonExpired, boolean credentialsNonExpired,
		boolean accountNonLocked, String group, List<String> roleGroups,
		Collection<? extends GrantedAuthority> authorities
	) {
		
		super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
		
		this.group = group;
		this.roleGroups = roleGroups;
		this.roles = authorities;
		
	}
	
	public String getGroup() {
		return group;
	}
	public List<String> getRoleGroups() {
		return roleGroups;
	}
	public Collection<? extends GrantedAuthority> getRoles() {
		return roles;
	}
	
}