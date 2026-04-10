package com.rydvrse.notification.application;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TemplateRenderService {

    public String render(String template, Map<String, Object> context) {
        String rendered = template == null ? "" : template;
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            rendered = rendered.replace("{{" + entry.getKey() + "}}", entry.getValue() == null ? "" : String.valueOf(entry.getValue()));
        }
        return rendered;
    }
}
