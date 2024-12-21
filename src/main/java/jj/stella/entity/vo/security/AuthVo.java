package jj.stella.entity.vo.security;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthVo {
	
	/** 사용자 그룹 */
	private String userGroup;
	/** 권한 그룹 */
	private List<GroupVo> roleGroups;
	/** 권한 리스트 */
	private List<RoleVo> roles;
	
}