package com.italankin.lnch.model.repository.search.web;

import com.italankin.lnch.model.repository.prefs.Preferences;

import java.util.HashMap;
import java.util.Map;

public class WebSearchProviderFactory {

    private static final Map<Preferences.SearchEngine, WebSearchProvider> PROVIDERS = new HashMap<>(8);
    private static final WebSearchProvider EMPTY = (label, query) -> null;

    static {
        PROVIDERS.put(Preferences.SearchEngine.GOOGLE, new FormattedProvider("https://www.google.com/search?q=%s"));
        PROVIDERS.put(Preferences.SearchEngine.BING, new FormattedProvider("https://www.bing.com/search?q=%s"));
        PROVIDERS.put(Preferences.SearchEngine.YANDEX, new FormattedProvider("https://yandex.ru/search/?text=%s"));
        PROVIDERS.put(Preferences.SearchEngine.DDG, new FormattedProvider("https://duckduckgo.com/?q=%s"));
        PROVIDERS.put(Preferences.SearchEngine.BAIDU, new FormattedProvider("https://www.baidu.com/s?wd=%s"));
    }

    public static WebSearchProvider get(Preferences preferences) {
        Preferences.SearchEngine searchEngine = preferences.get(Preferences.SEARCH_ENGINE);
        if (searchEngine == Preferences.SearchEngine.CUSTOM) {
            String format = preferences.get(Preferences.CUSTOM_SEARCH_ENGINE_FORMAT);
            // assume we have a valid format string
            return new FormattedProvider(format);
        } else {
            WebSearchProvider provider = PROVIDERS.get(searchEngine);
            return provider != null ? provider : EMPTY;
        }
    }
}
