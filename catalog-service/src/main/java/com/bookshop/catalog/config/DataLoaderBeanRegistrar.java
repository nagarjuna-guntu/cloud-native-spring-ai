package com.bookshop.catalog.config;


import com.bookshop.catalog.demo.BookDataLoader;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.BeanRegistrar;
import org.springframework.beans.factory.BeanRegistry;
import org.springframework.core.env.Environment;

public class DataLoaderBeanRegistrar implements BeanRegistrar {
    @Override
    public void register(@NonNull BeanRegistry registry, Environment env) {
        var profiles = env.getActiveProfiles();
        for(String profile : profiles) {
            if ("testdata".equals(profile)) {
                registry.registerBean("bookDataLoader", BookDataLoader.class,
                        spec -> spec.description("BookDataLoader"));
            }
        }

    }
}
