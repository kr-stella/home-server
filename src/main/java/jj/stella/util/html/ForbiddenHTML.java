package jj.stella.util.html;

public class ForbiddenHTML {
	public static String getCode() {
		return "<!DOCTYPE html>\n"
				+ "<html lang=\"ko\">\n"
				+ "    <head>\n"
				+ "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n"
				
				+ "        <meta http-equiv=\"Content-Script-Type\" content=\"text/javascript\">\n"
				+ "        <meta http-equiv=\"Content-Style-Type\" content=\"text/css\">\n"
				+ "        <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n"
				+ "        <meta http-equiv=\"Expires\" content=\"-1\">\n"
				+ "        <meta http-equiv=\"Pragma\" content=\"no-cache\">\n"
				+ "        <meta http-equiv=\"Cache-Control\" content=\"no-cache\">\n"
				
				+ "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n"
				
				+ "        <meta property=\"og:type\" content=\"website\">\n"
				+ "        <meta property=\"og:title\" content=\"St2lla\">\n"
				+ "        <meta property=\"og:image\" content=\"/resources/image/logo.png\">\n"
				
				+ "        <link rel=\"shortcut icon\" href=\"/resources/image/favicon.ico?v=20240208\" />\n"
				+ "        <title>St2lla :: Forbidden</title>\n"
				+ "        <link href=\"/resources/error/error.css\" rel=\"stylesheet\">\n"
				+ "    </head>\n"
				+ "<body>\n"
				
				+ "<noscript>이 페이지를 실행하려면 자바스크립트를 사용해야 합니다.</noscript>\n"
				+ "<div class=\"root\" style=\"visibility:hidden; display:none;\">\n"
				+ "    <div class=\"main_wrap\">\n"
				+ "        <div class=\"main_container\">\n"
				+ "            <div class=\"title\">\n"
				+ "                <h1>Access Denied</h1>\n"
				+ "                <h1>Access Denied</h1>\n"
				+ "                <h1>Access Denied</h1>\n"
				+ "                <h1>Access Denied</h1>\n"
				+ "            </div>\n"
				+ "            <div class=\"conts\">\n"
				+ "                <span>페이지 접근에 필요한 권한이 존재하지 않아 요청을 처리할 수 없습니다.</span>\n"
				+ "                <span>담당자에게 문의해 주시기 바랍니다.</span>\n"
				+ "                <div class=\"button_box\">\n"
				+ "                    <a href=\"/\">HOME</a>\n"
				+ "                    <button type=\"button\">REPORT</button>\n"
				+ "                </div>\n"
				+ "            </div>\n"
				+ "        </div>\n"
				+ "    </div>\n"
				+ "</div>\n"
				+ "<script defer src=\"/resources/error/error.js\"></script>\n"
				+ "</body>\n"
				+ "</html>\n";
	}
}