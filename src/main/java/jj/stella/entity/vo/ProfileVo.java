package jj.stella.entity.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileVo {
	
	private int ino;					// 사용자 고유번호
	private String id;					// 사용자 ID
	private String name;				// 사용자 이름
	private String department;			// 사용자 부서
	private boolean rtu;				// Root User의 약자, 슈퍼 관리자 계정 여부
	
}