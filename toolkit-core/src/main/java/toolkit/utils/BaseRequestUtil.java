package toolkit.utils;

import com.alibaba.fastjson.JSONObject;
import com.yilnz.surfing.core.SurfHttpRequest;
import com.yilnz.surfing.core.SurfHttpRequestBuilder;
import com.yilnz.surfing.core.SurfSpider;
import com.yilnz.surfing.core.basic.Html;
import com.yilnz.surfing.core.basic.Page;
import com.yilnz.surfing.core.site.Site;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseRequestUtil<T> {

    private String baseUrl;
    private Logger logger;

    public BaseRequestUtil(String baseUrl) {
        this.baseUrl = baseUrl;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public void handleRequest(JSONObject body, SurfHttpRequest req){

    }

    public abstract T handleResult(Html result, Class<T> returnClass);

    public T request(String path, String body, Class<T> returnClass) {
        String url = URLUtil.getPath(baseUrl, path);
        JSONObject jsonObject = JSONObject.parseObject(body);
        StringBuilder sb = new StringBuilder();
        sb.append("发起HTTP请求:");
        sb.append(" ").append(url);
        try {
            SurfHttpRequestBuilder builder = SurfHttpRequestBuilder.create(url, "POST");
            SurfHttpRequest req = builder.build();
            req.addHeader("Content-Type", "application/json; charset=utf-8");
            req.setBody(jsonObject.toJSONString());
            handleRequest(jsonObject, req);
            sb.append(" 请求体: ").append(req.getBody());
            Page page = SurfSpider.create()
                    .setSite(Site.me().setRetryTimes(0)).addRequest(req)
                    .request().get(0);
            Html html = page.getHtml();
            sb.append(" 结果:").append(html.get());
            return handleResult(html, returnClass);
        } finally {
            logger.info(sb.toString());
        }
    }
}