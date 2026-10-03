package com.carrental.api;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal hand-rolled JSON writer/reader — enough for this prototype's
 * simple flat request/response bodies, with zero extra dependencies.
 * Swap in Jackson/Gson if the API grows past basic objects.
 */
public final class JsonUtil {

    private JsonUtil() { }

    public static String toJson(Object obj) {
        StringBuilder sb = new StringBuilder();
        write(obj, sb);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void write(Object obj, StringBuilder sb) {
        if (obj == null) {
            sb.append("null");
        } else if (obj instanceof Map) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<String, Object> e : ((Map<String, Object>) obj).entrySet()) {
                if (!first) sb.append(',');
                first = false;
                sb.append('"').append(escape(e.getKey())).append("\":");
                write(e.getValue(), sb);
            }
            sb.append('}');
        } else if (obj instanceof List) {
            sb.append('[');
            boolean first = true;
            for (Object item : (List<Object>) obj) {
                if (!first) sb.append(',');
                first = false;
                write(item, sb);
            }
            sb.append(']');
        } else if (obj instanceof Number || obj instanceof Boolean) {
            sb.append(obj);
        } else {
            sb.append('"').append(escape(obj.toString())).append('"');
        }
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    /** Parses a flat JSON object of string/number/boolean values — matches what this API accepts as input. */
    public static Map<String, String> parseFlatObject(String json) {
        Map<String, String> result = new LinkedHashMap<>();
        if (json == null) return result;
        String body = json.trim();
        if (body.startsWith("{")) body = body.substring(1);
        if (body.endsWith("}")) body = body.substring(0, body.length() - 1);

        int i = 0;
        int len = body.length();
        while (i < len) {
            while (i < len && (body.charAt(i) == ',' || Character.isWhitespace(body.charAt(i)))) i++;
            if (i >= len) break;
            if (body.charAt(i) != '"') break;
            int keyStart = ++i;
            while (i < len && body.charAt(i) != '"') i++;
            String key = body.substring(keyStart, i);
            i++; // closing quote
            while (i < len && (body.charAt(i) == ':' || Character.isWhitespace(body.charAt(i)))) i++;

            String value;
            if (i < len && body.charAt(i) == '"') {
                int valStart = ++i;
                StringBuilder val = new StringBuilder();
                while (i < len && body.charAt(i) != '"') {
                    if (body.charAt(i) == '\\' && i + 1 < len) {
                        i++;
                    }
                    val.append(body.charAt(i));
                    i++;
                }
                value = val.toString();
                i++; // closing quote
            } else {
                int valStart = i;
                while (i < len && body.charAt(i) != ',' && body.charAt(i) != '}') i++;
                value = body.substring(valStart, i).trim();
            }
            result.put(key, value);
        }
        return result;
    }

    public static Map<String, Object> map(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put((String) kv[i], kv[i + 1]);
        }
        return m;
    }
}
