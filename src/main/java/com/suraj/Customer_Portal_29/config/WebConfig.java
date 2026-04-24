package com.suraj.Customer_Portal_29.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String basePath = System.getProperty("user.dir") + "/uploads/";

        // Serve aadharImages sub-folder
        registry.addResourceHandler("/uploads/aadharImages/**")
                .addResourceLocations("file:" + basePath + "aadharImages/");

        // Serve sitePhotos sub-folder
        registry.addResourceHandler("/uploads/sitePhotos/**")
                .addResourceLocations("file:" + basePath + "sitePhotos/");
    }
}