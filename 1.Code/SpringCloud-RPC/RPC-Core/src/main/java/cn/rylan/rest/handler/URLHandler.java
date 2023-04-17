/*
 * Copyright (c) 2023 Rylan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 */
package cn.rylan.rest.handler;

import cn.rylan.model.MethodTemplate;
import cn.rylan.model.ServiceInstance;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;
import java.util.Map;


public class URLHandler {

    private final ServiceInstance instance;

    private String uri;

    private final MethodTemplate methodTemplate;

    public URLHandler(ServiceInstance instance, Method method, Object[] objects, String uri) {
        this.uri = uri;
        this.instance = instance;
        MethodHandler methodHandler = new MethodHandler(method, objects);
        methodTemplate = methodHandler.handler();
    }

    public String getURL() {
        String uri = handler();
        return "http://" + instance.getIp() + ":" + instance.getPort() + uri;
    }

    private String handler() {

        Map<String, Object> paramValues = methodTemplate.getParamValues();

        Map<String, Object> pathValues = methodTemplate.getPathValues();

        if (pathValues.size() == 0 && paramValues.size() == 0) {
            return uri;
        }
        if (pathValues.size() > 0) {
            pathValues.forEach((name, value) -> {
                uri = replace(this.uri, name, value.toString());
            });
        }
        if (paramValues.size() > 0) {
            uri = this.uri + "?";
            paramValues.forEach((name, value) -> {
                uri = addParam(uri, name, value.toString());
            });
            uri = StringUtils.removeEnd(uri, "&");
        }
        return uri;
    }

    private String replace(String uri, String target, String param) {
        String s = "{" + target + "}";
        return uri.replace(s, param);
    }

    private String addParam(String uri, String param, String value) {
        uri = uri + param + "=" + value + "&";
        return uri;
    }

    public MethodTemplate getMethodTemplate() {
        return methodTemplate;
    }

}
