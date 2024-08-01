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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jsoup.Jsoup;
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

    protected boolean writeTab;

    public MainExportArkhamDB() throws Exception {
        mapping = Util.readConfigMap("run/mapping.txt");
        titles = Util.readConfigMap("run/titles.txt");
        types = Util.readConfigMap("run/types.txt");
        weaknesses = Util.readConfigMap("run/weaknesses.txt");
        backOverrides = Util.readConfigMap("run/backOverrides.txt");
        flipped = Util.readConfigSet("run/flipped.txt");
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
                cards.getCards().add(readCard(c.get(i)));
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

    protected void writeHTML(BufferedWriter bw, String text) throws Exception {
        if (text != null) {
            text = Jsoup.parse(text).text().replace("[[", "").replace("]]", "");
        }
        writeString(bw, text);
    }

    protected void writeString(BufferedWriter bw, String text) throws Exception {
        if (writeTab) {
            bw.write('\t');
        } else {
            writeTab = true;
        }
        if (text != null) {
            bw.write(text);
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
        try (Workbook wb = WorkbookFactory.create(new File(predefinedPath))) {
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

    protected void exportFrontSide(File imagesDir, BufferedWriter bw, Card c, boolean doubleSided, boolean linked) throws Exception {
        writeString(bw, c.getCode()); //databaseId
        writeString(bw, titles.getOrDefault(String.format("%s_A", c.getCode()), c.getFullName(true))); //name
        writeString(bw, getImageUrl(imagesDir, c.getCode(), true)); //imageUrl
        writeString(bw, doubleSided || linked ? "multi_sided" : c.getDefaultCardBack(backOverrides)); //cardBack
        writeString(bw, types.getOrDefault(String.format("%s_%s", c.getCode(), c.getTypeName()), c.getTypeName())); //type
        writeString(bw, weaknesses.getOrDefault(c.getCode(), c.getSubtypeName())); //subtype
        writeString(bw, c.getPackName()); //packName
        writeInteger(bw, c.getDeckbuilderQuantity()); //deckbuilderQuantity
        writeString(bw, c.getPackCode()); //setUuid
        writeInteger(bw, c.getPosition()); //numberInPack
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
        writeInteger(bw, c.getStage()); //stage
        writeBoolean(bw, c.getText() != null && c.getText().contains("[action]")); //action
        writeBoolean(bw, c.getText() != null && c.getText().contains("[reaction]")); //reaction
        writeBoolean(bw, c.getText() != null && c.getText().contains("[free]")); //free
        writeBoolean(bw, bondedCards.containsKey(c.getName())); //hasBonded
        writeString(bw, c.getText()); //text
        newLine(bw);
    }

    protected void exportBackSide(File imagesDir, BufferedWriter bw, Card c) throws Exception {
        writeString(bw, c.getCode()); //databaseId
        writeString(bw, titles.getOrDefault(String.format("%s_B", c.getCode()), c.getBackName() != null ? c.getBackName() : c.getFullName("Location".equals(c.getTypeName()) == false))); //name
        writeString(bw, getImageUrl(imagesDir, c.getCode(), false)); //imageUrl
        writeString(bw, "multi_sided"); //cardBack
        writeString(bw, types.getOrDefault(String.format("%s_%s", c.getCode(), c.getTypeName()), c.getTypeName())); //type
        writeString(bw, weaknesses.getOrDefault(c.getCode(), c.getSubtypeName())); //subtype
        writeString(bw, c.getPackName()); //packName
        writeInteger(bw, c.getDeckbuilderQuantity()); //deckbuilderQuantity
        writeString(bw, c.getPackCode()); //setUuid
        writeInteger(bw, c.getPosition()); //numberInPack
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
        writeInteger(bw, null); //stage
        writeBoolean(bw, c.getBackText() != null && c.getBackText().contains("[action]")); //action
        writeBoolean(bw, c.getBackText() != null && c.getBackText().contains("[reaction]")); //reaction
        writeBoolean(bw, c.getBackText() != null && c.getBackText().contains("[free]")); //free
        writeBoolean(bw, false); //hasBonded
        writeString(bw, c.getBackText()); //text
        newLine(bw);
    }

    protected void exportLinked(File imagesDir, BufferedWriter bw, Card c, Card cc) throws Exception {
        writeString(bw, c.getCode()); //databaseId: multi_sided must share
        writeString(bw, titles.getOrDefault(String.format("%s_B", c.getCode()), cc.getFullName(true))); //name
        writeString(bw, getImageUrl(imagesDir, c.getCode(), false)); //imageUrl
        writeString(bw, "multi_sided"); //cardBack
        writeString(bw, types.getOrDefault(String.format("%s_%s", c.getCode(), cc.getTypeName()), cc.getTypeName())); //type
        writeString(bw, weaknesses.getOrDefault(cc.getCode(), cc.getSubtypeName())); //subtype
        writeString(bw, cc.getPackName()); //packName
        writeInteger(bw, cc.getDeckbuilderQuantity()); //deckbuilderQuantity
        writeString(bw, cc.getPackCode()); //setUuid
        writeInteger(bw, cc.getPosition()); //numberInPack
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
        writeInteger(bw, cc.getStage()); //stage
        writeBoolean(bw, cc.getText() != null && cc.getText().contains("[action]")); //action
        writeBoolean(bw, cc.getText() != null && cc.getText().contains("[reaction]")); //reaction
        writeBoolean(bw, cc.getText() != null && cc.getText().contains("[free]")); //free
        writeBoolean(bw, false); //hasBonded
        writeString(bw, cc.getText()); //text
        newLine(bw);
    }

    protected boolean filter(Card c) {
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
            //Return to...
            case "rtnotz": //Return to the Night of the Zealot
            case "rtdwl": //Return to the Dunwich Legacy
            case "rtptc": //Return to the Path to Carcosa
                return true;
            //Investigator Starter Decks
            case "nat":
            case "har":
            case "win":
            case "jac":
            case "ste":
                return true;
            default:
                return false;
        }
    }

    protected void exportCards(Cards cards, String predefinedPath, String path, String imagesPath) throws Exception {
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
                writeString(bw, "stage");
                writeString(bw, "action");
                writeString(bw, "reaction");
                writeString(bw, "free");
                writeString(bw, "hasBonded");
                writeString(bw, "text");
                newLine(bw);
                exportDefaultCards(imagesDir, bw, predefinedPath);
                for (Card c : cards) {
                    if (c.getHidden() != null && c.getHidden()) {
                        continue;
                    }
                    if (filter(c) == false) { //skip cards outside core set for now
                        continue;
                    }
                    if (flipped.contains(c.getCode())) {
                        Card cc = c.getLinkedCard();
                        exportFrontSide(imagesDir, bw, cc, false, true);
                        exportLinked(imagesDir, bw, cc, c);
                    } else {
                        boolean doubleSided = c.getDoubleSided() != null && c.getDoubleSided();
                        boolean linked = c.getLinkedCard() != null;
                        exportFrontSide(imagesDir, bw, c, doubleSided, linked);
                        if (doubleSided) {
                            exportBackSide(imagesDir, bw, c);
                        } else if (linked) {
                            Card cc = c.getLinkedCard();
                            exportLinked(imagesDir, bw, c, cc);
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
                for (Card c : cards) {
                    if (c.getHidden() != null && c.getHidden()) {
                        continue;
                    }
                    if (filter(c) == false) { //skip cards outside core set for now
                        continue;
                    }
                    if ("Basic Weakness".equals(weaknesses.getOrDefault(c.getCode(), c.getSubtypeName()))) {
                        Integer qty = c.getDeckbuilderQuantity();
                        if (qty == null) {
                            qty = 1;
                        }
                        if ("core".equals(c.getPackCode()) || "rcore".equals(c.getPackCode())) {
                            qty *= 2;
                        }
                        ArrayList<String> list = map.get(c.getPackCode());
                        if (list == null) {
                            list = new ArrayList<>();
                            map.put(c.getPackCode(), list);
                        }
                        for (int i = 0; i < qty; i++) {
                            list.add(c.getCode());
                        }
                        if (c.getTraits() != null && (c.getTraits().contains("Madness.") || c.getTraits().contains("Injury.") || c.getTraits().contains("Pact."))) {
                            list = mapMadnessInjuryPact.get(c.getPackCode());
                            if (list == null) {
                                list = new ArrayList<>();
                                mapMadnessInjuryPact.put(c.getPackCode(), list);
                            }
                            for (int i = 0; i < qty; i++) {
                                list.add(c.getCode());
                            }
                        }
                    }
                }
                line(bw, "{");
                line(bw, "    \"functions\": {");
                line(bw, "        \"GET_LIST_OF_WEAKNESSES\": {");
                line(bw, "            \"args\": [\"$SET_UUID\"],");
                line(bw, "            \"code\": [");
                line(bw, "                [\"VALIDATE_NOT_EMPTY\", \"$SET_UUID\", \"GET_LIST_OF_WEAKNESSES.SET_UUID\"],");
                line(bw, "                [\"COND\",");
                for (Map.Entry<String, ArrayList<String>> e : map.entrySet()) {
                    line(bw, String.format("                    [\"EQUAL\", \"$SET_UUID\", \"%s\"],", e.getKey()));
                    line(bw, String.format("                    [\"LIST\", \"%s\"],", StringUtils.join(e.getValue(), "\", \"")));
                }
                line(bw, "                    [\"TRUE\"],");
                line(bw, "                    [\"LIST\"]");
                line(bw, "                ]");
                line(bw, "            ]");
                line(bw, "        },");
                line(bw, "        \"GET_LIST_OF_MADNESS_INJURY_PACT_WEAKNESSES\": {");
                line(bw, "            \"args\": [\"$SET_UUID\"],");
                line(bw, "            \"code\": [");
                line(bw, "                [\"VALIDATE_NOT_EMPTY\", \"$SET_UUID\", \"GET_LIST_OF_MADNESS_INJURY_PACT_WEAKNESSES.SET_UUID\"],");
                line(bw, "                [\"COND\",");
                for (Map.Entry<String, ArrayList<String>> e : mapMadnessInjuryPact.entrySet()) {
                    line(bw, String.format("                    [\"EQUAL\", \"$SET_UUID\", \"%s\"],", e.getKey()));
                    line(bw, String.format("                    [\"LIST\", \"%s\"],", StringUtils.join(e.getValue(), "\", \"")));
                }
                line(bw, "                    [\"TRUE\"],");
                line(bw, "                    [\"LIST\"]");
                line(bw, "                ]");
                line(bw, "            ]");
                line(bw, "        }");
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
                    if (c.getHidden() != null && c.getHidden()) {
                        continue;
                    }
                    if (filter(c) == false) { //skip cards outside core set for now
                        continue;
                    }
                    if (bondedCards.containsKey(c.getName())) {
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
                        line(bw, "                    [\"DO_CREATE_CARDS\", \"$TARGET_PLAYER\", \"Hank Samson\", \"10016\", 1, \"$TARGET_PLAYER_ASIDE\", true, null]");
                    } else if (bcs.size() == 1) {
                        Card bc = bcs.get(0);
                        line(bw, String.format("                    [\"DO_CREATE_CARDS\", \"$TARGET_PLAYER\", \"%s\", \"%s\", %d, \"$TARGET_PLAYER_ASIDE\", true, null],", bc.getName().replace("\"", "\\\""), bc.getCode(), bc.getBondedCount()));
                    } else {
                        line(bw, "                    [");
                        int size = bcs.size();
                        for (int i = 0; i < size; i++) {
                            Card bc = bcs.get(i);
                            line(bw, String.format("                        [\"DO_CREATE_CARDS\", \"$TARGET_PLAYER\", \"%s\", \"%s\", %d, \"$TARGET_PLAYER_ASIDE\", true, null]%s", bc.getName().replace("\"", "\\\""), bc.getCode(), bc.getBondedCount(), i + 1 == size ? "" : ","));
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

    public void run() throws Exception {
        Util.downloadIfOld("https://arkhamdb.com/api/public/cards/?encounter=1", "run/cards.json");
        Cards cards = loadCards("run/cards.json");
        exportCards(cards, "run/predefined.xlsx", "run/arkhamhorrorlcg.tsv", "../../cards/arkham/dragncards-arkhamhorrorlcg-plugin/images");
        exportWeaknesses(cards, "../../cards/arkham/dragncards-arkhamhorrorlcg-plugin/jsons/Core Weakness.json");
        exportBonded(cards, "../../cards/arkham/dragncards-arkhamhorrorlcg-plugin/jsons/Core Bonded.json");
    }

    public static void main(String[] args) {
        try {
            new MainExportArkhamDB().run();
        } catch (Exception ex) {
            log(ex);
        }
    }

}
