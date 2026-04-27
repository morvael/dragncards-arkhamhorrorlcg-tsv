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
import java.util.List;
import static pl.derwinski.arkham.Util.log;
import static pl.derwinski.arkham.Util.nvl;
import static pl.derwinski.arkham.Util.readInteger;
import static pl.derwinski.arkham.Util.readString;
import static pl.derwinski.arkham.Util.readStringList;
import static pl.derwinski.arkham.Util.unmodifiable;

/**
 *
 * @author morvael
 */
public final class MetadataPack {

    private static final HashSet<String> unhandled = new HashSet<>();

    public static LinkedHashMap<String, MetadataPack> readMetadataPacks(JsonNode c) throws Exception {
        if (c.isArray()) {
            var result = new LinkedHashMap<String, MetadataPack>();
            for (var i = 0; i < c.size(); i++) {
                var mp = readMetadataPack(c.get(i));
                result.put(mp.getCode(), mp);
            }
            return result;
        } else {
            if (c.isNull() == false) {
                log("Error reading MetadataPack array: %s", c.asText());
            }
            return null;
        }
    }

    public static MetadataPack readMetadataPack(JsonNode c) throws Exception {
        if (c.isObject()) {
            var o = new MetadataPack();
            var it = c.fieldNames();
            while (it.hasNext()) {
                var fieldName = it.next();
                switch (fieldName) {
                    case "code":
                        o.code = readString(c, fieldName);
                        break;
                    case "cycle_code":
                        o.cycleCode = readString(c, fieldName);
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
                    case "type":
                        o.type = readString(c, fieldName);
                        break;
                    case "chapter":
                        o.chapter = readInteger(c, fieldName);
                        break;
                    case "date_release":
                        o.dateRelease = readString(c, fieldName);
                        break;
                    case "size":
                        o.size = readInteger(c, fieldName);
                        break;
                    case "reprint_type":
                        o.reprintType = readString(c, fieldName);
                        break;
                    case "reprint_packs":
                        o.reprintPacks = unmodifiable(readStringList(c, fieldName));
                        break;
                    default:
                        if (unhandled.add(fieldName)) {
                            log("Unhandled field name in MetadataPack: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType());
                        }
                        break;
                }
            }
            return o;
        } else {
            if (c.isNull() == false) {
                log("Error reading MetadataPack object: %s", c.asText());
            }
            return null;
        }
    }

    private String code;
    private String cycleCode;
    private Integer position;
    private String name;
    private String realName;
    private String type;
    private Integer chapter;
    private String dateRelease;
    private Integer size;
    private String reprintType;
    private List<String> reprintPacks;

    private MetadataPack() {

    }

    public String getCode() {
        return code;
    }

    public String getCycleCode() {
        return cycleCode;
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

    public String getType() {
        return type;
    }

    public Integer getChapter() {
        return chapter;
    }

    public String getDateRelease() {
        return dateRelease;
    }

    public Integer getSize() {
        return size;
    }

    public String getReprintType() {
        return reprintType;
    }

    public List<String> getReprintPacks() {
        return reprintPacks;
    }

}
