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
import static pl.derwinski.arkham.Util.nvl;
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
                        o.parallel = Collections.unmodifiableList(Parallel.readParallels(c.get(fieldName)));
                        var allParallel = new HashSet<String>();
                        for (var p : o.parallel) {
                            allParallel.addAll(p.getRegular());
                            allParallel.addAll(p.getParallel());
                        }
                        o.allParallel = Collections.unmodifiableSet(allParallel);
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
                        o.imageMapping = Util.readStringStringMap(c, fieldName);
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

    private List<Parallel> parallel;
    private Set<String> allParallel;
    private Set<String> ignored;
    private Set<String> skipBonded;
    private Set<String> flipped;
    private Map<String, JsonNode> overrides;
    private List<JsonNode> extras;
    private Map<String, String> cardBacks;
    private Set<String> packFilter;
    private LinkedHashMap<String, String> imageMapping;
    private Set<String> ignoredPaths;

    private final HashMap<String, ArrayList<Card>> bondedCards = new HashMap<>();
    private final HashMap<String, ArrayList<Card>> parallelCards = new HashMap<>();

    private Configuration() {

    }

    public boolean isIgnored(Card c) {
        return (c.getOfficial() != null && c.getOfficial() == false)
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

    public List<Card> getBonded(Card c) {
        var tb = nvl(c.getTabooSetId(), 0);
        var baseList = bondedCards.get(c.getName());
        var usedCodes = new HashSet<String>();
        var list = new ArrayList<Card>();
        for (var bc : baseList.reversed()) { //pick latest version that is <= this version
            var btb = nvl(bc.getTabooSetId(), 0);
            if (btb <= tb && usedCodes.add(bc.getCode())) {
                list.add(bc);
            }
        }
        if (list.size() > 1) {
            list.sort(null);
        }
        return list;
    }

    public void process(Metadata metadata, ArrayList<Card> cards) throws Exception {
        // read and add extras (full cards defined in configuration)
        for (var c : extras) {
            var o = Card.readCard(this, metadata, c);
            //override(metadata, o);
            cards.add(o);
        }
        // eliminate newer taboos that are duplicates (except id and tabooSetId), requires preliminary sort
        cards.sort(null);
        var tabooCards = new LinkedHashMap<String, Card>();
        var maxTabooSetId = new HashMap<String, Integer>();
        for (var c : cards) {
            if (c.getId().contains("-")) {
                if (tabooCards.containsKey(c.getCode()) == false) {
                    tabooCards.put(c.getCode(), null);
                }
                var mts = maxTabooSetId.getOrDefault(c.getCode(), 0);
                if (c.getTabooSetId() > mts) {
                    maxTabooSetId.put(c.getCode(), c.getTabooSetId());
                }
            }
        }
        var originalCards = new LinkedHashMap<String, Card>();
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
            } else if (tabooCards.containsKey(c.getCode())) {
                originalCards.put(c.getCode(), c);
            }
        }
        var latestTabooSetId = metadata.getLatestTabooSetId();
        for (var c : tabooCards.values()) {
            var mts = maxTabooSetId.get(c.getCode());
            if (mts < latestTabooSetId) {
                var originalCard = originalCards.get(c.getCode());
                var o = originalCard.tabooClone(mts + 1);
                imageMapping.put(o.getId(), originalCard.getId());
                cards.add(o);
            }
        }
        // extra processing once duplicates removed
        for (var c : cards) {
            // register bonded cards under the name of the card they are bonded to
            var bondedTo = c.getBondedTo();
            if (bondedTo != null && bondedTo.length() > 0 && c.getQuantity() != null && c.getQuantity() > 0 && (c.getHidden() == null || c.getHidden() == false)) {
                var list = bondedCards.get(bondedTo);
                if (list == null) {
                    list = new ArrayList<>();
                    bondedCards.put(bondedTo, list);
                }
                list.add(c);
            }
            // register cards used to make parallel combinations under their code
            if (allParallel.contains(c.getCode())) {
                var list = parallelCards.get(c.getCode());
                if (list == null) {
                    list = new ArrayList<>();
                    parallelCards.put(c.getCode(), list);
                }
                list.add(c);
            }
        }
        for (var p : parallel) {
            String firstCodeR = null;
            var sortAdd = 0;
            for (var codeR : p.getRegular()) {
                if (firstCodeR == null) {
                    firstCodeR = codeR;
                }
                for (var codeP : p.getParallel()) {
                    var clr = parallelCards.get(codeR);
                    var clp = parallelCards.get(codeP);
                    if (clr != null && clp != null) {
                        clr.sort(null);
                        clp.sort(null);
                        for (var cr : clr) {
                            cr.parallelContent();
                            for (var cp : clp) {
                                cp.miniCode(p.isSameArt() ? firstCodeR : cp.getCode());
                                cards.add(cp.parallelClone(cr, cp.getId(), sortAdd, p.isSameArt() ? cr.getCode() : cp.getCode()));
                                cards.add(cr.parallelClone(cp, cp.getId(), sortAdd + 1, cr.getCode()));
                            }
                        }
                    } else {
                        log("Missing parallel cards data for %s and/or %s", codeR, codeP);
                    }
                    sortAdd += 2;
                }
            }
        }
        // sort cards according to canonical order again (same for bondedCards lists)
        cards.sort(null);
        for (var list : bondedCards.values()) {
            list.sort(null);
        }
    }

}
