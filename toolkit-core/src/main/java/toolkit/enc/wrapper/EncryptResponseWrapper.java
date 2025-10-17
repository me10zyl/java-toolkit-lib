package toolkit.enc.wrapper;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.function.Function;

public class EncryptResponseWrapper extends HttpServletResponseWrapper {
    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private PrintWriter printWriter = new PrintWriter(byteArrayOutputStream);
    private Function<String, String> encFunc;

    public EncryptResponseWrapper(HttpServletResponse response, Function<String, String> encFunc) {
        super(response);
        this.encFunc = encFunc;
    }

    // 重写输出流，将响应内容写入缓存
    @Override
    public ServletOutputStream getOutputStream() {
        return new ServletOutputStream() {
            @Override
            public void write(int b) {
                byteArrayOutputStream.write(b);
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener listener) {}
        };
    }

    @Override
    public PrintWriter getWriter() {
        return printWriter;
    }

    // 加密缓存的响应内容并输出到原始响应流
    public void flush() throws Exception {
        // 加密响应体
        String responseBody = new String(byteArrayOutputStream.toByteArray(), "UTF-8");
        String encrypted = encFunc.apply(responseBody);

        // 写入原始响应流
        ServletResponse response = getResponse();
        response.setContentLength(encrypted.getBytes("UTF-8").length);
        response.getWriter().write(encrypted);
        response.getWriter().flush();
    }
}