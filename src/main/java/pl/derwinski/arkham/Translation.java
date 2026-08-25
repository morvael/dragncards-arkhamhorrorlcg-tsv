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

import java.util.LinkedHashMap;

/**
 *
 * @author morvael
 */
public class Translation {

    private final LinkedHashMap<String, Integer> translations = new LinkedHashMap<>();

    public Translation() {

    }

    public Translation register(String realTrait) {
        var n = translations.getOrDefault(realTrait, 0) + 1;
        translations.put(realTrait, n);
        return this;
    }

    public LinkedHashMap<String, Integer> getTranslations() {
        return translations;
    }

    public String getMostPopular() {
        String trait = null;
        int max = 0;
        for (var e : translations.entrySet()) {
            if (e.getValue() > max) {
                trait = e.getKey();
                max = e.getValue();
            }
        }
        return trait;
    }

    public String format() {
        var sb = new StringBuilder();
        if (translations.isEmpty() == false) {
            for (var e : translations.entrySet()) {
                sb.append("\"");
                sb.append(e.getKey());
                sb.append("\" x");
                sb.append(e.getValue());
                sb.append(", ");
            }
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }

}
