package toolkit.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class URLUtil {

    public static String getPath(String baseUrl, String path) {
        if(baseUrl == null){
            return path;
        }
        if(!path.startsWith("/")){
            path = "/" + path;
        }
        if(baseUrl.endsWith("/")){
            return baseUrl.substring(0, baseUrl.length() -1 ) + path;
        }
        return baseUrl + path;
    }

    public static String addQueryParam(String url, String key, String value){
        if(url == null){
            return url;
        }
        if(key == null || value == null){
            return url;
        }
        if(!url.contains("?")){
            url += "?";
        }
        else{
            url += "&";
        }
        try {
            url = url + URLEncoder.encode(key, StandardCharsets.UTF_8.name()) + "=" + URLEncoder.encode(value
            , StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return url;
    }

}
