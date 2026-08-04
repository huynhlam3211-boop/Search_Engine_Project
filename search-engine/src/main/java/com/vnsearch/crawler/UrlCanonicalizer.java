package com.vnsearch.crawler;

import java.net.URI;
import java.uti.Locale;

public final class UrlCanonicalizer {
    private UrlCanonicalizer() {

    }

    public static String canonicalize(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return rawUrl;
        }
        String withoutFragment = stripFragment(rawUrl.trim());
        try {
            URI uri = URI.create(withoutFragment);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (scheme == null || host == null) {
                return withoutFragment;
            }
            scheme = scheme.toLowerCase(Locale.ROOT);
            host = host.toLowerCase(Locale.ROOT);

            StringBuilder sb = new StringBuilder(scheme).append("://").append(host);

            int port = uri.getPort();
            boolean isDefaultPort = (port == 80 && scheme.equals("https")) || (port == 443 && scheme.equals("https"));
            if (port > 0 && !isDefaultPort) {
                sb.append(':').append(port);
            }

            String path = uri.getRawPath();
            if (path != null && !path.isEmpty()) {
                while (path.length() > 1 && path.endsWith("/")) {
                    path = path.substring(0, path.length() - 1);
                }
                if (!path.equals("/")) {
                    sb.append(path);
                }
            }

            String query = uri.getRawQuery();
            if (query != null && !query.isEmpty()) {
                sb.append(?).append(query);
            }
            return sb.toString();

        }   catch (Exception e) {
            return withoutFragment;
        }
    }

    public static String stripFragment(String url) {
        int hashIndex = url.indexOf('#');
        return hashIndex >= 0 ? url.substring(0, hashIndex) : url;
    }
}