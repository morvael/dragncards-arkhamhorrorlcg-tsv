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
package pl.derwinski.arkham.json.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import pl.derwinski.arkham.Util;
import static pl.derwinski.arkham.Util.log;
import static pl.derwinski.arkham.Util.nvl;

/**
 *
 * @author morvael
 */
public final class Parallel {

    private static final HashSet<String> unhandled = new HashSet<>();

    public static ArrayList<Parallel> readParallels(JsonNode c) throws Exception {
        if (c.isArray()) {
            var result = new ArrayList<Parallel>();
            for (var i = 0; i < c.size(); i++) {
                var p = readParallel(c.get(i));
                result.add(p);
            }
            return result;
        } else {
            if (c.isNull() == false) {
                log("Error reading Parallel array: %s", c.asText());
            }
            return null;
        }
    }

    public static Parallel readParallel(JsonNode c) throws Exception {
        if (c.isObject()) {
            var o = new Parallel();
            var it = c.fieldNames();
            while (it.hasNext()) {
                var fieldName = it.next();
                switch (fieldName) {
                    case "regular":
                        o.regular = Collections.unmodifiableSet(Util.readStringSet(c, fieldName));
                        break;
                    case "parallel":
                        o.parallel = Collections.unmodifiableSet(Util.readStringSet(c, fieldName));
                        break;
                    case "sameArt":
                        o.sameArt = nvl(Util.readBoolean(c, fieldName), true);
                        break;
                    default:
                        if (unhandled.add(fieldName)) {
                            log("Unhandled field name in Parallel: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType());
                        }
                        break;
                }
            }
            return o;
        } else {
            if (c.isNull() == false) {
                log("Error reading Parallel object: %s", c.asText());
            }
            return null;
        }
    }

    private Set<String> regular;
    private Set<String> parallel;
    private boolean sameArt = true;

    public Parallel() {

    }

    public Set<String> getRegular() {
        return regular;
    }

    public Set<String> getParallel() {
        return parallel;
    }

    public boolean isSameArt() {
        return sameArt;
    }

}
