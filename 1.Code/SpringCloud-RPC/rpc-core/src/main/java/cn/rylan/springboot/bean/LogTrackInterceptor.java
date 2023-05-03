package cn.rylan.springboot.bean;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;


public class LogTrackInterceptor implements HandlerInterceptor {
    /**
     * 存储 traceId
     */
    private static final ThreadLocal<String> TRACE_ID_THREAD_LOCAL = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /**
         * 获取请求头header中传递的trace-id，若没有，则UUID代替
         */
        String traceId = Optional.ofNullable(request.getHeader("trace-id")).orElse(UUID.randomUUID().toString().replaceAll("-",""));
        // 请求前设置
        TRACE_ID_THREAD_LOCAL.set(traceId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 移除，防止内存泄漏
        TRACE_ID_THREAD_LOCAL.remove();
    }

    public static String getTraceId() {
        return TRACE_ID_THREAD_LOCAL.get();
    }

    public static void setTraceId(String traceId){
        TRACE_ID_THREAD_LOCAL.set(traceId);
    }

}

