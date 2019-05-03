package utils;

import java.util.regex.Pattern;

public class ValidChecker {
	public static boolean joinIDCheck(String Id) {
		String key = "^[¤¡-¤¾°¡-ÆRa-zA-Z0-9]*$";
		boolean result = Pattern.matches(key, Id);
		return result;
	}
	
	public static boolean joinPWCheck(String pw) {
		if((pw.matches(".*[a-z].*") || pw.matches(".*[A-Z].*")) && pw.matches(".*[0-9].*")) {
			return true;
		}
		return false;
	}
}
