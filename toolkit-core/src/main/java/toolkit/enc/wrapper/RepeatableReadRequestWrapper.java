package toolkit.enc.wrapper;


import lombok.Getter;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 允许重复读取请求体的 Request Wrapper
 */
public class RepeatableReadRequestWrapper extends HttpServletRequestWrapper {

    @Getter
    private final byte[] body; // 缓存解密后的请求体数据

    public RepeatableReadRequestWrapper(HttpServletRequest request, byte[] decryptedBody) {
        super(request);
        this.body = decryptedBody;
    }

    /**
     * 重写 getInputStream() 方法，返回包含解密后数据的输入流
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(body);

        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return bais.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // 不支持异步读取
            }

            @Override
            public int read() throws IOException {
                return bais.read();
            }
        };
    }

    /**
     * 重写 getReader() 方法，返回包含解密后数据的读取器
     */
    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.getInputStream(), StandardCharsets.UTF_8));
    }
}