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
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import pl.derwinski.arkham.Util;
import static pl.derwinski.arkham.Util.log;
import pl.derwinski.arkham.json.Card;
import pl.derwinski.arkham.json.metadata.Metadata;

/**
 *
 * @author morvael
 */
public final class Configuration {

    private static final HashSet<String> unhandled = new HashSet<>();

    public static Configuration loadConfiguration() throws Exception {
        return loadConfiguration("run/configuration.json");
    }

    public static Configuration loadConfiguration(String path) throws Exception {
        var file = new File(path);
        var c = new JsonMapper().readTree(file);
        if (c != null) {
            return loadConfiguration(c);
        } else {
            log("Error reading Configuration file");
            return null;
        }
    }

    public static Configuration loadConfiguration(JsonNode c) throws Exception {
        if (c.isObject()) {
            var o = new Configuration();
            var it = c.fieldNames();
            while (it.hasNext()) {
                var fieldName = it.next();
                switch (fieldName) {
                    case "parallel":
                        var map = Util.readStringStringMap(c, fieldName);
                        for (var e : new LinkedHashMap<>(map).entrySet()) {
                            map.put(e.getValue(), e.getKey());
                        }
                        o.parallel = Collections.unmodifiableMap(map);
                        break;
                    case "parallelMini":
                        o.parallelMini = Collections.unmodifiableMap(Util.readStringStringMap(c, fieldName));
                        break;
                    case "ignored":
                        o.ignored = Collections.unmodifiableSet(Util.readStringSet(c, fieldName));
                        break;
                    case "skipBonded":
                        o.skipBonded = Collections.unmodifiableSet(Util.readStringSet(c, fieldName));
                        break;
                    case "flipped":
                        o.flipped = Collections.unmodifiableSet(Util.readStringSet(c, fieldName));
                        break;
                    case "overrides":
                        o.overrides = Collections.unmodifiableMap(Util.readStringJsonNodeMap(c, fieldName));
                        break;
                    case "extras":
                        o.extras = Collections.unmodifiableList(Util.readJsonNodeList(c, fieldName));
                        break;
                    case "cardBacks":
                        o.cardBacks = Collections.unmodifiableMap(Util.readStringStringMap(c, fieldName));
                        break;
                    case "packFilter":
                        o.packFilter = Collections.unmodifiableSet(Util.readStringSet(c, fieldName));
                        break;
                    case "imageMapping":
                        o.imageMapping = Collections.unmodifiableMap(Util.readStringStringMap(c, fieldName));
                        break;
                    case "ignoredPaths":
                        o.ignoredPaths = Collections.unmodifiableSet(Util.readStringSet(c, fieldName));
                        break;
                    default:
                        if (unhandled.add(fieldName)) {
                            log("Unhandled field name in Configuration: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType());
                        }
                        break;
                }
            }
            return o;
        } else {
            if (c.isNull() == false) {
                log("Error reading Configuration object: %s", c.asText());
            }
            return null;
        }
    }

    private Map<String, String> parallel;
    private Map<String, String> parallelMini;
    private Set<String> ignored;
    private Set<String> skipBonded;
    private Set<String> flipped;
    private Map<String, JsonNode> overrides;
    private List<JsonNode> extras;
    private Map<String, String> cardBacks;
    private Set<String> packFilter;
    private Map<String, String> imageMapping;
    private Set<String> ignoredPaths;

    private final HashMap<String, ArrayList<Card>> bondedCards = new HashMap<>();
    private final HashMap<String, ArrayList<Card>> parallelCards = new HashMap<>();

    private Configuration() {

    }

    public Map<String, String> getParallelMini() {
        return parallelMini;
    }

    public boolean isIgnored(Card c) {
        return (c.getOfficial() != null && c.getOfficial() == false)
                || (c.getPreview() != null && c.getPreview())
                || (c.getTabooSetId() != null && c.getTabooSetId() > 0)
                || (ignored != null && ignored.contains(c.getId()));
    }

    public boolean isSkipBonded(Card c) {
        return skipBonded != null && skipBonded.contains(c.getId());
    }

    public boolean isFlipped(Card c) {
        return flipped != null && flipped.contains(c.getId());
    }

    public void override(Metadata metadata, Card c) throws Exception {
        c.override(this, metadata, overrides.get(c.getId()));
    }

    public String getCardBack(Card c) {
        return cardBacks != null ? cardBacks.get(c.getId()) : null;
    }

    public boolean filter(Card c) {
        if (c.getHidden() != null && c.getHidden()) {
            return false;
        }
        if (c.getPackCode() == null) {
            return false;
        }
        if (packFilter == null || packFilter.contains(c.getPackCode()) == false) {
            return false;
        }
        if ("rcore".equals(c.getPackCode()) && c.getPosition() > 103 && c.getPosition() < 183) {
            return false;
        }
        return true;
    }

    public String getImageMapping(String databaseId) {
        return imageMapping.getOrDefault(databaseId, databaseId);
    }

    public boolean isIgnoredPath(String relativePath) {
        for (var ignoredPath : ignoredPaths) {
            if (relativePath.startsWith(ignoredPath)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasBonded(Card c) {
        return bondedCards.containsKey(c.getName()) && isSkipBonded(c) == false;
    }

    public List<Card> getBonded(String name) {
        return bondedCards.get(name);
    }

    public void register(Metadata metadata, Card c) throws Exception {
        var bondedTo = c.getBondedTo();
        if (bondedTo != null && bondedTo.length() > 0 && c.getQuantity() != null && c.getQuantity() > 0 && (c.getHidden() == null || c.getHidden() == false)) {
            var list = bondedCards.get(bondedTo);
            if (list == null) {
                list = new ArrayList<>();
                bondedCards.put(bondedTo, list);
            }
            list.add(c);
        }
        if (parallel.containsKey(c.getCode())) { //enabled for all variants
            var list = parallelCards.get(c.getCode());
            if (list == null) {
                list = new ArrayList<>();
                parallelCards.put(c.getCode(), list);
            }
            list.add(c);
        }
    }

    public void process(Metadata metadata, ArrayList<Card> cards) throws Exception {
        for (var c : extras) {
            var o = Card.readCard(this, metadata, c);
            override(metadata, o);
            register(metadata, o);
            cards.add(o);
        }
        for (var e : parallel.entrySet()) {
            var cl1 = parallelCards.get(e.getKey());
            var cl2 = parallelCards.get(e.getValue());
            if (cl1 != null && cl2 != null) {
                for (var c1 : cl1) {
                    for (var c2 : cl2) {
                        cards.add(c1.parallelClone(c2));
                    }
                }
            } else {
                log("Missing parallel cards data for %s and/or %s", e.getKey(), e.getValue());
            }
        }
        cards.sort(null);
        for (var list : bondedCards.values()) {
            list.sort(null);
        }
        var tabooCards = new HashMap<String, Card>();
        for (var c : cards) {
            if (c.getId().contains("-") && tabooCards.containsKey(c.getCode()) == false) {
                tabooCards.put(c.getCode(), null);
            }
        }
        var it = cards.iterator();
        while (it.hasNext()) {
            var c = it.next();
            if (c.getId().contains("-")) {
                var pc = tabooCards.get(c.getCode());
                if (c.tabooEquals(pc) == false) {
                    tabooCards.put(c.getCode(), c);
                } else {
                    it.remove();
                }
            }
        }
    }

}
