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
