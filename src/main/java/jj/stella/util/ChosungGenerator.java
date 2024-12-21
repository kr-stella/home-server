package jj.stella.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jj.stella.properties.QueryProperties;

public class ChosungGenerator {
	
	private static char getChosung(char v) {
		
		int code = v - 44032;
		if(code < 0 || code > 11171)
			return v;
		
		return QueryProperties.INIT_CHOSUNG[code / 588];
		
	};
	
	private static void getChosungRecursive(Set<String> res, String name, String str, int cnt) {
		
		if(cnt == str.length()) {
			res.add(name);
			return;
		}
		
		char cur = str.charAt(cnt);
		char chosung = getChosung(cur);
		
		getChosungRecursive(res, name + chosung, str, cnt + 1);
		getChosungRecursive(res, name + cur, str, cnt + 1);
		
	};
	
	public static List<String> createChosung(String v) {
		
		Set<String> results = new HashSet<>();
		getChosungRecursive(results, "", v, 0);
		
		return new ArrayList<>(results);
		
	};
	
}