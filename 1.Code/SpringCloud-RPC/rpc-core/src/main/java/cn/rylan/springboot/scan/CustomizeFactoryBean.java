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
package cn.rylan.springboot.scan;

import cn.rylan.rest.annotation.RestHttpClient;
import cn.rylan.rest.proxy.CglibProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;

@Slf4j
public class CustomizeFactoryBean<T> implements FactoryBean<T> {
    private Class<T> interfaceClass;

    public CustomizeFactoryBean() {
    }

    public CustomizeFactoryBean(Class<T> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }


    @Override
    public T getObject() {
        RestHttpClient restHttpClient = interfaceClass.getAnnotation(RestHttpClient.class);
        String serviceName = restHttpClient.serviceName();
        CglibProxy cglibProxy = new CglibProxy(serviceName);
        T proxy = cglibProxy.getProxy(interfaceClass);
        if (proxy == null) {
            log.error("Proxy Object Error => {}", interfaceClass.getName());
        }
        log.info("Proxy Object Success => {}", interfaceClass.getName());
        return proxy;
    }

    @Override
    public Class<T> getObjectType() {
        return interfaceClass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
