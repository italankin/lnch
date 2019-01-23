package com.italankin.lnch.model.repository.search.web;

import java.util.HashMap;
import java.util.Map;

public class WebSearchProviderFactory {

    private static final WebSearchProvider DEFAULT = new StaticProvider("https://www.google.com/search?q=");
    private static final Map<String, WebSearchProvider> PROVIDERS = new HashMap<>(8);

    static {
        PROVIDERS.put("google", DEFAULT);
        PROVIDERS.put("bing", new StaticProvider("https://www.bing.com/search?q="));
        PROVIDERS.put("yandex", new StaticProvider("https://yandex.ru/search/?text="));
        PROVIDERS.put("ddg", new StaticProvider("https://duckduckgo.com/?q="));
        PROVIDERS.put("baidu", new StaticProvider("https://www.baidu.com/s?wd="));
    }

    public static WebSearchProvider get(String name) {
        WebSearchProvider provider = PROVIDERS.get(name);
        return provider != null ? provider : DEFAULT;
    }
}
