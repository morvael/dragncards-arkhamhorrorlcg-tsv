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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jsoup.Jsoup;
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

    protected boolean writeTab;

    protected String readString(JsonNode c) {
        if (c == null || c.isNull()) {
            return null;
        } else {
            String s = c.asText();
            if (s.contains("  ")) {
                s = s.replace("  ", " ");
            }
            if (s.indexOf('\n') != -1) {
                s = s.replace("\n", "  ");
            }
            return s;
        }
    }

    @SuppressWarnings("UnnecessaryTemporaryOnConversionFromString")
    protected Integer readInteger(JsonNode c) {
        if (c == null || c.isNull()) {
            return null;
        } else {
            try {
                return Integer.parseInt(c.asText());
            } catch (NumberFormatException ex) {
                System.out.println(String.format("Error reading Integer field: %s", c.asText()));
                return null;
            }
        }
    }

    @SuppressWarnings("UnnecessaryTemporaryOnConversionFromString")
    protected Boolean readBoolean(JsonNode c) {
        if (c == null || c.isNull()) {
            return null;
        } else {
            switch (c.asText()) {
                case "true":
                    return Boolean.TRUE;
                case "false":
                    return Boolean.FALSE;
                default:
                    System.out.println(String.format("Error reading Boolean field: %s", c.asText()));
                    return null;
            }
        }
    }

    protected ArrayList<String> readStringList(JsonNode c) throws Exception {
        if (c.isArray()) {
            ArrayList<String> list = new ArrayList<>();
            for (int i = 0; i < c.size(); i++) {
                list.add(readString(c.get(i)));
            }
            return list;
        } else {
            if (c.isNull() == false) {
                System.out.println(String.format("Error reading StringList field: %s", c.asText()));
            }
            return null;
        }
    }

    protected LinkedHashMap<String, String> readStringStringMap(JsonNode c) throws Exception {
        if (c.isObject()) {
            LinkedHashMap<String, String> map = new LinkedHashMap<>();
            Iterator<String> it = c.fieldNames();
            while (it.hasNext()) {
                String fieldName = it.next();
                map.put(fieldName, readString(c.get(fieldName)));
            }
            return map;
        } else {
            if (c.isNull() == false) {
                System.out.println(String.format("Error reading StringStringMap field: %s", c.asText()));
            }
            return null;
        }
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
                            System.out.println(String.format("Unhandled field name in DeckRequirementsRandom: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType()));
                        }
                        break;
                }
            }
            return randomRequirement;
        } else {
            if (c.isNull() == false) {
                System.out.println(String.format("Error reading DeckRequirementsRandom field: %s", c.asText()));
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
                                System.out.println(String.format("Error reading DeckRequirements object field %s: %s", fieldName, cc.asText()));
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
                                System.out.println(String.format("Error reading DeckRequirements array field %s: %s", fieldName, cc.asText()));
                            }
                        }
                        break;
                    }
                    default:
                        if (unhandledDeckRequirement.add(fieldName)) {
                            System.out.println(String.format("Unhandled field name in DeckRequirements: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType()));
                        }
                        break;
                }
            }
            return deckRequirements;
        } else {
            if (c.isNull() == false) {
                System.out.println(String.format("Error reading DeckRequirements field: %s", c.asText()));
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
                            System.out.println(String.format("Unhandled field name in DeckOptionLevel: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType()));
                        }
                        break;
                }
            }
            return deckOptionLevel;
        } else {
            if (c.isNull() == false) {
                System.out.println(String.format("Error reading DeckOptionLevel field: %s", c.asText()));
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
                            System.out.println(String.format("Unhandled field name in DeckOptionAtLeast: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType()));
                        }
                        break;
                }
            }
            return deckOptionAtLeast;
        } else {
            if (c.isNull() == false) {
                System.out.println(String.format("Error reading DeckOptionAtLeast field: %s", c.asText()));
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
                                System.out.println(String.format("Error reading DeckOption array field %s: %s", fieldName, cc.asText()));
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
                            System.out.println(String.format("Unhandled field name in DeckOption: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType()));
                        }
                        break;
                }
            }
            return deckOption;
        } else {
            if (c.isNull() == false) {
                System.out.println(String.format("Error reading DeckOption field: %s", c.asText()));
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
                            System.out.println(String.format("Unhandled field name in ErrataDate: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType()));
                        }
                        break;
                }
            }
            return errataDate;
        } else {
            if (c.isNull() == false) {
                System.out.println(String.format("Error reading ErrataDate field: %s", c.asText()));
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
                            System.out.println(String.format("Unhandled field name in Restrictions: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType()));
                        }
                        break;
                }
            }
            return restrictions;
        } else {
            if (c.isNull() == false) {
                System.out.println(String.format("Error reading Restrictions field: %s", c.asText()));
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
                            System.out.println(String.format("Unhandled field name in BondedCard: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType()));
                        }
                        break;
                }
            }
            return bondedCard;
        } else {
            if (c.isNull() == false) {
                System.out.println(String.format("Error reading BondedCard field: %s", c.asText()));
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
                            System.out.println(String.format("Unhandled field name in CustomizationOptionCard: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType()));
                        }
                        break;
                }
            }
            return customizationOptionCard;
        } else {
            if (c.isNull() == false) {
                System.out.println(String.format("Error reading CustomizationOptionCard field: %s", c.asText()));
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
                            System.out.println(String.format("Unhandled field name in CustomizationOption: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType()));
                        }
                        break;
                }
            }
            return customizationOption;
        } else {
            if (c.isNull() == false) {
                System.out.println(String.format("Error reading CustomizationOption field: %s", c.asText()));
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
                                System.out.println(String.format("Error reading Card array field %s: %s", fieldName, cc.asText()));
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
                                System.out.println(String.format("Error reading Card array field %s: %s", fieldName, cc.asText()));
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
                                System.out.println(String.format("Error reading Card array field %s: %s", fieldName, cc.asText()));
                            }
                        }
                        break;
                    }
                    case "id":
                        card.setId(readInteger(c.get(fieldName)));
                        break;
                    default:
                        if (unhandledCard.add(fieldName)) {
                            System.out.println(String.format("Unhandled field name in Card: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType()));
                        }
                        break;
                }
            }
            return card;
        } else {
            if (c.isNull() == false) {
                System.out.println(String.format("Error reading Card field: %s", c.asText()));
            }
            return null;
        }
    }

    protected Cards loadCards(String path) throws Exception {
        File file = new File(path);
        if (file.exists() == false) {
            System.out.println(String.format("Download and save https://arkhamdb.com/api/public/cards/?encounter=1 to %s", file.getAbsoluteFile().getCanonicalPath()));
            return null;
        }
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
                System.out.println(String.format("Error reading Cards array: %s", c.asText()));
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

    private void exportDefaultCards(BufferedWriter bw, String predefinedPath) throws Exception {
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
                writeString(bw, getString(row, idx++)); //imageUrl
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
                writeHTML(bw, getString(row, idx++)); //text
                newLine(bw);
            }
        }
    }

    protected void exportFrontSide(BufferedWriter bw, Card c, boolean doubleSided, boolean linked) throws Exception {
        writeString(bw, c.getCode()); //databaseId
        writeString(bw, c.getFullName(true)); //name
        writeString(bw, c.getCardFront()); //imageUrl
        writeString(bw, doubleSided || linked ? "multi_sided" : c.getDefaultCardBack()); //cardBack
        writeString(bw, c.getTypeName()); //type
        writeString(bw, c.getSubtypeName()); //subtype
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
        writeHTML(bw, c.getText()); //text
        newLine(bw);
    }

    protected void exportBackSide(BufferedWriter bw, Card c) throws Exception {
        writeString(bw, c.getCode()); //databaseId
        writeString(bw, c.getBackName() != null ? c.getBackName() : c.getFullName("Location".equals(c.getTypeName()) == false)); //name
        writeString(bw, c.getCardBack()); //imageUrl
        writeString(bw, "multi_sided"); //cardBack
        writeString(bw, c.getTypeName()); //type
        writeString(bw, c.getSubtypeName()); //subtype
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
        writeHTML(bw, c.getBackText()); //text
        newLine(bw);
    }

    protected void exportLinked(BufferedWriter bw, Card c, Card cc) throws Exception {
        writeString(bw, c.getCode()); //databaseId: multi_sided must share
        writeString(bw, cc.getFullName(true)); //name
        writeString(bw, cc.getCardFront()); //imageUrl
        writeString(bw, "multi_sided"); //cardBack
        writeString(bw, cc.getTypeName()); //type
        writeString(bw, cc.getSubtypeName()); //subtype
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
        writeHTML(bw, cc.getText()); //text
        newLine(bw);
    }

    private boolean filter(Card c) {
        if ("Core Set".equals(c.getPackName())) {
            return true;
        }
        if ("Revised Core Set".equals(c.getPackName()) && c.getPosition() >= 183) {
            return true;
        }
        return false;
    }

    protected void exportCards(Cards cards, String predefinedPath, String path) throws Exception {
        File file = new File(path);
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
                writeString(bw, "text");
                newLine(bw);
                exportDefaultCards(bw, predefinedPath);
                for (Card c : cards) {
                    if (c.getHidden() != null && c.getHidden()) {
                        continue;
                    }
                    if (filter(c) == false) { //skip cards outside core set for now
                        continue;
                    }
                    boolean doubleSided = c.getDoubleSided() != null && c.getDoubleSided();
                    boolean linked = c.getLinkedCard() != null;
                    exportFrontSide(bw, c, doubleSided, linked);
                    if (doubleSided) {
                        exportBackSide(bw, c);
                    } else if (linked) {
                        Card cc = c.getLinkedCard();
                        exportLinked(bw, c, cc);
                    }
                }
                bw.flush();
            }
        }
    }

    protected void run() throws Exception {
        Cards cards = loadCards("run/cards.json");
        exportCards(cards, "run/predefined.xlsx", "run/arkhamhorrorlcg.tsv");
    }

    public static void main(String[] args) {
        try {
            new MainExportArkhamDB().run();
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }

}
