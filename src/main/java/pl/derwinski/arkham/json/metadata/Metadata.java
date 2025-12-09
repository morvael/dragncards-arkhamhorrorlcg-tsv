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
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import pl.derwinski.arkham.Util;
import static pl.derwinski.arkham.Util.log;

/**
 *
 * @author morvael
 */
public final class Metadata {

    private static final HashSet<String> unhandled = new HashSet<>();
    private static final HashSet<String> unhandledPacks = new HashSet<>();
    private static final HashSet<String> unhandledCycles = new HashSet<>();
    private static final HashSet<String> unhandledEncounters = new HashSet<>();
    private static final HashSet<Integer> unhandledTaboos = new HashSet<>();
    private static final HashSet<String> unhandledTypes = new HashSet<>();
    private static final HashSet<String> unhandledFactions = new HashSet<>();
    private static final HashSet<String> unhandledSubtypes = new HashSet<>();

    public static Metadata loadMetadata() throws Exception {
        Util.downloadIfOld("https://api.arkham.build/v1/cache/metadata/en", "run/metadata.json");
        return loadMetadata("run/metadata.json");
    }

    public static Metadata loadMetadata(String path) throws Exception {
        var file = new File(path);
        var c = new JsonMapper().readTree(file).findValue("data");
        if (c != null) {
            return loadMetadata(c);
        } else {
            log("Error reading Metadata file");
            return null;
        }
    }

    public static Metadata loadMetadata(JsonNode c) throws Exception {
        if (c.isObject()) {
            var o = new Metadata();
            var it = c.fieldNames();
            while (it.hasNext()) {
                var fieldName = it.next();
                switch (fieldName) {
                    case "pack":
                        o.packs = Collections.unmodifiableMap(MetadataPack.readMetadataPacks(c.get(fieldName)));
                        break;
                    case "cycle":
                        o.cycles = Collections.unmodifiableMap(MetadataCycle.readMetadataCycles(c.get(fieldName)));
                        break;
                    case "card_encounter_set":
                        o.encounters = Collections.unmodifiableMap(MetadataEncounterSet.readMetadataEncounterSets(c.get(fieldName)));
                        break;
                    case "taboo_set":
                        o.taboos = Collections.unmodifiableMap(MetadataTabooSet.readMetadataTabooSets(c.get(fieldName)));
                        break;
                    default:
                        if (unhandled.add(fieldName)) {
                            log("Unhandled field name in Metadata: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType());
                        }
                        break;
                }
            }
            return o;
        } else {
            if (c.isNull() == false) {
                log("Error reading Metadata object: %s", c.asText());
            }
            return null;
        }
    }

    private Map<String, MetadataPack> packs;
    private Map<String, MetadataCycle> cycles;
    private Map<String, MetadataEncounterSet> encounters;
    private Map<Integer, MetadataTabooSet> taboos;

    private Metadata() {

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
                log("Unhandled pack code: %s", packCode);
            }
            return null;
        }
    }

    public String getCycleName(String cycleCode) {
        if (cycleCode == null) {
            return null;
        }
        var c = cycles.get(cycleCode);
        if (c != null) {
            return c.getName();
        } else {
            if (unhandledCycles.add(cycleCode)) {
                log("Unhandled cycle code: %s", cycleCode);
            }
            return null;
        }
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
                log("Unhandled encounter code: %s", encounterCode);
            }
            return null;
        }
    }

    public String getTabooName(Integer tabooSetId) {
        if (tabooSetId == null) {
            return null;
        }
        if (tabooSetId == 0) {
            return "None";
        }
        var es = taboos.get(tabooSetId);
        if (es != null) {
            return es.getName();
        } else {
            if (unhandledTaboos.add(tabooSetId)) {
                log("Unhandled taboo set id: %d", tabooSetId);
            }
            return null;
        }
    }

    public String getTypeName(String typeCode) {
        return switch (typeCode) {
            case null ->
                null;
            case "act" ->
                "Act";
            case "agenda" ->
                "Agenda";
            case "asset" ->
                "Asset";
            case "enemy" ->
                "Enemy";
            case "enemy_location" ->
                "Enemy Location";
            case "event" ->
                "Event";
            case "investigator" ->
                "Investigator";
            case "key" ->
                "Key";
            case "location" ->
                "Location";
            case "reference" ->
                "Reference"; //local type for use in overrides
            case "scenario" ->
                "Scenario";
            case "skill" ->
                "Skill";
            case "story" ->
                "Story";
            case "treachery" ->
                "Treachery";
            default -> {
                if (unhandledTypes.add(typeCode)) {
                    log("Unhandled type code: %s", typeCode);
                }
                yield null;
            }
        };
    }

    public String getFactionName(String factionCode) {
        return switch (factionCode) {
            case "guardian" ->
                "Guardian";
            case "mystic" ->
                "Mystic";
            case "mythos" ->
                "Mythos";
            case "neutral" ->
                "Neutral";
            case "rogue" ->
                "Rogue";
            case "seeker" ->
                "Seeker";
            case "survivor" ->
                "Survivor";
            default -> {
                if (unhandledFactions.add(factionCode)) {
                    log("Unhandled faction code: %s", factionCode);
                }
                yield null;
            }
        };
    }

    public String getSubtypeName(String subtypeCode) {
        return switch (subtypeCode) {
            case null ->
                null;
            case "basicweakness" ->
                "Basic Weakness";
            case "weakness" ->
                "Weakness";
            default -> {
                if (unhandledSubtypes.add(subtypeCode)) {
                    log("Unhandled subtype code: %s", subtypeCode);
                }
                yield null;
            }
        };
    }

    public int getLatestTabooSetId() {
        var max = 0;
        for (var t : taboos.values()) {
            if (t.getId() != null && t.getId() > max) {
                max = t.getId();
            }
        }
        return max;
    }

}
