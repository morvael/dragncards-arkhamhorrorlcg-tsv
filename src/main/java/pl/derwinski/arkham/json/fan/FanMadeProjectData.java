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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import static pl.derwinski.arkham.Util.log;
import pl.derwinski.arkham.json.Card;
import static pl.derwinski.arkham.json.Cards.initializeCardMap;
import pl.derwinski.arkham.json.configuration.Configuration;
import pl.derwinski.arkham.json.metadata.Metadata;
import pl.derwinski.arkham.json.metadata.MetadataEncounterSet;
import pl.derwinski.arkham.json.metadata.MetadataPack;

/**
 *
 * @author morvael
 */
public final class FanMadeProjectData implements Iterable<Card> {

    private static final HashSet<String> unhandled = new HashSet<>();
    private static final HashSet<String> unhandledPacks = new HashSet<>();
    private static final HashSet<String> unhandledEncounters = new HashSet<>();

    public static FanMadeProjectData readFanMadeProjectData(Configuration configuration, Metadata metadata, String projectCode, JsonNode c) throws Exception {
        if (c.isObject()) {
            var o = new FanMadeProjectData(configuration, metadata);
            var it = c.fieldNames();
            while (it.hasNext()) {
                var fieldName = it.next();
                switch (fieldName) {
                    case "encounter_sets":
                        o.encounters = Collections.unmodifiableMap(MetadataEncounterSet.readMetadataEncounterSets(c.get(fieldName)));
                        metadata.addEncounters(o.encounters);
                        break;
                    case "packs":
                        o.packs = Collections.unmodifiableMap(MetadataPack.readMetadataPacks(c.get(fieldName)));
                        metadata.addPacks(o.packs);
                        configuration.addPacks(o.packs);
                        break;
                    // ignored fields
                    case "cards":
                        break;
                    default:
                        if (unhandled.add(fieldName)) {
                            log("Unhandled field name in FanMadeProjectData: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType());
                        }
                        break;
                }
            }
            it = c.fieldNames();
            while (it.hasNext()) {
                var fieldName = it.next();
                switch (fieldName) {
                    case "cards":
                        o.cards = Collections.unmodifiableList(Card.readCards(configuration, metadata, projectCode, c.get(fieldName)));
                        break;
                    // ignored fields
                    case "encounter_sets":
                    case "packs":
                        break;
                    default:
                        if (unhandled.add(fieldName)) {
                            log("Unhandled field name in FanMadeProjectData: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType());
                        }
                        break;
                }
            }
            initializeCardMap(configuration, o.cards, o.map);
            return o;
        } else {
            if (c.isNull() == false) {
                log("Error reading FanMadeProjectData object: %s", c.asText());
            }
            return null;
        }
    }

    private final Configuration configuration;
    private final Metadata metadata;
    private final HashMap<String, Card> map = new HashMap<>();

    private List<Card> cards;
    private Map<String, MetadataEncounterSet> encounters;
    private Map<String, MetadataPack> packs;

    public FanMadeProjectData(Configuration configuration, Metadata metadata) {
        this.configuration = configuration;
        this.metadata = metadata;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public HashMap<String, Card> getMap() {
        return map;
    }

    public List<Card> getCards() {
        return cards;
    }

    @Override
    public Iterator<Card> iterator() {
        return cards.iterator();
    }

    public String getEncounterName(String encounterCode) {
        if (encounterCode == null) {
            return null;
        }
        var es = encounters.get(encounterCode);
        if (es != null) {
            return es.getName();
        } else {
            if (unhandledEncounters.add(encounterCode)) {
                log("Unhandled fan encounter code: %s", encounterCode);
            }
            return null;
        }
    }

    public String getPackName(String packCode) {
        if (packCode == null) {
            return null;
        }
        var p = packs.get(packCode);
        if (p != null) {
            return p.getName();
        } else {
            if (unhandledPacks.add(packCode)) {
                log("Unhandled fan pack code: %s", packCode);
            }
            return null;
        }
    }

    public Card getCard(String code) {
        return map.get(code);
    }

}
