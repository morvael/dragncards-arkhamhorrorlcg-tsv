/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org/>
 */
package pl.derwinski.arkham;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author morvael
 */
public final class Util {

    private Util() {
        throw new UnsupportedOperationException();
    }

    public static String readStringRaw(JsonNode c) {
        if (c == null || c.isNull()) {
            return null;
        } else {
            return c.asText();
        }
    }

    public static String readString(JsonNode c) {
        if (c == null || c.isNull()) {
            return null;
        } else {
            String s = c.asText();
            if (s.contains("  ")) {
                s = s.replace("  ", " ");
            }
            if (s.indexOf('\n') != -1) {
                s = s.replace("\n", "  ");
            }
            return s;
        }
    }

    public static Integer readInteger(JsonNode c) {
        if (c == null || c.isNull()) {
            return null;
        } else {
            try {
                return Integer.valueOf(c.asText());
            } catch (NumberFormatException ex) {
                log("Error reading Integer field: %s", c.asText());
                return null;
            }
        }
    }

    public static Boolean readBoolean(JsonNode c) {
        if (c == null || c.isNull()) {
            return null;
        } else {
            switch (c.asText()) {
                case "true":
                    return Boolean.TRUE;
                case "false":
                    return Boolean.FALSE;
                default:
                    log("Error reading Boolean field: %s", c.asText());
                    return null;
            }
        }
    }

    public static ArrayList<String> readStringList(JsonNode c) throws Exception {
        if (c.isArray()) {
            ArrayList<String> list = new ArrayList<>();
            for (int i = 0; i < c.size(); i++) {
                list.add(readString(c.get(i)));
            }
            return list;
        } else {
            if (c.isNull() == false) {
                log("Error reading StringList field: %s", c.asText());
            }
            return null;
        }
    }

    public static LinkedHashMap<String, String> readStringStringMap(JsonNode c) throws Exception {
        if (c.isObject()) {
            LinkedHashMap<String, String> map = new LinkedHashMap<>();
            Iterator<String> it = c.fieldNames();
            while (it.hasNext()) {
                String fieldName = it.next();
                map.put(fieldName, readString(c.get(fieldName)));
            }
            return map;
        } else {
            if (c.isNull() == false) {
                log("Error reading StringStringMap field: %s", c.asText());
            }
            return null;
        }
    }

    public static HashSet<String> readConfigSet(String path) throws Exception {
        HashSet<String> result = new HashSet<>();
        File f = new File(path);
        if (f.exists()) {
            List<String> lines = FileUtils.readLines(f, StandardCharsets.UTF_8);
            if (lines != null && lines.isEmpty() == false) {
                for (String s : lines) {
                    if (s.trim().length() > 0) {
                        result.add(s.trim());
                    }
                }
            }
        }
        return result;
    }

    private static final String PIPE = Pattern.quote("|");

    public static HashMap<String, String> readConfigMap(String path) throws Exception {
        HashMap<String, String> result = new HashMap<>();
        File f = new File(path);
        if (f.exists()) {
            List<String> lines = FileUtils.readLines(f, StandardCharsets.UTF_8);
            if (lines != null && lines.isEmpty() == false) {
                for (String s : lines) {
                    if (s.trim().length() > 0) {
                        String[] data = s.split(PIPE);
                        if (data.length >= 2) {
                            result.put(data[0].trim(), data[1].trim());
                        }
                    }
                }
            }
        }
        return result;
    }

    public static void download(String url, File target) throws Exception {
        try {
            if (target.exists() == false && url != null && url.length() > 0) {
                log("Downloading %s", url);
                HttpClient client = HttpClient.newBuilder()
                        .followRedirects(Redirect.NORMAL)
                        .connectTimeout(Duration.ofSeconds(20))
                        .build();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(url))
                        .header("user-agent", "curl/7.81.0")
                        .header("accept", "*/*")
                        .GET()
                        .timeout(Duration.ofSeconds(10))
                        .build();
                client.send(request, BodyHandlers.ofFile(target.toPath()));
            }
        } catch (Exception ex) {
            log(ex);
        }
    }
    private static final long DAYS_7 = TimeUnit.DAYS.toMillis(7);

    public static void downloadIfOld(String url, File target) throws Exception {
        if (target.exists() && target.lastModified() + DAYS_7 < System.currentTimeMillis()) {
            target.delete();
        }
        download(url, target);
    }

    public static void downloadIfOld(String url, String target) throws Exception {
        downloadIfOld(url, new File(target));
    }

    public static boolean checkExistence(String path, String format) throws Exception {
        File f = new File(path);
        if (f.exists()) {
            return true;
        } else {
            log(format, f.getAbsoluteFile().getCanonicalPath());
            return false;
        }
    }

    public static void log(String message) {
        System.out.println(message);
    }

    public static void log(String format, Object... params) {
        System.out.println(String.format(format, params));
    }

    public static void log(Throwable t) {
        System.err.println(t.getMessage());
        t.printStackTrace(System.err);
    }

}
