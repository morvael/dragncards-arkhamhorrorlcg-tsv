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
package pl.derwinski.arkham.json.fan;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashSet;
import static pl.derwinski.arkham.Util.log;
import static pl.derwinski.arkham.Util.readString;
import pl.derwinski.arkham.json.configuration.Configuration;
import pl.derwinski.arkham.json.metadata.Metadata;

/**
 *
 * @author morvael
 */
public final class FanMadeProjectMeta {

    private static final HashSet<String> unhandled = new HashSet<>();

    public static FanMadeProjectMeta readFanMadeProjectMeta(Configuration configuration, Metadata metadata, JsonNode c) throws Exception {
        if (c.isObject()) {
            var o = new FanMadeProjectMeta();
            var it = c.fieldNames();
            while (it.hasNext()) {
                var fieldName = it.next();
                switch (fieldName) {
                    case "code":
                        o.code = readString(c, fieldName);
                        break;
                    case "name":
                        o.name = readString(c, fieldName);
                        break;
                    case "url":
                        o.url = readString(c, fieldName);
                        break;
                    // ignored fields
                    case "author":
                    case "banner_url":
                    case "banner_credit":
                    case "date_updated":
                    case "description":
                    case "external_link":
                    case "generator":
                    case "language":
                    case "status":
                    case "types":
                        break;
                    default:
                        if (unhandled.add(fieldName)) {
                            log("Unhandled field name in FanMadeProjectMeta: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType());
                        }
                        break;

                }
            }
            return o;
        } else {
            if (c.isNull() == false) {
                log("Error reading FanMadeProjectMeta object: %s", c.asText());
            }
            return null;
        }
    }

    private String code;
    private String name;
    private String url;

    public FanMadeProjectMeta() {

    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getURL() {
        return url;
    }

    @Override
    public String toString() {
        return String.format("%s %s", code, name);
    }

}
