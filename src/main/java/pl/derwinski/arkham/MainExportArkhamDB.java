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
package pl.derwinski.arkham;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import static pl.derwinski.arkham.Util.log;
import static pl.derwinski.arkham.Util.readBoolean;
import static pl.derwinski.arkham.Util.readInteger;
import static pl.derwinski.arkham.Util.readString;
import static pl.derwinski.arkham.Util.readStringList;
import static pl.derwinski.arkham.Util.readStringStringMap;
import pl.derwinski.arkham.json.BondedCard;
import pl.derwinski.arkham.json.Card;
import pl.derwinski.arkham.json.Cards;
import pl.derwinski.arkham.json.CustomizationOption;
import pl.derwinski.arkham.json.CustomizationOptionCard;
import pl.derwinski.arkham.json.DeckOption;
import pl.derwinski.arkham.json.DeckOptionAtLeast;
import pl.derwinski.arkham.json.DeckOptionLevel;
import pl.derwinski.arkham.json.DeckRequirements;
import pl.derwinski.arkham.json.DeckRequirementsRandom;
import pl.derwinski.arkham.json.ErrataDate;
import pl.derwinski.arkham.json.Restrictions;

/**
 * Console program to convert ArkhamDB json into TSV file.
 *
 * @author morvael
 */
public class MainExportArkhamDB {

    protected final HashMap<String, String> mapping;
    protected final HashMap<String, String> titles;
    protected final HashMap<String, String> types;
    protected final HashMap<String, String> weaknesses;
    protected final HashMap<String, String> backOverrides;
    protected final HashSet<String> flipped;
    protected final HashSet<String> unhidden;
    protected final HashSet<String> ignoreDoublesided;
    protected final HashSet<String> skipBonded;
    protected final HashSet<String> bondedForEach;
    protected final HashMap<String, String> parallel;
    protected final HashMap<String, String> parallelMini;
    protected final HashSet<String> ignorePaths;
    protected final HashSet<String> forceDoublesided;
    protected final HashSet<String> ignoreCards;
    protected final HashSet<String> backOverridesVerified;
    protected final HashMap<String, String> quantities;

    protected final HashSet<String> unhandledDeckRequirementsRandom = new HashSet<>();
    protected final HashSet<String> unhandledDeckRequirement = new HashSet<>();
    protected final HashSet<String> unhandledDeckOptionLevel = new HashSet<>();
    protected final HashSet<String> unhandledDeckOptionAtLeast = new HashSet<>();
    protected final HashSet<String> unhandledDeckOption = new HashSet<>();
    protected final HashSet<String> unhandledErrataDate = new HashSet<>();
    protected final HashSet<String> unhandledRestrictions = new HashSet<>();
    protected final HashSet<String> unhandledBondedCard = new HashSet<>();
    protected final HashSet<String> unhandledCustomizationOptionCard = new HashSet<>();
    protected final HashSet<String> unhandledCustomizationOption = new HashSet<>();
    protected final HashSet<String> unhandledCard = new HashSet<>();

    protected final HashMap<String, ArrayList<Card>> bondedCards = new HashMap<>();
    protected final HashMap<String, Card> parallelCards = new HashMap<>();

    protected boolean writeTab;

    public MainExportArkhamDB() throws Exception {
        mapping = Util.readConfigMap("run/mapping.txt");
        titles = Util.readConfigMap("run/titles.txt");
        types = Util.readConfigMap("run/types.txt");
        weaknesses = Util.readConfigMap("run/weaknesses.txt");
        backOverrides = Util.readConfigMap("run/backOverrides.txt");
        flipped = Util.readConfigSet("run/flipped.txt");
        unhidden = Util.readConfigSet("run/unhidden.txt");
        ignoreDoublesided = Util.readConfigSet("run/ignoreDoublesided.txt");
        skipBonded = Util.readConfigSet("run/skipBonded.txt");
        bondedForEach = Util.readConfigSet("run/bondedForEach.txt");
        parallel = Util.readConfigMap("run/parallel.txt");
        for (Map.Entry<String, String> e : new HashMap<>(parallel).entrySet()) {
            parallel.put(e.getValue(), e.getKey());
        }
        parallelMini = Util.readConfigMap("run/parallelMini.txt");
        ignorePaths = Util.readConfigSet("run/ignorePaths.txt");
        forceDoublesided = Util.readConfigSet("run/forceDoublesided.txt");
        ignoreCards = Util.readConfigSet("run/ignoreCards.txt");
        backOverridesVerified = Util.readConfigSet("run/backOverridesVerified.txt");
        quantities = Util.readConfigMap("run/quantities.txt");
    }

