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

import cn.rylan.rest.http.HTTP;
import cn.rylan.rest.http.HttpRequest;
import cn.rylan.model.MethodTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;


public class RequestHandler {

    private final MethodTemplate methodTemplate;


    public RequestHandler(MethodTemplate methodTemplate) {
        this.methodTemplate = methodTemplate;

    }


    public Object http(String url, HTTP methodType) throws JsonProcessingException {
        Class<?> returnType = methodTemplate.getReturnType();
        HttpRequest httpRequest = new HttpRequest();
        Object result = null;
        switch (methodType) {
            case GET:
                result = httpRequest.get(url, returnType);
                break;
            case POST:
                result = httpRequest.post(url, methodTemplate.getBody(), returnType);
                break;
        }
        return result;
    }


}
