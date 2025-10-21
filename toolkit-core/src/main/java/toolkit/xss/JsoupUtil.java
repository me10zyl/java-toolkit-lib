package toolkit.xss;

import cn.hutool.core.util.StrUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

import java.io.IOException;

/**
 * <p>
 * xss非法标签过滤工具类
 * 过滤html中的xss字符
 */
public class JsoupUtil {


    /**
     * 配置过滤化参数,不对代码进行格式化
     */
    private static final Document.OutputSettings OUTPUT_SETTINGS = new Document.OutputSettings().prettyPrint(false);


    public static String clean(String content) {
        if (StrUtil.isNotBlank(content)) {
            content = content.trim();
        }
        return Jsoup.clean(content, "", Safelist.basicWithImages().preserveRelativeLinks(true), OUTPUT_SETTINGS);
    }

    public static void main(String[] args) throws IOException {
        String text = "{\"allowLoginAdmin\":true,\"code\":\"ROLE_ADMIN\",\"defaultRole\":false,\"description\":\"这是系统默认的管理角色<a href=\\\"http://www.baidu.com/a\\\" onclick=\\\"alert(1);\\\">sss</a>\",\"id\":1,\"name\":\"系统管理员\",\"orderNo\":0,\"status\":false,\"updateDate\":\"2019-07-25 17:47:07\",\"_index\":0,\"_rowKey\":3}";
        System.out.println(clean(text));
    }
}