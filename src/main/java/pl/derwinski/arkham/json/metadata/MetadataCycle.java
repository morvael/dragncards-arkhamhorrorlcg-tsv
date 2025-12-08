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
package pl.derwinski.arkham.json.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashSet;
import java.util.LinkedHashMap;
import static pl.derwinski.arkham.Util.log;
import static pl.derwinski.arkham.Util.nvl;
import static pl.derwinski.arkham.Util.readInteger;
import static pl.derwinski.arkham.Util.readString;

/**
 *
 * @author morvael
 */
public final class MetadataCycle {

    private static final HashSet<String> unhandled = new HashSet<>();

    public static LinkedHashMap<String, MetadataCycle> readMetadataCycles(JsonNode c) throws Exception {
        if (c.isArray()) {
            var result = new LinkedHashMap<String, MetadataCycle>();
            for (var i = 0; i < c.size(); i++) {
                var mc = readMetadataCycle(c.get(i));
                result.put(mc.getCode(), mc);
            }
            return result;
        } else {
            if (c.isNull() == false) {
                log("Error reading MetadataCycle array: %s", c.asText());
            }
            return null;
        }
    }

    public static MetadataCycle readMetadataCycle(JsonNode c) throws Exception {
        if (c.isObject()) {
            var o = new MetadataCycle();
            var it = c.fieldNames();
            while (it.hasNext()) {
                var fieldName = it.next();
                switch (fieldName) {
                    case "code":
                        o.code = readString(c, fieldName);
                        break;
                    case "position":
                        o.position = readInteger(c, fieldName);
                        break;
                    case "name":
                        o.name = readString(c, fieldName);
                        break;
                    case "real_name":
                        o.realName = readString(c, fieldName);
                        o.name = nvl(o.name, o.realName);
                        break;
                    default:
                        if (unhandled.add(fieldName)) {
                            log("Unhandled field name in MetadataCycle: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType());
                        }
                        break;
                }
            }
            return o;
        } else {
            if (c.isNull() == false) {
                log("Error reading MetadataCycle object: %s", c.asText());
            }
            return null;
        }
    }

    private String code;
    private Integer position;
    private String name;
    private String realName;

    private MetadataCycle() {

    }

    public String getCode() {
        return code;
    }

    public Integer getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    public String getRealName() {
        return realName;
    }

}