    protected DeckRequirementsRandom readDeckRequirementsRandom(JsonNode c) throws Exception {
        if (c.isObject()) {
            DeckRequirementsRandom randomRequirement = new DeckRequirementsRandom();
            Iterator<String> it = c.fieldNames();
            while (it.hasNext()) {
                String fieldName = it.next();
                switch (fieldName) {
                    case "target":
                        randomRequirement.setTarget(readString(c.get(fieldName)));
                        break;
                    case "value":
                        randomRequirement.setValue(readString(c.get(fieldName)));
                        break;
                    default:
                        if (unhandledDeckRequirementsRandom.add(fieldName)) {
                            log("Unhandled field name in DeckRequirementsRandom: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType());
                        }
                        break;
                }
            }
            return randomRequirement;
        } else {
            if (c.isNull() == false) {
                log("Error reading DeckRequirementsRandom field: %s", c.asText());
            }
            return null;
        }
    }

    protected DeckRequirements readDeckRequirements(JsonNode c) throws Exception {
        if (c.isObject()) {
            DeckRequirements deckRequirements = new DeckRequirements();
            Iterator<String> it = c.fieldNames();
            while (it.hasNext()) {
                String fieldName = it.next();
                switch (fieldName) {
                    case "size":
                        deckRequirements.setSize(readInteger(c.get(fieldName)));
                        break;
                    case "card": {
                        JsonNode cc = c.get(fieldName);
                        if (cc.isObject()) {
                            deckRequirements.setCard(new LinkedHashMap<>());
                            Iterator<String> it2 = cc.fieldNames();
                            while (it2.hasNext()) {
                                String fieldName2 = it2.next();
                                deckRequirements.getCard().put(fieldName2, readStringStringMap(cc.get(fieldName2)));
                            }
                        } else {
                            if (c.isNull() == false) {
                                log("Error reading DeckRequirements object field %s: %s", fieldName, cc.asText());
                            }
                        }
                        break;
                    }
                    case "random": {
                        JsonNode cc = c.get(fieldName);
                        if (cc.isArray()) {
                            deckRequirements.setRandom(new ArrayList<>(cc.size()));
                            for (int j = 0; j < cc.size(); j++) {
                                deckRequirements.getRandom().add(readDeckRequirementsRandom(cc.get(j)));
                            }
                        } else {
                            if (c.isNull() == false) {
                                log("Error reading DeckRequirements array field %s: %s", fieldName, cc.asText());
                            }
                        }
                        break;
                    }
                    default:
                        if (unhandledDeckRequirement.add(fieldName)) {
                            log("Unhandled field name in DeckRequirements: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType());
                        }
                        break;
                }
            }
            return deckRequirements;
        } else {
            if (c.isNull() == false) {
                log("Error reading DeckRequirements field: %s", c.asText());
            }
            return null;
        }
    }

    protected DeckOptionLevel readDeckOptionLevel(JsonNode c) throws Exception {
        if (c.isObject()) {
            DeckOptionLevel deckOptionLevel = new DeckOptionLevel();
            Iterator<String> it = c.fieldNames();
            while (it.hasNext()) {
                String fieldName = it.next();
                switch (fieldName) {
                    case "min":
                        deckOptionLevel.setMin(readInteger(c.get(fieldName)));
                        break;
                    case "max":
                        deckOptionLevel.setMax(readInteger(c.get(fieldName)));
                        break;
                    default:
                        if (unhandledDeckOptionLevel.add(fieldName)) {
                            log("Unhandled field name in DeckOptionLevel: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType());
                        }
                        break;
                }
            }
            return deckOptionLevel;
        } else {
            if (c.isNull() == false) {
                log("Error reading DeckOptionLevel field: %s", c.asText());
            }
            return null;
        }
    }

    protected DeckOptionAtLeast readDeckOptionAtLeast(JsonNode c) throws Exception {
        if (c.isObject()) {
            DeckOptionAtLeast deckOptionAtLeast = new DeckOptionAtLeast();
            Iterator<String> it = c.fieldNames();
            while (it.hasNext()) {
                String fieldName = it.next();
                switch (fieldName) {
                    case "factions":
                        deckOptionAtLeast.setFactions(readInteger(c.get(fieldName)));
                        break;
                    case "min":
                        deckOptionAtLeast.setMin(readInteger(c.get(fieldName)));
                        break;
                    default:
                        if (unhandledDeckOptionAtLeast.add(fieldName)) {
                            log("Unhandled field name in DeckOptionAtLeast: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType());
                        }
                        break;
                }
            }
            return deckOptionAtLeast;
        } else {
            if (c.isNull() == false) {
                log("Error reading DeckOptionAtLeast field: %s", c.asText());
            }
            return null;
        }
    }

    protected DeckOption readDeckOption(JsonNode c) throws Exception {
        if (c.isObject()) {
            DeckOption deckOption = new DeckOption();
            Iterator<String> it = c.fieldNames();
            while (it.hasNext()) {
                String fieldName = it.next();
                switch (fieldName) {
                    case "faction":
                        deckOption.setFaction(readStringList(c.get(fieldName)));
                        break;
                    case "level":
                        deckOption.setLevel(readDeckOptionLevel(c.get(fieldName)));
                        break;
                    case "limit":
                        deckOption.setLimit(readInteger(c.get(fieldName)));
                        break;
                    case "error":
                        deckOption.setError(readString(c.get(fieldName)));
                        break;
                    case "not":
                        deckOption.setNot(readBoolean(c.get(fieldName)));
                        break;
                    case "trait":
                        deckOption.setTrait(readStringList(c.get(fieldName)));
                        break;
                    case "tag":
                        deckOption.setTag(readStringList(c.get(fieldName)));
                        break;
                    case "atleast":
                        deckOption.setAtLeast(readDeckOptionAtLeast(c.get(fieldName)));
                        break;
                    case "uses":
                        deckOption.setUses(readStringList(c.get(fieldName)));
                        break;
                    case "text":
                        deckOption.setText(readStringList(c.get(fieldName)));
                        break;
                    case "name":
                        deckOption.setName(readString(c.get(fieldName)));
                        break;
                    case "faction_select":
                        deckOption.setFactionSelect(readStringList(c.get(fieldName)));
                        break;
                    case "type":
                        deckOption.setType(readStringList(c.get(fieldName)));
                        break;
                    case "deck_size_select":
                        deckOption.setDeckSizeSelect(readStringList(c.get(fieldName)));
                        break;
                    case "slot":
                        deckOption.setSlot(readStringList(c.get(fieldName)));
                        break;
                    case "option_select": {
                        JsonNode cc = c.get(fieldName);
                        if (cc.isArray()) {
                            deckOption.setOptionSelect(new ArrayList<>(cc.size()));
                            for (int j = 0; j < cc.size(); j++) {
                                deckOption.getOptionSelect().add(readDeckOption(cc.get(j)));
                            }
                        } else {
                            if (cc.isNull() == false) {
                                log("Error reading DeckOption array field %s: %s", fieldName, cc.asText());
                            }
                        }
                        break;
                    }
                    case "id":
                        deckOption.setId(readString(c.get(fieldName)));
                        break;
                    case "permanent":
                        deckOption.setPermanent(readBoolean(c.get(fieldName)));
                        break;
                    case "base_level":
                        deckOption.setBaseLevel(readDeckOptionLevel(c.get(fieldName)));
                        break;
                    case "size":
                        deckOption.setSize(readInteger(c.get(fieldName)));
                        break;
                    default:
                        if (unhandledDeckOption.add(fieldName)) {
                            log("Unhandled field name in DeckOption: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType());
                        }
                        break;
                }
            }
            return deckOption;
        } else {
            if (c.isNull() == false) {
                log("Error reading DeckOption field: %s", c.asText());
            }
            return null;
        }
    }

    protected ErrataDate readErrataDate(JsonNode c) throws Exception {
        if (c.isObject()) {
            ErrataDate errataDate = new ErrataDate();
            Iterator<String> it = c.fieldNames();
            while (it.hasNext()) {
                String fieldName = it.next();
                switch (fieldName) {
                    case "date":
                        errataDate.setDate(readString(c.get(fieldName)));
                        break;
                    case "timezone_type":
                        errataDate.setTimezoneType(readInteger(c.get(fieldName)));
                        break;
                    case "timezone":
                        errataDate.setTimezone(readString(c.get(fieldName)));
                        break;
                    default:
                        if (unhandledErrataDate.add(fieldName)) {
                            log("Unhandled field name in ErrataDate: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType());
                        }
                        break;
                }
            }
            return errataDate;
        } else {
            if (c.isNull() == false) {
                log("Error reading ErrataDate field: %s", c.asText());
            }
            return null;
        }
    }

    protected Restrictions readRestrictions(JsonNode c) throws Exception {
        if (c.isObject()) {
            Restrictions restrictions = new Restrictions();
            Iterator<String> it = c.fieldNames();
            while (it.hasNext()) {
                String fieldName = it.next();
                switch (fieldName) {
                    case "investigator":
                        restrictions.setInvestigator(readStringStringMap(c.get(fieldName)));
                        break;
                    default:
                        if (unhandledRestrictions.add(fieldName)) {
                            log("Unhandled field name in Restrictions: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType());
                        }
                        break;
                }
            }
            return restrictions;
        } else {
            if (c.isNull() == false) {
                log("Error reading Restrictions field: %s", c.asText());
            }
            return null;
        }
    }

    protected BondedCard readBondedCard(JsonNode c) throws Exception {
        if (c.isObject()) {
            BondedCard bondedCard = new BondedCard();
            Iterator<String> it = c.fieldNames();
            while (it.hasNext()) {
                String fieldName = it.next();
                switch (fieldName) {
                    case "count":
                        bondedCard.setCount(readInteger(c.get(fieldName)));
                        break;
                    case "code":
                        bondedCard.setCode(readString(c.get(fieldName)));
                        break;
                    default:
                        if (unhandledBondedCard.add(fieldName)) {
                            log("Unhandled field name in BondedCard: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType());
                        }
                        break;
                }
            }
            return bondedCard;
        } else {
            if (c.isNull() == false) {
                log("Error reading BondedCard field: %s", c.asText());
            }
            return null;
        }
    }

    protected CustomizationOptionCard readCustomizationOptionCard(JsonNode c) throws Exception {
        if (c.isObject()) {
            CustomizationOptionCard customizationOptionCard = new CustomizationOptionCard();
            Iterator<String> it = c.fieldNames();
            while (it.hasNext()) {
                String fieldName = it.next();
                switch (fieldName) {
                    case "type":
                        customizationOptionCard.setType(readStringList(c.get(fieldName)));
                        break;
                    case "trait":
                        customizationOptionCard.setTrait(readStringList(c.get(fieldName)));
                        break;
                    default:
                        if (unhandledCustomizationOptionCard.add(fieldName)) {
                            log("Unhandled field name in CustomizationOptionCard: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType());
                        }
                        break;
                }
            }
            return customizationOptionCard;
        } else {
            if (c.isNull() == false) {
                log("Error reading CustomizationOptionCard field: %s", c.asText());
            }
            return null;
        }
    }

    protected CustomizationOption readCustomizationOption(JsonNode c) throws Exception {
        if (c.isObject()) {
            CustomizationOption customizationOption = new CustomizationOption();
            Iterator<String> it = c.fieldNames();
            while (it.hasNext()) {
                String fieldName = it.next();
                switch (fieldName) {
                    case "xp":
                        customizationOption.setXp(readInteger(c.get(fieldName)));
                        break;
                    case "real_traits":
                        customizationOption.setRealTraits(readString(c.get(fieldName)));
                        break;
                    case "real_slot":
                        customizationOption.setRealSlot(readString(c.get(fieldName)));
                        break;
                    case "text_change":
                        customizationOption.setTextChange(readString(c.get(fieldName)));
                        break;
                    case "health":
                        customizationOption.setHealth(readInteger(c.get(fieldName)));
                        break;
                    case "sanity":
                        customizationOption.setSanity(readInteger(c.get(fieldName)));
                        break;
                    case "cost":
                        customizationOption.setCost(readInteger(c.get(fieldName)));
                        break;
                    case "real_text":
                        customizationOption.setRealText(readString(c.get(fieldName)));
                        break;
                    case "tags":
                        customizationOption.setTags(readString(c.get(fieldName)));
                        break;
                    case "position":
                        customizationOption.setPosition(readInteger(c.get(fieldName)));
                        break;
                    case "choice":
                        customizationOption.setChoice(readString(c.get(fieldName)));
                        break;
                    case "quantity":
                        customizationOption.setQuantity(readInteger(c.get(fieldName)));
                        break;
                    case "card":
                        customizationOption.setCard(readCustomizationOptionCard(c.get(fieldName)));
                        break;
                    case "deck_limit":
                        customizationOption.setDeckLimit(readInteger(c.get(fieldName)));
                        break;
                    default:
                        if (unhandledCustomizationOption.add(fieldName)) {
                            log("Unhandled field name in CustomizationOption: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType());
                        }
                        break;
                }
            }
            return customizationOption;
        } else {
            if (c.isNull() == false) {
                log("Error reading CustomizationOption field: %s", c.asText());
            }
            return null;
        }
    }

    protected Card readCard(JsonNode c) throws Exception {
        if (c.isObject()) {
            Card card = new Card();
            Iterator<String> it = c.fieldNames();
            while (it.hasNext()) {
                String fieldName = it.next();
                switch (fieldName) {
                    case "pack_code":
                        card.setPackCode(readString(c.get(fieldName)));
                        break;
                    case "pack_name":
                        card.setPackName(readString(c.get(fieldName)));
                        break;
                    case "type_code":
                        card.setTypeCode(readString(c.get(fieldName)));
                        break;
                    case "type_name":
                        card.setTypeName(readString(c.get(fieldName)));
                        break;
                    case "faction_code":
                        card.setFactionCode(readString(c.get(fieldName)));
                        break;
                    case "faction_name":
                        card.setFactionName(readString(c.get(fieldName)));
                        break;
                    case "position":
                        card.setPosition(readInteger(c.get(fieldName)));
                        break;
                    case "exceptional":
                        card.setExceptional(readBoolean(c.get(fieldName)));
                        break;
                    case "myriad":
                        card.setMyriad(readBoolean(c.get(fieldName)));
                        break;
                    case "code":
                        card.setCode(readString(c.get(fieldName)));
                        break;
                    case "name":
                        card.setName(readString(c.get(fieldName)));
                        break;
                    case "real_name":
                        card.setRealName(readString(c.get(fieldName)));
                        break;
                    case "subname":
                        card.setSubname(readString(c.get(fieldName)));
                        break;
                    case "text":
                        card.setText(readString(c.get(fieldName)));
                        break;
                    case "real_text":
                        card.setRealText(readString(c.get(fieldName)));
                        break;
                    case "quantity":
                        card.setQuantity(readInteger(c.get(fieldName)));
                        break;
                    case "skill_willpower":
                        card.setSkillWillpower(readInteger(c.get(fieldName)));
                        break;
                    case "skill_intellect":
                        card.setSkillIntellect(readInteger(c.get(fieldName)));
                        break;
                    case "skill_combat":
                        card.setSkillCombat(readInteger(c.get(fieldName)));
                        break;
                    case "skill_agility":
                        card.setSkillAgility(readInteger(c.get(fieldName)));
                        break;
                    case "health":
                        card.setHealth(readInteger(c.get(fieldName)));
                        break;
                    case "health_per_investigator":
                        card.setHealthPerInvestigator(readBoolean(c.get(fieldName)));
                        break;
                    case "sanity":
                        card.setSanity(readInteger(c.get(fieldName)));
                        break;
                    case "deck_limit":
                        card.setDeckLimit(readInteger(c.get(fieldName)));
                        break;
                    case "real_slot":
                        card.setRealSlot(readString(c.get(fieldName)));
                        break;
                    case "traits":
                        card.setTraits(readString(c.get(fieldName)));
                        break;
                    case "real_traits":
                        card.setRealTraits(readString(c.get(fieldName)));
                        break;
                    case "deck_requirements":
                        card.setDeckRequirements(readDeckRequirements(c.get(fieldName)));
                        break;
                    case "deck_options": {
                        JsonNode cc = c.get(fieldName);
                        if (cc.isArray()) {
                            card.setDeckOptions(new ArrayList<>(cc.size()));
                            for (int j = 0; j < cc.size(); j++) {
                                card.getDeckOptions().add(readDeckOption(cc.get(j)));
                            }
                        } else {
                            if (cc.isNull() == false) {
                                log("Error reading Card array field %s: %s", fieldName, cc.asText());
                            }
                        }
                        break;
                    }
                    case "flavor":
                        card.setFlavor(readString(c.get(fieldName)));
                        break;
                    case "illustrator":
                        card.setIllustrator(readString(c.get(fieldName)));
                        break;
                    case "is_unique":
                        card.setUnique(readBoolean(c.get(fieldName)));
                        break;
                    case "permanent":
                        card.setPermanent(readBoolean(c.get(fieldName)));
                        break;
                    case "double_sided":
                        card.setDoubleSided(readBoolean(c.get(fieldName)));
                        break;
                    case "back_text":
                        card.setBackText(readString(c.get(fieldName)));
                        break;
                    case "back_flavor":
                        card.setBackFlavor(readString(c.get(fieldName)));
                        break;
                    case "octgn_id":
                        card.setOctgnId(readString(c.get(fieldName)));
                        break;
                    case "url":
                        card.setUrl(readString(c.get(fieldName)));
                        break;
                    case "imagesrc":
                        card.setImagesrc(readString(c.get(fieldName)));
                        break;
                    case "backimagesrc":
                        card.setBackimagesrc(readString(c.get(fieldName)));
                        break;
                    case "duplicated_by":
                        card.setDuplicatedBy(readStringList(c.get(fieldName)));
                        break;
                    case "alternated_by":
                        card.setAlternatedBy(readStringList(c.get(fieldName)));
                        break;
                    case "cost":
                        card.setCost(readInteger(c.get(fieldName)));
                        break;
                    case "xp":
                        card.setXp(readInteger(c.get(fieldName)));
                        break;
                    case "slot":
                        card.setSlot(readString(c.get(fieldName)));
                        break;
                    case "subtype_code":
                        card.setSubtypeCode(readString(c.get(fieldName)));
                        break;
                    case "subtype_name":
                        card.setSubtypeName(readString(c.get(fieldName)));
                        break;
                    case "errata_date":
                        card.setErrataDate(readErrataDate(c.get(fieldName)));
                        break;
                    case "skill_wild":
                        card.setSkillWild(readInteger(c.get(fieldName)));
                        break;
                    case "restrictions":
                        card.setRestrictions(readRestrictions(c.get(fieldName)));
                        break;
                    case "encounter_code":
                        card.setEncounterCode(readString(c.get(fieldName)));
                        break;
                    case "encounter_name":
                        card.setEncounterName(readString(c.get(fieldName)));
                        break;
                    case "encounter_position":
                        card.setEncounterPosition(readInteger(c.get(fieldName)));
                        break;
                    case "spoiler":
                        card.setSpoiler(readInteger(c.get(fieldName)));
                        break;
                    case "enemy_damage":
                        card.setEnemyDamage(readInteger(c.get(fieldName)));
                        break;
                    case "enemy_horror":
                        card.setEnemyHorror(readInteger(c.get(fieldName)));
                        break;
                    case "enemy_fight":
                        card.setEnemyFight(readInteger(c.get(fieldName)));
                        break;
                    case "enemy_evade":
                        card.setEnemyEvade(readInteger(c.get(fieldName)));
                        break;
                    case "victory":
                        card.setVictory(readInteger(c.get(fieldName)));
                        break;
                    case "shroud":
                        card.setShroud(readInteger(c.get(fieldName)));
                        break;
                    case "clues":
                        card.setClues(readInteger(c.get(fieldName)));
                        break;
                    case "doom":
                        card.setDoom(readInteger(c.get(fieldName)));
                        break;
                    case "stage":
                        card.setStage(readInteger(c.get(fieldName)));
                        break;
                    case "back_name":
                        card.setBackName(readString(c.get(fieldName)));
                        break;
                    case "tags":
                        card.setTags(readString(c.get(fieldName)));
                        break;
                    case "linked_to_code":
                        card.setLinkedToCode(readString(c.get(fieldName)));
                        break;
                    case "linked_to_name":
                        card.setLinkedToName(readString(c.get(fieldName)));
                        break;
                    case "linked_card":
                        card.setLinkedCard(readCard(c.get(fieldName)));
                        break;
                    case "hidden":
                        card.setHidden(readBoolean(c.get(fieldName)));
                        break;
                    case "clues_fixed":
                        card.setCluesFixed(readBoolean(c.get(fieldName)));
                        break;
                    case "exile":
                        card.setExile(readBoolean(c.get(fieldName)));
                        break;
                    case "vengeance":
                        card.setVengeance(readInteger(c.get(fieldName)));
                        break;
                    case "faction2_code":
                        card.setFaction2Code(readString(c.get(fieldName)));
                        break;
                    case "faction2_name":
                        card.setFaction2Name(readString(c.get(fieldName)));
                        break;
                    case "bonded_cards": {
                        JsonNode cc = c.get(fieldName);
                        if (cc.isArray()) {
                            card.setBondedCards(new ArrayList<>(cc.size()));
                            for (int j = 0; j < cc.size(); j++) {
                                card.getBondedCards().add(readBondedCard(cc.get(j)));
                            }
                        } else {
                            if (cc.isNull() == false) {
                                log("Error reading Card array field %s: %s", fieldName, cc.asText());
                            }
                        }
                        break;
                    }
                    case "bonded_to":
                        card.setBondedTo(readString(c.get(fieldName)));
                        break;
                    case "bonded_count":
                        card.setBondedCount(readInteger(c.get(fieldName)));
                        break;
                    case "alternate_of_code":
                        card.setAlternateOfCode(readString(c.get(fieldName)));
                        break;
                    case "alternate_of_name":
                        card.setAlternateOfName(readString(c.get(fieldName)));
                        break;
                    case "duplicate_of_code":
                        card.setDuplicateOfCode(readString(c.get(fieldName)));
                        break;
                    case "duplicate_of_name":
                        card.setDuplicateOfName(readString(c.get(fieldName)));
                        break;
                    case "faction3_code":
                        card.setFaction3Code(readString(c.get(fieldName)));
                        break;
                    case "faction3_name":
                        card.setFaction3Name(readString(c.get(fieldName)));
                        break;
                    case "customization_text":
                        card.setCustomizationText(readString(c.get(fieldName)));
                        break;
                    case "customization_change":
                        card.setCustomizationChange(readString(c.get(fieldName)));
                        break;
                    case "customization_options": {
                        JsonNode cc = c.get(fieldName);
                        if (cc.isArray()) {
                            card.setCustomizationOptions(new ArrayList<>(cc.size()));
                            for (int j = 0; j < cc.size(); j++) {
                                card.getCustomizationOptions().add(readCustomizationOption(cc.get(j)));
                            }
                        } else {
                            if (cc.isNull() == false) {
                                log("Error reading Card array field %s: %s", fieldName, cc.asText());
                            }
                        }
                        break;
                    }
                    case "id":
                        card.setId(readInteger(c.get(fieldName)));
                        break;
                    default:
                        if (unhandledCard.add(fieldName)) {
                            log("Unhandled field name in Card: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType());
                        }
                        break;
                }
            }
            if (card.getBondedTo() != null && card.getBondedTo().length() > 0 && card.getBondedCount() != null && card.getBondedCount() > 0) {
                ArrayList<Card> list = bondedCards.get(card.getBondedTo());
                if (list == null) {
                    list = new ArrayList<>();
                    bondedCards.put(card.getBondedTo(), list);
                }
                list.add(card);
            }
            if (parallel.containsKey(card.getCode())) {
                parallelCards.put(card.getCode(), card);
            }
            return card;
        } else {
            if (c.isNull() == false) {
                log("Error reading Card field: %s", c.asText());
            }
            return null;
        }
    }

    protected Cards loadCards(String path) throws Exception {
        File file = new File(path);
        JsonNode c = new JsonMapper().readTree(file);
        if (c.isArray()) {
            Cards cards = new Cards();
            cards.setCards(new ArrayList<>(c.size()));
            for (int i = 0; i < c.size(); i++) {
                Card cc = readCard(c.get(i));
                if (ignoreCards.contains(cc.getCode())) {
                    continue;
                }
                cards.getCards().add(cc);
            }
            for (Map.Entry<String, String> e : parallel.entrySet()) {
                Card c1 = parallelCards.get(e.getKey());
                Card c2 = parallelCards.get(e.getValue());
                if (c1 != null && c2 != null) {
                    cards.getCards().add(c1.parallelClone(c2));
                } else {
                    log("Missing parallel cards data for %s and/or %s", e.getKey(), e.getValue());
                }
            }
            cards.getCards().sort(null);
            return cards;
        } else {
            if (c.isNull() == false) {
                log("Error reading Cards array: %s", c.asText());
            }
            return null;
        }
    }

    protected void writeBoolean(BufferedWriter bw, Boolean bool) throws Exception {
        writeString(bw, bool != null ? (bool ? "1" : "0") : null);
    }

    protected void writeInteger(BufferedWriter bw, Integer number) throws Exception {
        writeString(bw, number != null ? number.toString() : null);
    }

    protected void writeString(BufferedWriter bw, String text) throws Exception {
        if (writeTab) {
            bw.write('\t');
        } else {
            writeTab = true;
        }
        if (text != null) {
            bw.write(text.replace("’", "'").replace("·", "•").replace("“", "\"").replace("”", "\"").replace("–", "-").replace("…", "..."));
        }
    }

    protected void newLine(BufferedWriter bw) throws Exception {
        bw.newLine();
        writeTab = false;
    }

    protected String getString(Row row, int index) {
        try {
            Cell c = row.getCell(index);
            if (c == null) {
                return null;
            } else if (c.getCellType() == CellType.NUMERIC) {
                return Long.toString((long) row.getCell(index).getNumericCellValue());
            } else if (c.getCellType() == CellType.STRING) {
                String s = row.getCell(index).getStringCellValue().trim();
                if (s.equals("")) {
                    return null;
                }
                s = s.trim();
                return s;
            } else {
                return null;
            }
        } catch (Exception ex) {
            return null;
        }
    }

    protected Integer getInteger(Row row, int index) {
        try {
            Cell c = row.getCell(index);
            if (c == null) {
                return null;
            } else if (c.getCellType() == CellType.NUMERIC) {
                return (int) row.getCell(index).getNumericCellValue();
            } else if (c.getCellType() == CellType.STRING) {
                return Integer.valueOf(row.getCell(index).getStringCellValue().trim());
            } else {
                return null;
            }
        } catch (Exception ex) {
            return null;
        }
    }

    protected Boolean getBoolean(Row row, int index) {
        Integer i = getInteger(row, index);
        if (i == null) {
            return null;
        } else {
            return i != 0;
        }
    }

    protected String getImageUrl(File imagesDir, String databaseId, Boolean front) throws Exception {
        if (front == null) {
            if (imagesDir.exists()) {
                File imageFile = new File(imagesDir, databaseId);
                if (imageFile.exists() == false) {
                    log("Missing image %s", databaseId);
                }
            }
            return databaseId;
        } else {
            String id = mapping.getOrDefault(databaseId, databaseId);
            String imageUrl = String.format("card_images/%s/%s%s.webp", id.substring(0, 2), id, front ? "a" : "b");
            if (imagesDir.exists()) {
                File imageFile = new File(imagesDir, imageUrl);
                if (imageFile.exists() == false) {
                    log("Missing image %s for id %s %s", imageUrl, databaseId, front ? "front" : "back");
                }
            }
            return imageUrl;
        }
    }

    protected void exportDefaultCards(File imagesDir, BufferedWriter bw, String predefinedPath) throws Exception {
        try (Workbook wb = WorkbookFactory.create(new File(predefinedPath), null, true)) {
            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }
                int idx = 0;
                String databaseId = getString(row, idx++);
                if (databaseId == null) {
                    continue;
                }
                writeString(bw, databaseId); //databaseId
                writeString(bw, getString(row, idx++)); //name
                writeString(bw, getImageUrl(imagesDir, getString(row, idx++), null)); //imageUrl
                writeString(bw, getString(row, idx++)); //cardBack
                writeString(bw, getString(row, idx++)); //type
                writeString(bw, getString(row, idx++)); //subtype
                writeString(bw, getString(row, idx++)); //packName
                writeInteger(bw, getInteger(row, idx++)); //deckbuilderQuantity
                writeString(bw, getString(row, idx++)); //setUuid
                writeInteger(bw, getInteger(row, idx++)); //numberInPack
                writeString(bw, getString(row, idx++)); //encounterSet
                writeInteger(bw, getInteger(row, idx++)); //encounterNumber
                writeBoolean(bw, getBoolean(row, idx++)); //unique
                writeBoolean(bw, getBoolean(row, idx++)); //permanent
                writeBoolean(bw, getBoolean(row, idx++)); //exceptional
                writeBoolean(bw, getBoolean(row, idx++)); //myriad
                writeString(bw, getString(row, idx++)); //faction
                writeString(bw, getString(row, idx++)); //traits
                writeString(bw, getString(row, idx++)); //side
                writeInteger(bw, getInteger(row, idx++)); //xp
                writeInteger(bw, getInteger(row, idx++)); //cost
                writeInteger(bw, getInteger(row, idx++)); //skillWillpower
                writeInteger(bw, getInteger(row, idx++)); //skillIntellect
                writeInteger(bw, getInteger(row, idx++)); //skillCombat
                writeInteger(bw, getInteger(row, idx++)); //skillAgility
                writeInteger(bw, getInteger(row, idx++)); //skillWild
                writeInteger(bw, getInteger(row, idx++)); //health
                writeBoolean(bw, getBoolean(row, idx++)); //healthPerInvestigator
                writeInteger(bw, getInteger(row, idx++)); //sanity
                writeInteger(bw, getInteger(row, idx++)); //uses
                writeInteger(bw, getInteger(row, idx++)); //enemyDamage
                writeInteger(bw, getInteger(row, idx++)); //enemyHorror
                writeInteger(bw, getInteger(row, idx++)); //enemyFight
                writeInteger(bw, getInteger(row, idx++)); //enemyEvade
                writeInteger(bw, getInteger(row, idx++)); //shroud
                writeInteger(bw, getInteger(row, idx++)); //doom
                writeInteger(bw, getInteger(row, idx++)); //clues
                writeBoolean(bw, getBoolean(row, idx++)); //cluesFixed
                writeInteger(bw, getInteger(row, idx++)); //victoryPoints
                writeInteger(bw, getInteger(row, idx++)); //vengeance
                writeInteger(bw, getInteger(row, idx++)); //stage
                writeBoolean(bw, getBoolean(row, idx++)); //action
                writeBoolean(bw, getBoolean(row, idx++)); //reaction
                writeBoolean(bw, getBoolean(row, idx++)); //free
                writeBoolean(bw, getBoolean(row, idx++)); //hasBonded
                writeString(bw, getString(row, idx++)); //text
                newLine(bw);
            }
        }
    }

    protected HashMap<String, String> readCardOverrides(File imagesDir, String overridesPath) throws Exception {
        HashMap<String, String> map = new HashMap<>();
        try (Workbook wb = WorkbookFactory.create(new File(overridesPath), null, true)) {
            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }
                int idx = 0;
                String databaseId = getString(row, idx++);
                if (databaseId == null) {
                    continue;
                }
                try (StringWriter sw = new StringWriter();
                        BufferedWriter bw = new BufferedWriter(sw)) {
                    writeString(bw, databaseId); //databaseId
                    writeString(bw, getString(row, idx++)); //name
                    writeString(bw, getImageUrl(imagesDir, getString(row, idx++), null)); //imageUrl
                    writeString(bw, getString(row, idx++)); //cardBack
                    writeString(bw, getString(row, idx++)); //type
                    writeString(bw, getString(row, idx++)); //subtype
                    writeString(bw, getString(row, idx++)); //packName
                    writeInteger(bw, getInteger(row, idx++)); //deckbuilderQuantity
                    writeString(bw, getString(row, idx++)); //setUuid
                    writeInteger(bw, getInteger(row, idx++)); //numberInPack
                    writeString(bw, getString(row, idx++)); //encounterSet
                    writeInteger(bw, getInteger(row, idx++)); //encounterNumber
                    writeBoolean(bw, getBoolean(row, idx++)); //unique
                    writeBoolean(bw, getBoolean(row, idx++)); //permanent
                    writeBoolean(bw, getBoolean(row, idx++)); //exceptional
                    writeBoolean(bw, getBoolean(row, idx++)); //myriad
                    writeString(bw, getString(row, idx++)); //faction
                    writeString(bw, getString(row, idx++)); //traits
                    String side = getString(row, idx++);
                    writeString(bw, side); //side
                    writeInteger(bw, getInteger(row, idx++)); //xp
                    writeInteger(bw, getInteger(row, idx++)); //cost
                    writeInteger(bw, getInteger(row, idx++)); //skillWillpower
                    writeInteger(bw, getInteger(row, idx++)); //skillIntellect
                    writeInteger(bw, getInteger(row, idx++)); //skillCombat
                    writeInteger(bw, getInteger(row, idx++)); //skillAgility
                    writeInteger(bw, getInteger(row, idx++)); //skillWild
                    writeInteger(bw, getInteger(row, idx++)); //health
                    writeBoolean(bw, getBoolean(row, idx++)); //healthPerInvestigator
                    writeInteger(bw, getInteger(row, idx++)); //sanity
                    writeInteger(bw, getInteger(row, idx++)); //uses
                    writeInteger(bw, getInteger(row, idx++)); //enemyDamage
                    writeInteger(bw, getInteger(row, idx++)); //enemyHorror
                    writeInteger(bw, getInteger(row, idx++)); //enemyFight
                    writeInteger(bw, getInteger(row, idx++)); //enemyEvade
                    writeInteger(bw, getInteger(row, idx++)); //shroud
                    writeInteger(bw, getInteger(row, idx++)); //doom
                    writeInteger(bw, getInteger(row, idx++)); //clues
                    writeBoolean(bw, getBoolean(row, idx++)); //cluesFixed
                    writeInteger(bw, getInteger(row, idx++)); //victoryPoints
                    writeInteger(bw, getInteger(row, idx++)); //vengeance
                    writeInteger(bw, getInteger(row, idx++)); //stage
                    writeBoolean(bw, getBoolean(row, idx++)); //action
                    writeBoolean(bw, getBoolean(row, idx++)); //reaction
                    writeBoolean(bw, getBoolean(row, idx++)); //free
                    writeBoolean(bw, getBoolean(row, idx++)); //hasBonded
                    writeString(bw, getString(row, idx++)); //text
                    newLine(bw);
                    bw.flush();
                    map.put(String.format("%s_%s", databaseId, side != null && side.length() > 0 ? side : "A"), sw.toString());
                }
            }
        }
        return map;
    }

    protected void exportFrontSide(File imagesDir, BufferedWriter bw, Card c, boolean doubleSided, boolean linked, HashMap<String, String> overrides) throws Exception {
        String key = String.format("%s_%s", c.getCode(), "A");
        if (overrides.containsKey(key)) {
            bw.write(overrides.get(key));
            return;
        }
        String cardBack = doubleSided || linked ? "multi_sided" : c.getDefaultCardBack(backOverrides, backOverridesVerified);
        writeString(bw, c.getCode()); //databaseId
        writeString(bw, titles.getOrDefault(String.format("%s_A", c.getCode()), c.getFullName(true))); //name
        writeString(bw, getImageUrl(imagesDir, c.getImageCode(true), true)); //imageUrl
        writeString(bw, cardBack); //cardBack
        writeString(bw, types.getOrDefault(String.format("%s_%s", c.getCode(), c.getTypeName()), c.getTypeName())); //type
        writeString(bw, weaknesses.getOrDefault(c.getCode(), c.getSubtypeName())); //subtype
        writeString(bw, c.getPackName()); //packName
        writeInteger(bw, c.getDeckbuilderQuantity(quantities, cardBack)); //deckbuilderQuantity
        writeString(bw, c.getPackCode()); //setUuid
        writeInteger(bw, c.getPosition(true)); //numberInPack
        writeString(bw, c.getEncounterName()); //encounterSet
        writeInteger(bw, c.getEncounterPosition()); //encounterNumber
        writeBoolean(bw, c.getUnique()); //unique
        writeBoolean(bw, c.getPermanent()); //permanent
        writeBoolean(bw, c.getExceptional()); //exceptional
        writeBoolean(bw, c.getMyriad()); //myriad
        writeString(bw, c.getFactions()); //faction
        writeString(bw, c.getTraits()); //traits
        writeString(bw, doubleSided || linked ? "A" : null); //side
        writeInteger(bw, c.getXp()); //xp
        writeInteger(bw, c.getCost()); //cost
        writeInteger(bw, c.getSkillWillpower()); //skillWillpower
        writeInteger(bw, c.getSkillIntellect()); //skillIntellect
        writeInteger(bw, c.getSkillCombat()); //skillCombat
        writeInteger(bw, c.getSkillAgility()); //skillAgility
        writeInteger(bw, c.getSkillWild()); //skillWild
        writeInteger(bw, c.getHealth()); //health
        writeBoolean(bw, c.getHealthPerInvestigator()); //healthPerInvestigator
        writeInteger(bw, c.getSanity()); //sanity
        writeInteger(bw, c.getUses()); //uses
        writeInteger(bw, c.getEnemyDamage()); //enemyDamage
        writeInteger(bw, c.getEnemyHorror()); //enemyHorror
        writeInteger(bw, c.getEnemyFight()); //enemyFight
        writeInteger(bw, c.getEnemyEvade()); //enemyEvade
        writeInteger(bw, c.getShroud()); //shroud
        writeInteger(bw, c.getDoom()); //doom
        writeInteger(bw, c.getClues()); //clues
        writeBoolean(bw, c.getCluesFixed()); //cluesFixed
        writeInteger(bw, c.getVictory()); //victoryPoints
        writeInteger(bw, c.getVengeance()); //vengeance
        writeInteger(bw, c.getStage()); //stage
        writeBoolean(bw, c.getText() != null && c.getText().contains("[action]")); //action
        writeBoolean(bw, c.getText() != null && c.getText().contains("[reaction]")); //reaction
        writeBoolean(bw, c.getText() != null && c.getText().contains("[free]")); //free
        writeBoolean(bw, bondedCards.containsKey(c.getName())); //hasBonded
        writeString(bw, c.getText()); //text
        newLine(bw);
    }

    protected void exportBackSide(File imagesDir, BufferedWriter bw, Card c, HashMap<String, String> overrides) throws Exception {
        String key = String.format("%s_%s", c.getCode(), "B");
        if (overrides.containsKey(key)) {
            bw.write(overrides.get(key));
            return;
        }
        writeString(bw, c.getCode()); //databaseId
        writeString(bw, titles.getOrDefault(String.format("%s_B", c.getCode()), c.getBackName() != null ? c.getBackName() : c.getFullName("Location".equals(c.getTypeName()) == false))); //name
        writeString(bw, getImageUrl(imagesDir, c.getImageCode(false), false)); //imageUrl
        writeString(bw, "multi_sided"); //cardBack
        writeString(bw, types.getOrDefault(String.format("%s_%s", c.getCode(), c.getTypeName()), c.getTypeName())); //type
        writeString(bw, weaknesses.getOrDefault(c.getCode(), c.getSubtypeName())); //subtype
        writeString(bw, c.getPackName()); //packName
        writeInteger(bw, c.getDeckbuilderQuantity(quantities, "multi_sided")); //deckbuilderQuantity
        writeString(bw, c.getPackCode()); //setUuid
        writeInteger(bw, c.getPosition(false)); //numberInPack
        writeString(bw, c.getEncounterName()); //encounterSet
        writeInteger(bw, c.getEncounterPosition()); //encounterNumber
        writeBoolean(bw, c.getUnique()); //unique
        writeBoolean(bw, c.getPermanent()); //permanent
        writeBoolean(bw, c.getExceptional()); //exceptional
        writeBoolean(bw, c.getMyriad()); //myriad
        writeString(bw, c.getFactions()); //faction
        writeString(bw, "Location".equals(c.getTypeName()) ? c.getTraits() : null); //traits
        writeString(bw, "B"); //side
        writeInteger(bw, null); //xp
        writeInteger(bw, null); //cost
        writeInteger(bw, null); //skillWillpower
        writeInteger(bw, null); //skillIntellect
        writeInteger(bw, null); //skillCombat
        writeInteger(bw, null); //skillAgility
        writeInteger(bw, null); //skillWild
        writeInteger(bw, null); //health
        writeBoolean(bw, null); //healthPerInvestigator
        writeInteger(bw, null); //sanity
        writeInteger(bw, null); //uses
        writeInteger(bw, null); //enemyDamage
        writeInteger(bw, null); //enemyHorror
        writeInteger(bw, null); //enemyFight
        writeInteger(bw, null); //enemyEvade
        writeInteger(bw, null); //shroud
        writeInteger(bw, null); //doom
        writeInteger(bw, null); //clues
        writeBoolean(bw, null); //cluesFixed
        writeInteger(bw, null); //victoryPoints
        writeInteger(bw, null); //vengeance
        writeInteger(bw, null); //stage
        writeBoolean(bw, c.getBackText() != null && c.getBackText().contains("[action]")); //action
        writeBoolean(bw, c.getBackText() != null && c.getBackText().contains("[reaction]")); //reaction
        writeBoolean(bw, c.getBackText() != null && c.getBackText().contains("[free]")); //free
        writeBoolean(bw, false); //hasBonded
        writeString(bw, c.getBackText()); //text
        newLine(bw);
    }

    protected void exportLinked(File imagesDir, BufferedWriter bw, Card c, Card cc, HashMap<String, String> overrides) throws Exception {
        String key = String.format("%s_%s", c.getCode(), "B");
        if (overrides.containsKey(key)) {
            bw.write(overrides.get(key));
            return;
        }
        writeString(bw, c.getCode()); //databaseId: multi_sided must share
        writeString(bw, titles.getOrDefault(String.format("%s_B", c.getCode()), cc.getFullName(true))); //name
        writeString(bw, getImageUrl(imagesDir, c.getImageCode(false), false)); //imageUrl
        writeString(bw, "multi_sided"); //cardBack
        writeString(bw, types.getOrDefault(String.format("%s_%s", c.getCode(), cc.getTypeName()), cc.getTypeName())); //type
        writeString(bw, weaknesses.getOrDefault(cc.getCode(), cc.getSubtypeName())); //subtype
        writeString(bw, cc.getPackName()); //packName
        writeInteger(bw, cc.getDeckbuilderQuantity(quantities, "multi_sided")); //deckbuilderQuantity
        writeString(bw, cc.getPackCode()); //setUuid
        writeInteger(bw, cc.getPosition(false)); //numberInPack
        writeString(bw, cc.getEncounterName()); //encounterSet
        writeInteger(bw, cc.getEncounterPosition()); //encounterNumber
        writeBoolean(bw, cc.getUnique()); //unique
        writeBoolean(bw, cc.getPermanent()); //permanent
        writeBoolean(bw, cc.getExceptional()); //exceptional
        writeBoolean(bw, cc.getMyriad()); //myriad
        writeString(bw, cc.getFactions()); //faction
        writeString(bw, cc.getTraits()); //traits
        writeString(bw, "B"); //side
        writeInteger(bw, cc.getXp()); //xp
        writeInteger(bw, cc.getCost()); //cost
        writeInteger(bw, cc.getSkillWillpower()); //skillWillpower
        writeInteger(bw, cc.getSkillIntellect()); //skillIntellect
        writeInteger(bw, cc.getSkillCombat()); //skillCombat
        writeInteger(bw, cc.getSkillAgility()); //skillAgility
        writeInteger(bw, cc.getSkillWild()); //skillWild
        writeInteger(bw, cc.getHealth()); //health
        writeBoolean(bw, cc.getHealthPerInvestigator()); //healthPerInvestigator
        writeInteger(bw, cc.getSanity()); //sanity
        writeInteger(bw, cc.getUses()); //uses
        writeInteger(bw, cc.getEnemyDamage()); //enemyDamage
        writeInteger(bw, cc.getEnemyHorror()); //enemyHorror
        writeInteger(bw, cc.getEnemyFight()); //enemyFight
        writeInteger(bw, cc.getEnemyEvade()); //enemyEvade
        writeInteger(bw, cc.getShroud()); //shroud
        writeInteger(bw, cc.getDoom()); //doom
        writeInteger(bw, cc.getClues()); //clues
        writeBoolean(bw, cc.getCluesFixed()); //cluesFixed
        writeInteger(bw, cc.getVictory()); //victoryPoints
        writeInteger(bw, cc.getVengeance()); //vengeance
        writeInteger(bw, cc.getStage()); //stage
        writeBoolean(bw, cc.getText() != null && cc.getText().contains("[action]")); //action
        writeBoolean(bw, cc.getText() != null && cc.getText().contains("[reaction]")); //reaction
        writeBoolean(bw, cc.getText() != null && cc.getText().contains("[free]")); //free
        writeBoolean(bw, false); //hasBonded
        writeString(bw, cc.getText()); //text
        newLine(bw);
    }

    protected boolean filter(Card c) {
        if (c.getHidden() != null && c.getHidden() && unhidden.contains(c.getCode()) == false) {
            return false;
        }
        if (c.getPackCode() == null) {
            return false;
        }
        switch (c.getPackCode()) {
            //Core Set
            case "core":
                return true;
            //Revised Core Set (only player cards)
            case "rcore":
                return c.getPosition() <= 103 || c.getPosition() >= 183;
            //The Dunwich Legacy
            case "dwl":
            case "tmm":
            case "tece":
            case "bota":
            case "uau":
            case "wda":
            case "litas":
                return true;
            //The Path to Carcosa
            case "ptc":
            case "eotp":
            case "tuo":
            case "apot":
            case "tpm":
            case "bsr":
            case "dca":
                return true;
            //The Forgotten Age
            case "tfa":
            case "tof":
            case "tbb":
            case "hote":
            case "tcoa":
            case "tdoy":
            case "sha":
                return true;
            //The Circle Undone
            case "tcu":
            case "tsn":
            case "wos":
            case "fgg":
            case "uad":
            case "icc":
            case "bbt":
                return true;
            //The Dream-Eaters
            case "tde":
            case "sfk":
            case "tsh":
            case "dsm":
            case "pnr":
            case "wgd":
            case "woc":
                return true;
            //The Innsmouth Conspiracy
            case "tic":
            case "itd":
            case "def":
            case "hhg":
            case "lif":
            case "lod":
            case "itm":
                return true;
            //Edge of the Earth
            case "eoep":
                return true;
            //The Scarlet Keys
            case "tskp":
                return true;
            //The Feast of Hemlock Vale
            case "fhvp":
                return true;
            //Return to...
            case "rtnotz": //Return to the Night of the Zealot
            case "rtdwl": //Return to the Dunwich Legacy
            case "rtptc": //Return to the Path to Carcosa
            case "rttfa": //Return to the Forgotten Age
            case "rttcu": //Return to the Curcle Undone
                return true;
            //Investigator Starter Decks
            case "nat":
            case "har":
            case "win":
            case "jac":
            case "ste":
                return true;
            //Side Stories
            case "cotr": //Curse of the Rougarou
                return true;
            //Promotional
            case "books":
            case "hoth":
            case "tdor":
            case "iotv":
            case "tdg":
            case "tftbw":
            case "bob":
            case "dre":
            case "promo":
                return true;
            //Parallel
            case "rod":
            case "aon":
            case "bad":
            case "btb":
            case "rtr":
            case "otr":
            case "ltr":
            case "ptr":
            case "rop":
            case "hfa":
            case "pap":
            case "aof":
                return true;
            default:
                return false;
        }
    }

    protected void exportCards(Cards cards, String predefinedPath, String overridesPath, String path, String imagesPath) throws Exception {
        File file = new File(path);
        File imagesDir = new File(imagesPath);
        if (cards != null && cards.getCards() != null && cards.getCards().isEmpty() == false) {
            try (FileOutputStream fos = new FileOutputStream(file, false);
                    OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                    BufferedWriter bw = new BufferedWriter(osw)) {
                writeString(bw, "databaseId");
                writeString(bw, "name");
                writeString(bw, "imageUrl");
                writeString(bw, "cardBack");
                writeString(bw, "type");
                writeString(bw, "subtype");
                writeString(bw, "packName");
                writeString(bw, "deckbuilderQuantity");
                writeString(bw, "setUuid");
                writeString(bw, "numberInPack");
                writeString(bw, "encounterSet");
                writeString(bw, "encounterNumber");
                writeString(bw, "unique");
                writeString(bw, "permanent");
                writeString(bw, "exceptional");
                writeString(bw, "myriad");
                writeString(bw, "faction");
                writeString(bw, "traits");
                writeString(bw, "side");
                writeString(bw, "xp");
                writeString(bw, "cost");
                writeString(bw, "skillWillpower");
                writeString(bw, "skillIntellect");
                writeString(bw, "skillCombat");
                writeString(bw, "skillAgility");
                writeString(bw, "skillWild");
                writeString(bw, "health");
                writeString(bw, "healthPerInvestigator");
                writeString(bw, "sanity");
                writeString(bw, "uses");
                writeString(bw, "enemyDamage");
                writeString(bw, "enemyHorror");
                writeString(bw, "enemyFight");
                writeString(bw, "enemyEvade");
                writeString(bw, "shroud");
                writeString(bw, "doom");
                writeString(bw, "clues");
                writeString(bw, "cluesFixed");
                writeString(bw, "victoryPoints");
                writeString(bw, "vengeance");
                writeString(bw, "stage");
                writeString(bw, "action");
                writeString(bw, "reaction");
                writeString(bw, "free");
                writeString(bw, "hasBonded");
                writeString(bw, "text");
                newLine(bw);
                exportDefaultCards(imagesDir, bw, predefinedPath);
                HashMap<String, String> overrides = readCardOverrides(imagesDir, overridesPath);
                for (Card c : cards) {
                    if (filter(c) == false) {
                        continue;
                    }
                    if (flipped.contains(c.getCode())) {
                        Card cc = c.getLinkedCard();
                        String ccCode = cc.getCode();
                        cc.setCode(c.getCode());
                        c.setCode(ccCode);
                        exportFrontSide(imagesDir, bw, cc, false, true, overrides);
                        exportLinked(imagesDir, bw, cc, c, overrides);
                    } else {
                        boolean doubleSided = c.getDoubleSided() != null && c.getDoubleSided() || forceDoublesided.contains(c.getCode());
                        boolean linked = c.getLinkedCard() != null;
                        if (doubleSided && linked && ignoreDoublesided.contains(c.getCode()) == false) {
                            log("Double-sided and linked for %s", c.getCode());
                        }
                        exportFrontSide(imagesDir, bw, c, doubleSided, linked, overrides);
                        if (doubleSided && ignoreDoublesided.contains(c.getCode()) == false) {
                            exportBackSide(imagesDir, bw, c, overrides);
                        } else if (linked) {
                            Card cc = c.getLinkedCard();
                            exportLinked(imagesDir, bw, c, cc, overrides);
                        }
                    }
                }
                bw.flush();
            }
        }
    }

    protected void line(BufferedWriter bw, String s) throws Exception {
        bw.write(s);
        bw.newLine();
    }

    protected boolean hasTrait(Card c, String[] traits) {
        if (c.getTraits() == null) {
            return false;
        }
        for (String trait : traits) {
            if (c.getTraits().contains(trait)) {
                return true;
            }
        }
        return false;
    }

    protected void fillWeaknessMap(LinkedHashMap<String, ArrayList<String>> map, Card c, Integer qty, String... traits) {
        ArrayList<String> list;
        if (traits == null || traits.length == 0 || hasTrait(c, traits)) {
            list = map.get(c.getPackCode());
            if (list == null) {
                list = new ArrayList<>();
                map.put(c.getPackCode(), list);
            }
            for (int i = 0; i < qty; i++) {
                list.add(c.getCode());
            }
        }
    }

    protected void generateWeaknessFunction(BufferedWriter bw, String name, LinkedHashMap<String, ArrayList<String>> map, boolean last) throws Exception {
        line(bw, String.format("        \"%s\": {", name));
        line(bw, "            \"args\": [\"$SET_UUID\"],");
        line(bw, "            \"code\": [");
        line(bw, String.format("                [\"VALIDATE_NOT_EMPTY\", \"$SET_UUID\", \"%s.SET_UUID\"],", name));
        line(bw, "                [\"COND\",");
        for (Map.Entry<String, ArrayList<String>> e : map.entrySet()) {
            line(bw, String.format("                    [\"EQUAL\", \"$SET_UUID\", \"%s\"],", e.getKey()));
            line(bw, String.format("                    [\"LIST\", \"%s\"],", StringUtils.join(e.getValue(), "\", \"")));
        }
        line(bw, "                    [\"TRUE\"],");
        line(bw, "                    [\"LIST\"]");
        line(bw, "                ]");
        line(bw, "            ]");
        line(bw, String.format("        }%s", last ? "" : ","));
    }

    protected void exportWeaknesses(Cards cards, String path) throws Exception {
        File file = new File(path);
        if (file.exists() == false) {
            file = new File("run/Core Weakness.json");
        }
        if (cards != null && cards.getCards() != null && cards.getCards().isEmpty() == false) {
            try (FileOutputStream fos = new FileOutputStream(file, false);
                    OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                    BufferedWriter bw = new BufferedWriter(osw)) {
                LinkedHashMap<String, ArrayList<String>> map = new LinkedHashMap<>();
                LinkedHashMap<String, ArrayList<String>> mapMadnessInjuryPact = new LinkedHashMap<>();
                LinkedHashMap<String, ArrayList<String>> mapMadnessPactCultistDetective = new LinkedHashMap<>();
                for (Card c : cards) {
                    if (filter(c) == false) { //skip cards outside core set for now
                        continue;
                    }
                    if ("Basic Weakness".equals(weaknesses.getOrDefault(c.getCode(), c.getSubtypeName()))) {
                        Integer qty = c.getDeckbuilderQuantity(quantities, "Player Card");
                        if (qty == null) {
                            qty = 1;
                        }
                        if ("core".equals(c.getPackCode()) || "rcore".equals(c.getPackCode())) {
                            qty *= 2;
                        }
                        fillWeaknessMap(map, c, qty);
                        fillWeaknessMap(mapMadnessInjuryPact, c, qty, "Madness.", "Injury.", "Pact.");
                        fillWeaknessMap(mapMadnessPactCultistDetective, c, qty, "Madness.", "Pact.", "Cultist.", "Detective.");
                    }
                }
                line(bw, "{");
                line(bw, "    \"functions\": {");
                generateWeaknessFunction(bw, "GET_LIST_OF_MADNESS_PACT_CULTIST_DETECTIVE_WEAKNESSES", mapMadnessPactCultistDetective, false);
                generateWeaknessFunction(bw, "GET_LIST_OF_MADNESS_INJURY_PACT_WEAKNESSES", mapMadnessInjuryPact, false);
                generateWeaknessFunction(bw, "GET_LIST_OF_WEAKNESSES", map, true);
                line(bw, "    }");
                line(bw, "}");
                bw.flush();
            }
        }
    }

    protected void exportBonded(Cards cards, String path) throws Exception {
        File file = new File(path);
        if (file.exists() == false) {
            file = new File("run/Core Bonded.json");
        }
        if (cards != null && cards.getCards() != null && cards.getCards().isEmpty() == false) {
            try (FileOutputStream fos = new FileOutputStream(file, false);
                    OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                    BufferedWriter bw = new BufferedWriter(osw)) {
                ArrayList<Card> cardsWithBonded = new ArrayList<>();
                for (Card c : cards) {
                    if (filter(c) == false) { //skip cards outside core set for now
                        continue;
                    }
                    if (bondedCards.containsKey(c.getName()) && skipBonded.contains(c.getCode()) == false) {
                        cardsWithBonded.add(c);
                    }
                }
                line(bw, "{");
                line(bw, "    \"functions\": {");
                line(bw, "        \"DO_SPAWN_BONDED\": {");
                line(bw, "            \"args\": [\"$DATABASE_ID\", \"$TARGET_PLAYER\"],");
                line(bw, "            \"code\": [");
                line(bw, "                [\"VALIDATE_NOT_EMPTY\", \"$DATABASE_ID\", \"DO_SPAWN_BONDED.DATABASE_ID\"],");
                line(bw, "                [\"VALIDATE_PLAYER\", \"$TARGET_PLAYER\", \"DO_SPAWN_BONDED.TARGET_PLAYER\"],");
                line(bw, "                [\"VAR\", \"$TARGET_PLAYER_ASIDE\", [\"GET_CONTROLLER_ASIDE\", \"$TARGET_PLAYER\"]],");
                line(bw, "                [\"COND\",");
                for (Card c : cardsWithBonded) {
                    line(bw, String.format("                    [\"EQUAL\", \"$DATABASE_ID\", \"%s\"],", c.getCode()));
                    ArrayList<Card> bcs = bondedCards.get(c.getName());
                    if (c.getCode().equals("10015")) {
                        line(bw, "                    [\"DO_CREATE_MISSING_CARDS\", \"$TARGET_PLAYER\", \"Hank Samson\", \"10016\", 1, \"$TARGET_PLAYER_ASIDE\", true, null],");
                    } else if (bcs.size() == 1) {
                        Card bc = bcs.get(0);
                        line(bw, String.format("                    [\"%s\", \"$TARGET_PLAYER\", \"%s\", \"%s\", %d, \"$TARGET_PLAYER_ASIDE\", true, null],", bondedForEach.contains(bc.getCode()) ? "DO_CREATE_CARDS" : "DO_CREATE_MISSING_CARDS", bc.getName().replace("\"", "\\\""), bc.getCode(), bc.getBondedCount()));
                    } else {
                        line(bw, "                    [");
                        int size = bcs.size();
                        for (int i = 0; i < size; i++) {
                            Card bc = bcs.get(i);
                            line(bw, String.format("                        [\"%s\", \"$TARGET_PLAYER\", \"%s\", \"%s\", %d, \"$TARGET_PLAYER_ASIDE\", true, null]%s", bondedForEach.contains(bc.getCode()) ? "DO_CREATE_CARDS" : "DO_CREATE_MISSING_CARDS", bc.getName().replace("\"", "\\\""), bc.getCode(), bc.getBondedCount(), i + 1 == size ? "" : ","));
                        }
                        line(bw, "                    ],");
                    }
                }
                line(bw, "                    [\"TRUE\"],");
                line(bw, "                    \"$GAME\"");
                line(bw, "                ]");
                line(bw, "            ]");
                line(bw, "        }");
                line(bw, "    }");
                line(bw, "}");
                bw.flush();
            }
        }
    }

    protected void exportMini(Cards cards, String path) throws Exception {
        File file = new File(path);
        if (file.exists() == false) {
            file = new File("run/Core Mini.json");
        }
        if (cards != null && cards.getCards() != null && cards.getCards().isEmpty() == false) {
            try (FileOutputStream fos = new FileOutputStream(file, false);
                    OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                    BufferedWriter bw = new BufferedWriter(osw)) {
                line(bw, "{");
                line(bw, "    \"functions\": {");
                line(bw, "        \"GET_MINI_ID\": {");
                line(bw, "            \"args\": [\"$PREFIX\", \"$DATABASE_ID\"],");
                line(bw, "            \"code\": [");
                line(bw, "                [\"VALIDATE_NOT_EMPTY\", \"$PREFIX\", \"GET_MINI_ID.$PREFIX\"],");
                line(bw, "                [\"VALIDATE_NOT_EMPTY\", \"$DATABASE_ID\", \"GET_MINI_ID.DATABASE_ID\"],");
                line(bw, "                [\"COND\",");
                for (Map.Entry<String, String> e : parallelMini.entrySet()) {
                    line(bw, String.format("                    [\"EQUAL\", \"$DATABASE_ID\", \"%s\"],", e.getKey()));
                    line(bw, String.format("                    \"{{$PREFIX}}%s\",", e.getValue()));
                }
                line(bw, "                    [\"TRUE\"],");
                line(bw, "                    \"{{$PREFIX}}{{$DATABASE_ID}}\"");
                line(bw, "                ]");
                line(bw, "            ]");
                line(bw, "        }");
                line(bw, "    }");
                line(bw, "}");
                bw.flush();
            }
        }
    }

    protected void exportRavenQuill(Cards cards, String path) throws Exception {
        File file = new File(path);
        if (file.exists() == false) {
            file = new File("run/raven_quill.tsv");
        }
        if (cards != null && cards.getCards() != null && cards.getCards().isEmpty() == false) {
            try (FileOutputStream fos = new FileOutputStream(file, false);
                    OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                    BufferedWriter bw = new BufferedWriter(osw)) {
                LinkedHashMap<String, String> names = new LinkedHashMap<>();
                for (Card c : cards) {
                    if (c.getTypeName() != null && c.getTypeName().equals("Asset") && c.getTraits() != null && (c.getTraits().contains("Tome.") || c.getTraits().contains("Spell."))) {
                        names.put(c.getCode(), c.getName());
                    }
                }
                for (var e : names.entrySet()) {
                    line(bw, String.format("%s\t%s", e.getKey(), e.getValue()));
                }
                bw.flush();
            }
        }
    }

    protected void exportTraits(Cards cards, String path) throws Exception {
        File file = new File(path);
        if (file.exists() == false) {
            file = new File("run/traits.tsv");
        }
        if (cards != null && cards.getCards() != null && cards.getCards().isEmpty() == false) {
            try (FileOutputStream fos = new FileOutputStream(file, false);
                    OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                    BufferedWriter bw = new BufferedWriter(osw)) {
                TreeMap<String, String> names = new TreeMap<>();
                for (Card c : cards) {
                    if (c.getTraits() != null) {
                        String[] traits = c.getTraits().split("\\.");
                        for (String trait : traits) {
                            String t = trait.trim();
                            if (t.length() > 0) {
                                names.put(t.replace(" ", "").replace("'", "").replace("-", "").replace("?", "Q"), t);
                            }
                        }
                    }
                }
                for (var e : names.entrySet()) {
                    line(bw, String.format("%s\t%s", e.getKey(), e.getValue()));
                }
                bw.flush();
            }
        }
    }

    protected void testImages(Cards cards, String language, String imagesPath) throws Exception {
        File imagesDir = new File(imagesPath);
        File languageDir = new File(imagesDir, language);
        if (languageDir.exists() == false) {
            log("Missing language %s", language);
            return;
        }
        String base = imagesDir.getAbsoluteFile().getCanonicalPath();
        HashSet<String> sourceFiles = new HashSet<>();
        Collection<File> originalFiles = FileUtils.listFiles(imagesDir, new String[]{"webp"}, true);
        for (File f : originalFiles) {
            String relativePath = f.getAbsoluteFile().getCanonicalPath().replace(base, "");
            boolean ignore = false;
            for (String ip : ignorePaths) {
                if (relativePath.startsWith(ip)) {
                    ignore = true;
                    break;
                }
            }
            if (ignore == false) {
                sourceFiles.add(relativePath);
            }
        }
        base = languageDir.getAbsoluteFile().getCanonicalPath();
        Collection<File> languageFiles = FileUtils.listFiles(languageDir, new String[]{"webp"}, true);
        ArrayList<String> unwanted = new ArrayList<>();
        for (File f : languageFiles) {
            String relativePath = f.getAbsoluteFile().getCanonicalPath().replace(base, "");
            if (sourceFiles.remove(relativePath) == false) {
                unwanted.add(relativePath);
            }
        }
        ArrayList<String> missing = new ArrayList<>(sourceFiles);
        if (missing.isEmpty() == false) {
            log("Missing %s files:", language);
            missing.sort(null);
            for (String s : missing) {
                log("%s%s", language, s);
            }
        }
        if (unwanted.isEmpty() == false) {
            log("Unwanted %s files:", language);
            unwanted.sort(null);
            for (String s : unwanted) {
                log("%s%s", language, s);
            }
        }
    }

    public void run() throws Exception {
        Util.downloadIfOld("https://arkhamdb.com/api/public/cards/?encounter=1", "run/cards.json");
        Cards cards = loadCards("run/cards.json");
        exportCards(cards, "run/predefined.xlsx", "run/overrides.xlsx", "run/arkhamhorrorlcg.tsv", "../../cards/arkham/dragncards-arkhamhorrorlcg-plugin/images");
        exportWeaknesses(cards, "../../cards/arkham/dragncards-arkhamhorrorlcg-plugin/jsons/Core Weakness.json");
        exportBonded(cards, "../../cards/arkham/dragncards-arkhamhorrorlcg-plugin/jsons/Core Bonded.json");
        exportMini(cards, "../../cards/arkham/dragncards-arkhamhorrorlcg-plugin/jsons/Core Mini.json");
        exportRavenQuill(cards, "../../cards/arkham/dragncards-arkhamhorrorlcg-php/raven_quill.tsv");
        exportTraits(cards, "../../cards/arkham/dragncards-arkhamhorrorlcg-php/traits.tsv");
        //testImages(cards, "es", "../../cards/arkham/dragncards-arkhamhorrorlcg-plugin/images");
    }

    public static void main(String[] args) {
        try {
            new MainExportArkhamDB().run();
        } catch (Exception ex) {
            log(ex);
        }
    }

}
