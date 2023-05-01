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

import cn.rylan.rest.model.MethodTemplate;
import lombok.Data;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.websocket.server.PathParam;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Data
public class MethodHandler {

    private Map<Parameter, Object> paramMap = new ConcurrentHashMap<>();

    private Method method;

    private Object[] values;


    public MethodHandler(Method method, Object[] values) {
        this.method = method;
        this.values = values;

        //init
        Parameter[] parameters = method.getParameters();
        int index = 0;
        for (Parameter param : parameters) {
            paramMap.put(param, values[index++]);
        }
    }


    public MethodTemplate handler() {
        Class<?> returnType = this.method.getReturnType();
        Map<String, Object> pathValues = new HashMap<>();
        Map<String, Object> paramValues = new HashMap<>();
        AtomicReference<Object> body = new AtomicReference<>();

        paramMap.forEach((key, value) -> {
            if (key.isAnnotationPresent(PathVariable.class)) {
                PathVariable pathVariable = key.getAnnotation(PathVariable.class);
                pathValues.put(pathVariable.value(), value);
            } else if (key.isAnnotationPresent(RequestBody.class)) {
                body.set(value);
            } else {
                RequestParam pathParam = key.getAnnotation(RequestParam.class);
                paramValues.put(pathParam.value(), value);
            }
        });

        MethodTemplate methodTemplate = new MethodTemplate();
        methodTemplate.setMethod(method);
        methodTemplate.setPathValues(pathValues);
        methodTemplate.setParamValues(paramValues);
        methodTemplate.setBody(body);
        methodTemplate.setReturnType(returnType);
        return methodTemplate;
    }
}
