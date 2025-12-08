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
import static pl.derwinski.arkham.Util.readInteger;
import static pl.derwinski.arkham.Util.readString;

/**
 *
 * @author morvael
 */
public final class MetadataTabooSet {

    private static final HashSet<String> unhandled = new HashSet<>();

    public static LinkedHashMap<Integer, MetadataTabooSet> readMetadataTabooSets(JsonNode c) throws Exception {
        if (c.isArray()) {
            var result = new LinkedHashMap<Integer, MetadataTabooSet>();
            for (var i = 0; i < c.size(); i++) {
                var mt = readMetadataTabooSet(c.get(i));
                result.put(mt.getId(), mt);
            }
            return result;
        } else {
            if (c.isNull() == false) {
                log("Error reading MetadataTabooSet array: %s", c.asText());
            }
            return null;
        }
    }

    public static MetadataTabooSet readMetadataTabooSet(JsonNode c) throws Exception {
        if (c.isObject()) {
            var o = new MetadataTabooSet();
            var it = c.fieldNames();
            while (it.hasNext()) {
                var fieldName = it.next();
                switch (fieldName) {
                    case "name":
                        o.name = readString(c, fieldName);
                        break;
                    case "card_count":
                        o.cardCount = readInteger(c, fieldName);
                        break;
                    case "id":
                        o.id = readInteger(c, fieldName);
                        break;
                    case "date":
                        o.date = readString(c, fieldName);
                        break;
                    default:
                        if (unhandled.add(fieldName)) {
                            log("Unhandled field name in MetadataTabooSet: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType());
                        }
                        break;
                }
            }
            return o;
        } else {
            if (c.isNull() == false) {
                log("Error reading MetadataTabooSet object: %s", c.asText());
            }
            return null;
        }
    }

    private String name;
    private Integer cardCount;
    private Integer id;
    private String date;

    private MetadataTabooSet() {

    }

    public String getName() {
        return name;
    }

    public Integer getCardCount() {
        return cardCount;
    }

    public Integer getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

}
