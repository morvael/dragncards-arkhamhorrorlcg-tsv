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
package pl.derwinski.arkham.json;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pl.derwinski.arkham.Copyable;
import static pl.derwinski.arkham.Util.log;
import static pl.derwinski.arkham.Util.nvl;
import static pl.derwinski.arkham.Util.readBoolean;
import static pl.derwinski.arkham.Util.readInteger;
import static pl.derwinski.arkham.Util.readString;
import pl.derwinski.arkham.json.configuration.Configuration;
import pl.derwinski.arkham.json.metadata.Metadata;

/**
 *
 * @author morvael
 */
public final class Card implements Comparable<Card>, Copyable<Card> {

    private static final HashSet<String> unhandled = new HashSet<>();

    public static ArrayList<Card> readCards(Configuration configuration, Metadata metadata, JsonNode c) throws Exception {
        if (c.isArray()) {
            var result = new ArrayList<Card>();
            for (var i = 0; i < c.size(); i++) {
                var o = readCard(configuration, metadata, c.get(i));
                configuration.override(metadata, o);
                if (configuration.isIgnored(o) == false) {
                    result.add(o);
                }
            }
            configuration.process(metadata, result);
            return result;
        } else {
            if (c.isNull() == false) {
                log("Error reading Card array: %s", c.asText());
            }
            return null;
        }
    }

    private static Card readCard(Configuration configuration, Metadata metadata, JsonNode c, Card o) throws Exception {
        var it = c.fieldNames();
        while (it.hasNext()) {
            var fieldName = it.next();
            switch (fieldName) {
                case "back_flavor":
                    o.backFlavor = readString(c, fieldName);
                    break;
                case "real_back_flavor":
                    o.realBackFlavor = readString(c, fieldName);
                    o.backFlavor = nvl(o.backFlavor, o.realBackFlavor);
                    break;
                case "back_illustrator":
                    o.backIllustrator = readString(c, fieldName);
                    break;
                case "back_link_id":
                    o.backLinkId = readString(c, fieldName);
                    break;
                case "back_name":
                    o.backName = readString(c, fieldName);
                    break;
                case "real_back_name":
                    o.realBackName = readString(c, fieldName);
                    o.backName = nvl(o.backName, o.realBackName);
                    break;
                case "back_subname":
                    o.backSubname = readString(c, fieldName);
                    break;
                case "real_back_subname":
                    o.realBackSubname = readString(c, fieldName);
                    o.backSubname = nvl(o.backSubname, o.realBackSubname);
                    break;
                case "back_text":
                    o.backText = readString(c, fieldName);
                    break;
                case "real_back_text":
                    o.realBackText = readString(c, fieldName);
                    o.backText = nvl(o.backText, o.realBackText);
                    break;
                case "back_traits":
                    o.backTraits = readString(c, fieldName);
                    break;
                case "real_back_traits":
                    o.realBackTraits = readString(c, fieldName);
                    o.backTraits = nvl(o.backTraits, o.realBackTraits);
                    break;
                case "clues_fixed":
                    o.cluesFixed = nvl(readBoolean(c, fieldName), false);
                    break;
                case "clues":
                    o.clues = readInteger(c, fieldName);
                    break;
                case "code":
                    o.code = readString(c, fieldName);
                    break;
                case "cost":
                    o.cost = readInteger(c, fieldName);
                    break;
                case "deck_limit":
                    o.deckLimit = readInteger(c, fieldName);
                    break;
                case "doom_per_investigator":
                    o.doomPerInvestigator = nvl(readBoolean(c, fieldName), false);
                    break;
                case "doom":
                    o.doom = readInteger(c, fieldName);
                    break;
                case "double_sided":
                    o.doubleSided = nvl(readBoolean(c, fieldName), false);
                    break;
                case "encounter_code":
                    o.encounterCode = readString(c, fieldName);
                    o.encounterName = metadata.getEncounterName(o.encounterCode);
                    break;
                case "encounter_position":
                    o.encounterPosition = readInteger(c, fieldName);
                    break;
                case "enemy_damage":
                    o.enemyDamage = readInteger(c, fieldName);
                    break;
                case "enemy_evade_per_investigator":
                    o.enemyEvadePerInvestigator = nvl(readBoolean(c, fieldName), false);
                    break;
                case "enemy_evade":
                    o.enemyEvade = readInteger(c, fieldName);
                    break;
                case "enemy_fight_per_investigator":
                    o.enemyFightPerInvestigator = nvl(readBoolean(c, fieldName), false);
                    break;
                case "enemy_fight":
                    o.enemyFight = readInteger(c, fieldName);
                    break;
                case "enemy_horror":
                    o.enemyHorror = readInteger(c, fieldName);
                    break;
                case "errata_date":
                    o.errataDate = readString(c, fieldName);
                    break;
                case "exceptional":
                    o.exceptional = nvl(readBoolean(c, fieldName), false);
                    break;
                case "exile":
                    o.exile = nvl(readBoolean(c, fieldName), false);
                    break;
                case "faction2_code":
                    o.faction2Code = readString(c, fieldName);
                    o.faction2Name = metadata.getFactionName(o.faction2Code);
                    break;
                case "faction3_code":
                    o.faction3Code = readString(c, fieldName);
                    o.faction3Name = metadata.getFactionName(o.faction3Code);
                    break;
                case "faction_code":
                    o.factionCode = readString(c, fieldName);
                    o.factionName = metadata.getFactionName(o.factionCode);
                    break;
                case "flavor":
                    o.flavor = readString(c, fieldName);
                    break;
                case "real_flavor":
                    o.realFlavor = readString(c, fieldName);
                    o.flavor = nvl(o.flavor, o.realFlavor);
                    break;
                case "health_per_investigator":
                    o.healthPerInvestigator = nvl(readBoolean(c, fieldName), false);
                    break;
                case "health":
                    o.health = readInteger(c, fieldName);
                    break;
                case "hidden":
                    o.hidden = nvl(readBoolean(c, fieldName), false);
                    break;
                case "id":
                    o.id = readString(c, fieldName);
                    break;
                case "illustrator":
                    o.illustrator = readString(c, fieldName);
                    break;
                case "is_unique":
                    o.isUnique = nvl(readBoolean(c, fieldName), false);
                    break;
                case "linked":
                    o.linked = nvl(readBoolean(c, fieldName), false);
                    break;
                case "locale":
                    o.locale = readString(c, fieldName);
                    break;
                case "myriad":
                    o.myriad = nvl(readBoolean(c, fieldName), false);
                    break;
                case "name":
                    o.name = readString(c, fieldName);
                    break;
                case "real_name":
                    o.realName = readString(c, fieldName);
                    o.name = nvl(o.name, o.realName);
                    break;
                case "official":
                    o.official = nvl(readBoolean(c, fieldName), true);
                    break;
                case "pack_code":
                    o.packCode = readString(c, fieldName);
                    o.packName = metadata.getPackName(o.packCode);
                    break;
                case "pack_position":
                    o.packPosition = readInteger(c, fieldName);
                    break;
                case "permanent":
                    o.permanent = nvl(readBoolean(c, fieldName), false);
                    break;
                case "position":
                    o.position = readInteger(c, fieldName);
                    break;
                case "preview":
                    o.preview = nvl(readBoolean(c, fieldName), false);
                    break;
                case "quantity":
                    o.quantity = readInteger(c, fieldName);
                    break;
                case "sanity":
                    o.sanity = readInteger(c, fieldName);
                    break;
                case "shroud_per_investigator":
                    o.shroudPerInvestigator = nvl(readBoolean(c, fieldName), false);
                    break;
                case "shroud":
                    o.shroud = readInteger(c, fieldName);
                    break;
                case "skill_agility":
                    o.skillAgility = readInteger(c, fieldName);
                    break;
                case "skill_combat":
                    o.skillCombat = readInteger(c, fieldName);
                    break;
                case "skill_intellect":
                    o.skillIntellect = readInteger(c, fieldName);
                    break;
                case "skill_wild":
                    o.skillWild = readInteger(c, fieldName);
                    break;
                case "skill_willpower":
                    o.skillWillpower = readInteger(c, fieldName);
                    break;
                case "slot":
                    o.slot = readString(c, fieldName);
                    break;
                case "real_slot":
                    o.realSlot = readString(c, fieldName);
                    o.slot = nvl(o.slot, o.realSlot);
                    break;
                case "stage":
                    o.stage = readInteger(c, fieldName);
                    break;
                case "subname":
                    o.subname = readString(c, fieldName);
                    break;
                case "real_subname":
                    o.realSubname = readString(c, fieldName);
                    o.subname = nvl(o.subname, o.realSubname);
                    break;
                case "subtype_code":
                    o.subtypeCode = readString(c, fieldName);
                    o.subtypeName = metadata.getSubtypeName(o.subtypeCode);
                    break;
                case "taboo_set_id":
                    o.tabooSetId = readInteger(c, fieldName);
                    break;
                case "taboo_xp":
                    o.tabooXp = readInteger(c, fieldName);
                    break;
                case "text":
                    o.text = readString(c, fieldName);
                    break;
                case "real_text":
                    o.realText = readString(c, fieldName);
                    o.text = nvl(o.text, o.realText);
                    break;
                case "traits":
                    o.traits = readString(c, fieldName);
                    break;
                case "real_traits":
                    o.realTraits = readString(c, fieldName);
                    o.traits = nvl(o.traits, o.realTraits);
                    break;
                case "type_code":
                    o.typeCode = readString(c, fieldName);
                    o.typeName = metadata.getTypeName(o.typeCode);
                    break;
                case "vengeance":
                    o.vengeance = readInteger(c, fieldName);
                    break;
                case "victory":
                    o.victory = readInteger(c, fieldName);
                    break;
                case "xp":
                    o.xp = readInteger(c, fieldName);
                    break;
                // ignored fields
                case "alt_art_investigator":
                case "alternate_of_code":
                case "bonded_count":
                case "bonded_to":
                case "customization_change":
                case "customization_options":
                case "customization_text":
                case "deck_options":
                case "deck_requirements":
                case "duplicate_of_code":
                case "heals_damage":
                case "heals_horror":
                case "real_customization_change":
                case "real_customization_text":
                case "real_taboo_text_change":
                case "restrictions":
                case "side_deck_options":
                case "side_deck_requirements":
                case "starts_in_hand":
                case "starts_in_play":
                case "sticky_mulligan":
                case "taboo_text_change":
                case "tags":
                    break;
                default:
                    if (unhandled.add(fieldName)) {
                        log("Unhandled field name in Card: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType());
                    }
                    break;
            }
        }
        o.cardBack = configuration.getCardBack(o);
        return o;
    }

    public static Card readCard(Configuration configuration, Metadata metadata, JsonNode c) throws Exception {
        if (c.isObject()) {
            return readCard(configuration, metadata, c, new Card());
        } else {
            if (c.isNull() == false) {
                log("Error reading Card object: %s", c.asText());
            }
            return null;
        }
    }

    private String backFlavor;
    private String realBackFlavor;
    private String backIllustrator;
    private String backLinkId;
    private String backName;
    private String realBackName;
    private String backSubname;
    private String realBackSubname;
    private String backText;
    private String realBackText;
    private String backTraits;
    private String realBackTraits;
    private Boolean cluesFixed = false;
    private Integer clues;
    private String code;
    private Integer cost;
    private Integer deckLimit;
    private Boolean doomPerInvestigator = false;
    private Integer doom;
    private Boolean doubleSided = false;
    private String encounterCode;
    private String encounterName;
    private Integer encounterPosition;
    private Integer enemyDamage;
    private Boolean enemyEvadePerInvestigator = false;
    private Integer enemyEvade;
    private Boolean enemyFightPerInvestigator = false;
    private Integer enemyFight;
    private Integer enemyHorror;
    private String errataDate;
    private Boolean exceptional = false;
    private Boolean exile = false;
    private String faction2Code;
    private String faction2Name;
    private String faction3Code;
    private String faction3Name;
    private String factionCode;
    private String factionName;
    private String flavor;
    private String realFlavor;
    private Boolean healthPerInvestigator = false;
    private Integer health;
    private Boolean hidden = false;
    private String id;
    private String illustrator;
    private Boolean isUnique = false;
    private Boolean linked = false;
    private String locale;
    private Boolean myriad = false;
    private String name;
    private String realName;
    private Boolean official = true;
    private String packCode;
    private String packName;
    @Deprecated
    private Integer packPosition;
    private Boolean permanent = false;
    private Integer position;
    private Boolean preview = false;
    private Integer quantity;
    private Integer sanity;
    private Boolean shroudPerInvestigator = false;
    private Integer shroud;
    private Integer skillAgility;
    private Integer skillCombat;
    private Integer skillIntellect;
    private Integer skillWild;
    private Integer skillWillpower;
    private String slot;
    private String realSlot;
    private Integer stage;
    private String subname;
    private String realSubname;
    private String subtypeCode;
    private String subtypeName;
    private Integer tabooSetId;
    private Integer tabooXp;
    private String text;
    private String realText;
    private String traits;
    private String realTraits;
    private String typeCode;
    private String typeName;
    private Integer vengeance;
    private Integer victory;
    private Integer xp;
    //
    private String cardBack;
    private boolean parallel;
    private String frontId;
    private Integer frontPosition;
    private String backId;
    private Integer backPosition;
    private String sortId;
    private int sortAdd;
    private String miniCode;

    private Long sortOrder;

    public Card() {

    }

    public Card tabooClone(int tabooSetId) {
        Card c = copy();
        c.id = "%s-%d".formatted(code, tabooSetId);
        c.tabooSetId = tabooSetId;
        return c;
    }

    public Card parallelClone(Card back, String sortId, int sortAdd, String miniCode) {
        this.hidden = null;
        back.hidden = null;
        Card c = copy();
        c.parallel = true;
        c.frontId = id;
        c.frontPosition = position;
        c.backId = back.id;
        c.backPosition = back.position;
        c.sortId = sortId;
        c.sortAdd = sortAdd;
        c.miniCode = miniCode;
        c.tabooSetId = Math.max(nvl(tabooSetId, 0), nvl(back.tabooSetId, 0));
        //
        c.backFlavor = back.backFlavor;
        c.realBackFlavor = back.realBackFlavor;
        c.backName = back.backName;
        c.realBackName = back.realBackName;
        c.backSubname = back.backSubname;
        c.realBackSubname = back.realBackSubname;
        c.backText = back.backText;
        c.realBackText = back.realBackText;
        c.backTraits = back.backTraits;
        c.realBackTraits = back.realBackTraits;
        c.code = String.format("%s%s", code, back.code);
        if (id.contains("-") || back.id.contains("-")) {
            c.id = String.format("%s%s-%d", code, back.code, c.tabooSetId);
        } else {
            c.id = String.format("%s%s", code, back.code);
        }
        c.packCode = back.id.startsWith("9") ? back.packCode : packCode;
        c.packName = String.format("%s / %s", packName, back.packName);
        return c;
    }

    @Override
    public Card copy() {
        Card o = new Card();
        o.backFlavor = backFlavor;
        o.realBackFlavor = realBackFlavor;
        o.backIllustrator = backIllustrator;
        o.backLinkId = backLinkId;
        o.backName = backName;
        o.realBackName = realBackName;
        o.backSubname = backSubname;
        o.realBackSubname = realBackSubname;
        o.backText = backText;
        o.realBackText = realBackText;
        o.backTraits = backTraits;
        o.realBackTraits = realBackTraits;
        o.cluesFixed = cluesFixed;
        o.clues = clues;
        o.code = code;
        o.cost = cost;
        o.deckLimit = deckLimit;
        o.doomPerInvestigator = doomPerInvestigator;
        o.doom = doom;
        o.doubleSided = doubleSided;
        o.encounterCode = encounterCode;
        o.encounterName = encounterName;
        o.encounterPosition = encounterPosition;
        o.enemyDamage = enemyDamage;
        o.enemyEvadePerInvestigator = enemyEvadePerInvestigator;
        o.enemyEvade = enemyEvade;
        o.enemyFightPerInvestigator = enemyFightPerInvestigator;
        o.enemyFight = enemyFight;
        o.enemyHorror = enemyHorror;
        o.errataDate = errataDate;
        o.exceptional = exceptional;
        o.exile = exile;
        o.faction2Code = faction2Code;
        o.faction2Name = faction2Name;
        o.faction3Code = faction3Code;
        o.faction3Name = faction3Name;
        o.factionCode = factionCode;
        o.factionName = factionName;
        o.flavor = flavor;
        o.realFlavor = realFlavor;
        o.healthPerInvestigator = healthPerInvestigator;
        o.health = health;
        o.hidden = hidden;
        o.id = id;
        o.illustrator = illustrator;
        o.isUnique = isUnique;
        o.linked = linked;
        o.locale = locale;
        o.myriad = myriad;
        o.name = name;
        o.realName = realName;
        o.official = official;
        o.packCode = packCode;
        o.packName = packName;
        o.packPosition = packPosition;
        o.permanent = permanent;
        o.position = position;
        o.preview = preview;
        o.quantity = quantity;
        o.sanity = sanity;
        o.shroudPerInvestigator = shroudPerInvestigator;
        o.shroud = shroud;
        o.skillAgility = skillAgility;
        o.skillCombat = skillCombat;
        o.skillIntellect = skillIntellect;
        o.skillWild = skillWild;
        o.skillWillpower = skillWillpower;
        o.slot = slot;
        o.realSlot = realSlot;
        o.stage = stage;
        o.subname = subname;
        o.realSubname = realSubname;
        o.subtypeCode = subtypeCode;
        o.subtypeName = subtypeName;
        o.tabooSetId = tabooSetId;
        o.tabooXp = tabooXp;
        o.text = text;
        o.realText = realText;
        o.traits = traits;
        o.realTraits = realTraits;
        o.typeCode = typeCode;
        o.typeName = typeName;
        o.vengeance = vengeance;
        o.victory = victory;
        o.xp = xp;
        //
        o.cardBack = cardBack;
        o.parallel = parallel;
        o.frontId = frontId;
        o.frontPosition = frontPosition;
        o.backId = backId;
        o.backPosition = backPosition;
        o.sortId = sortId;
        o.sortAdd = sortAdd;
        o.miniCode = miniCode;
        return o;
    }

    public String getBackFlavor() {
        return backFlavor;
    }

    public String getBackIllustrator() {
        return backIllustrator;
    }

    public String getBackLinkId() {
        return backLinkId;
    }

    public String getBackName() {
        return backName;
    }

    public String getBackSubname() {
        return backSubname;
    }

    public String getBackText() {
        return backText;
    }

    public String getBackTraits() {
        return backTraits;
    }

    public Boolean getCluesFixed() {
        return cluesFixed;
    }

    public Integer getClues() {
        return clues;
    }

    public String getCode() {
        return code;
    }

    public Integer getCost() {
        return cost;
    }

    public Integer getDeckLimit() {
        return deckLimit;
    }

    public Boolean getDoomPerInvestigator() {
        return doomPerInvestigator;
    }

    public Integer getDoom() {
        return doom;
    }

    public Boolean getDoubleSided() {
        return doubleSided;
    }

    public String getEncounterCode() {
        return encounterCode;
    }

    public String getEncounterName() {
        return encounterName;
    }

    public Integer getEncounterPosition() {
        return encounterPosition;
    }

    public Integer getEnemyDamage() {
        return enemyDamage;
    }

    public Boolean getEnemyEvadePerInvestigator() {
        return enemyEvadePerInvestigator;
    }

    public Integer getEnemyEvade() {
        return enemyEvade;
    }

    public Boolean getEnemyFightPerInvestigator() {
        return enemyFightPerInvestigator;
    }

    public Integer getEnemyFight() {
        return enemyFight;
    }

    public Integer getEnemyHorror() {
        return enemyHorror;
    }

    public String getErrataDate() {
        return errataDate;
    }

    public Boolean getExceptional() {
        return exceptional;
    }

    public Boolean getExile() {
        return exile;
    }

    public String getFaction2Code() {
        return faction2Code;
    }

    public String getFaction2Name() {
        return faction2Name;
    }

    public String getFaction3Code() {
        return faction3Code;
    }

    public String getFaction3Name() {
        return faction3Name;
    }

    public String getFactionCode() {
        return factionCode;
    }

    public String getFactionName() {
        return factionName;
    }

    public String getFlavor() {
        return flavor;
    }

    public Boolean getHealthPerInvestigator() {
        return healthPerInvestigator;
    }

    public Integer getHealth() {
        return health;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public String getId() {
        return id;
    }

    public String getIllustrator() {
        return illustrator;
    }

    public Boolean getIsUnique() {
        return isUnique;
    }

    public Boolean getLinked() {
        return linked;
    }

    public String getLocale() {
        return locale;
    }

    public Boolean getMyriad() {
        return myriad;
    }

    public String getName() {
        return name;
    }

    public Boolean getOfficial() {
        return official;
    }

    public String getPackCode() {
        return packCode;
    }

    public String getPackName() {
        return packName;
    }

    @Deprecated
    public Integer getPackPosition() {
        return packPosition;
    }

    public Boolean getPermanent() {
        return permanent;
    }

    public Integer getPosition() {
        return position;
    }

    public Boolean getPreview() {
        return preview;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getSanity() {
        return sanity;
    }

    public Boolean getShroudPerInvestigator() {
        return shroudPerInvestigator;
    }

    public Integer getShroud() {
        return shroud;
    }

    public Integer getSkillAgility() {
        return skillAgility;
    }

    public Integer getSkillCombat() {
        return skillCombat;
    }

    public Integer getSkillIntellect() {
        return skillIntellect;
    }

    public Integer getSkillWild() {
        return skillWild;
    }

    public Integer getSkillWillpower() {
        return skillWillpower;
    }

    public String getSlot() {
        return slot;
    }

    public Integer getStage() {
        return stage;
    }

    public String getSubname() {
        return subname;
    }

    public String getRealSubname() {
        return realSubname;
    }

    public String getSubtypeCode() {
        return subtypeCode;
    }

    public String getSubtypeName() {
        return subtypeName;
    }

    public Integer getTabooSetId() {
        return tabooSetId;
    }

    public Integer getTabooXp() {
        return tabooXp;
    }

    public String getText() {
        return text;
    }

    public String getTraits() {
        return traits;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public String getTypeName() {
        return typeName;
    }

    public Integer getVengeance() {
        return vengeance;
    }

    public Integer getVictory() {
        return victory;
    }

    public Integer getXp() {
        return xp;
    }

    public boolean isParallel() {
        return parallel;
    }

    public String getMiniCode() {
        return miniCode;
    }

    public String getImageId(boolean front) {
        if (parallel) {
            return front ? frontId : backId;
        } else {
            return id;
        }
    }

    public String getMiniImageId() {
        return nvl(miniCode, code);
    }

    private static final Pattern ID = Pattern.compile("([0-9]+)([a-z])?(?:-([0-9]+))?");
    private static final int BASE_CHAR = (int) '`'; //so that a becomes 1

    private long getSortOrder() {
        if (sortOrder == null) {
            var c = nvl(sortId, id);
            if (c == null) {
                log("No code for %s %s", id, name);
                sortOrder = 0L;
            } else {
                var m = ID.matcher(c);
                if (m.matches() == false) {
                    log("No code for %s %s", id, name);
                    sortOrder = 0L;
                } else {
                    try {
                        var cd = Long.valueOf(nvl(m.group(1), "0"));
                        var lt = (long) (nvl(m.group(2), "`").charAt(0) - BASE_CHAR);
                        var tb = Long.valueOf(nvl(m.group(3), "0"));
                        sortOrder = cd * 10000000L + lt * 100000L + tb * 100L + sortAdd; //2 digits for letter, 3 digits for taboo, 2 digits for sortAdd
                    } catch (Exception ex) {
                        log("Failed to calculate code for %s %s", id, name);
                        sortOrder = 0L;
                    }
                }
            }
        }
        return sortOrder;
    }

    @Override
    public int compareTo(Card o) {
        return Long.compare(getSortOrder(), o.getSortOrder());
    }

    public String getFrontFullName(boolean showSubname) {
        if (showSubname && subname != null) {
            return String.format("%s: %s", name, subname);
        } else {
            return name;
        }
    }

    public String getBackFullName(boolean showSubname) {
        if ("Investigator".equals(typeName)) {
            return getFrontFullName(showSubname);
        } else if (showSubname && backSubname != null) {
            return "%s: %s".formatted(nvl(backName, name), backSubname);
        } else {
            return nvl(backName, name);
        }
    }

    public String getCardBack() {
        if (cardBack != null) {
            return cardBack;
        } else if (encounterCode != null) {
            return "Encounter Card";
        } else {
            return "Player Card";
        }
    }

    public String getFactions() {
        StringBuilder sb = new StringBuilder();
        if (factionName != null && factionName.length() > 0) {
            sb.append(factionName);
            sb.append(". ");
        }
        if (faction2Name != null && faction2Name.length() > 0) {
            sb.append(faction2Name);
            sb.append(". ");
        }
        if (faction3Name != null && faction3Name.length() > 0) {
            sb.append(faction3Name);
            sb.append(". ");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
            return sb.toString();
        } else {
            return null;
        }
    }

    public Integer getDeckbuilderQuantity() {
        if (tabooSetId != null && tabooSetId > 0) {
            return 0;
        } else if (deckLimit != null) {
            return deckLimit;
        } else if ("Investigator".equals(typeName)) {
            return 1;
        } else if (encounterCode != null) {
            return 0;
        } else if (quantity != null) {
            return quantity;
        } else {
            log("No quantity for %s", id);
            return null;
        }
    }

    private static final Pattern USES = Pattern.compile("Uses \\(([0-9X]+) ", Pattern.MULTILINE);

    public Integer getUses() {
        if (text != null && text.contains("Uses (")) {
            Matcher m = USES.matcher(text);
            if (m.find()) {
                String value = m.group(1);
                if ("X".equals(value)) {
                    return -2;
                } else {
                    return Integer.valueOf(m.group(1));
                }
            }
        }
        return null;
    }

    private static final Pattern BONDED = Pattern.compile("Bonded \\(([^)]+)\\)", Pattern.MULTILINE);

    public String getBondedTo() {
        if (text != null && text.contains("Bonded (")) {
            Matcher m = BONDED.matcher(text);
            if (m.find()) {
                return m.group(1);
            }
        }
        return null;
    }

    public Integer getPosition(boolean front) {
        if (parallel) {
            return front ? frontPosition : backPosition;
        } else {
            return position;
        }
    }

    public void flip(Card c) {
        //this becomes front card
        //c becomes back card
        var oldCode = code;
        var oldId = id;
        backLinkId = oldId;
        code = c.code;
        id = c.id;
        hidden = false;
        c.backLinkId = null;
        c.code = oldCode;
        c.id = oldId;
        c.hidden = true;
    }

    public void hide() {
        this.hidden = true;
    }

    public void miniCode(String miniCode) {
        this.miniCode = miniCode;
    }

    public void override(Configuration configuration, Metadata metadata, JsonNode override) throws Exception {
        if (override != null && override.isObject()) {
            readCard(configuration, metadata, override, this);
        }
    }

    public boolean tabooEquals(Card other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!Objects.equals(this.backFlavor, other.backFlavor)) {
            return false;
        }
//        if (!Objects.equals(this.realBackFlavor, other.realBackFlavor)) {
//            return false;
//        }
        if (!Objects.equals(this.backIllustrator, other.backIllustrator)) {
            return false;
        }
        if (!Objects.equals(this.backLinkId, other.backLinkId)) {
            return false;
        }
        if (!Objects.equals(this.backName, other.backName)) {
            return false;
        }
//        if (!Objects.equals(this.realBackName, other.realBackName)) {
//            return false;
//        }
        if (!Objects.equals(this.backSubname, other.backSubname)) {
            return false;
        }
//        if (!Objects.equals(this.realBackSubname, other.realBackSubname)) {
//            return false;
//        }
        if (!Objects.equals(this.backText, other.backText)) {
            return false;
        }
//        if (!Objects.equals(this.realBackText, other.realBackText)) {
//            return false;
//        }
        if (!Objects.equals(this.backTraits, other.backTraits)) {
            return false;
        }
//        if (!Objects.equals(this.realBackTraits, other.realBackTraits)) {
//            return false;
//        }
        if (!Objects.equals(this.code, other.code)) {
            return false;
        }
        if (!Objects.equals(this.encounterCode, other.encounterCode)) {
            return false;
        }
        if (!Objects.equals(this.encounterName, other.encounterName)) {
            return false;
        }
        if (!Objects.equals(this.errataDate, other.errataDate)) {
            return false;
        }
        if (!Objects.equals(this.faction2Code, other.faction2Code)) {
            return false;
        }
        if (!Objects.equals(this.faction2Name, other.faction2Name)) {
            return false;
        }
        if (!Objects.equals(this.faction3Code, other.faction3Code)) {
            return false;
        }
        if (!Objects.equals(this.faction3Name, other.faction3Name)) {
            return false;
        }
        if (!Objects.equals(this.factionCode, other.factionCode)) {
            return false;
        }
        if (!Objects.equals(this.factionName, other.factionName)) {
            return false;
        }
        if (!Objects.equals(this.flavor, other.flavor)) {
            return false;
        }
//        if (!Objects.equals(this.realFlavor, other.realFlavor)) {
//            return false;
//        }
        if (!Objects.equals(this.illustrator, other.illustrator)) {
            return false;
        }
        if (!Objects.equals(this.locale, other.locale)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
//        if (!Objects.equals(this.realName, other.realName)) {
//            return false;
//        }
        if (!Objects.equals(this.packCode, other.packCode)) {
            return false;
        }
        if (!Objects.equals(this.packName, other.packName)) {
            return false;
        }
        if (!Objects.equals(this.slot, other.slot)) {
            return false;
        }
//        if (!Objects.equals(this.realSlot, other.realSlot)) {
//            return false;
//        }
        if (!Objects.equals(this.subname, other.subname)) {
            return false;
        }
//        if (!Objects.equals(this.realSubname, other.realSubname)) {
//            return false;
//        }
        if (!Objects.equals(this.subtypeCode, other.subtypeCode)) {
            return false;
        }
        if (!Objects.equals(this.subtypeName, other.subtypeName)) {
            return false;
        }
        if (!Objects.equals(this.text, other.text)) {
            return false;
        }
//        if (!Objects.equals(this.realText, other.realText)) {
//            return false;
//        }
        if (!Objects.equals(this.traits, other.traits)) {
            return false;
        }
//        if (!Objects.equals(this.realTraits, other.realTraits)) {
//            return false;
//        }
        if (!Objects.equals(this.typeCode, other.typeCode)) {
            return false;
        }
        if (!Objects.equals(this.typeName, other.typeName)) {
            return false;
        }
        if (!Objects.equals(this.cluesFixed, other.cluesFixed)) {
            return false;
        }
        if (!Objects.equals(this.clues, other.clues)) {
            return false;
        }
        if (!Objects.equals(this.cost, other.cost)) {
            return false;
        }
        if (!Objects.equals(this.deckLimit, other.deckLimit)) {
            return false;
        }
        if (!Objects.equals(this.doomPerInvestigator, other.doomPerInvestigator)) {
            return false;
        }
        if (!Objects.equals(this.doom, other.doom)) {
            return false;
        }
        if (!Objects.equals(this.doubleSided, other.doubleSided)) {
            return false;
        }
        if (!Objects.equals(this.encounterPosition, other.encounterPosition)) {
            return false;
        }
        if (!Objects.equals(this.enemyDamage, other.enemyDamage)) {
            return false;
        }
        if (!Objects.equals(this.enemyEvadePerInvestigator, other.enemyEvadePerInvestigator)) {
            return false;
        }
        if (!Objects.equals(this.enemyEvade, other.enemyEvade)) {
            return false;
        }
        if (!Objects.equals(this.enemyFightPerInvestigator, other.enemyFightPerInvestigator)) {
            return false;
        }
        if (!Objects.equals(this.enemyFight, other.enemyFight)) {
            return false;
        }
        if (!Objects.equals(this.enemyHorror, other.enemyHorror)) {
            return false;
        }
        if (!Objects.equals(this.exceptional, other.exceptional)) {
            return false;
        }
        if (!Objects.equals(this.exile, other.exile)) {
            return false;
        }
        if (!Objects.equals(this.healthPerInvestigator, other.healthPerInvestigator)) {
            return false;
        }
        if (!Objects.equals(this.health, other.health)) {
            return false;
        }
        if (!Objects.equals(this.hidden, other.hidden)) {
            return false;
        }
        if (!Objects.equals(this.isUnique, other.isUnique)) {
            return false;
        }
        if (!Objects.equals(this.linked, other.linked)) {
            return false;
        }
        if (!Objects.equals(this.myriad, other.myriad)) {
            return false;
        }
        if (!Objects.equals(this.official, other.official)) {
            return false;
        }
        if (!Objects.equals(this.packPosition, other.packPosition)) {
            return false;
        }
        if (!Objects.equals(this.permanent, other.permanent)) {
            return false;
        }
        if (!Objects.equals(this.position, other.position)) {
            return false;
        }
        if (!Objects.equals(this.preview, other.preview)) {
            return false;
        }
        if (!Objects.equals(this.quantity, other.quantity)) {
            return false;
        }
        if (!Objects.equals(this.sanity, other.sanity)) {
            return false;
        }
        if (!Objects.equals(this.shroudPerInvestigator, other.shroudPerInvestigator)) {
            return false;
        }
        if (!Objects.equals(this.shroud, other.shroud)) {
            return false;
        }
        if (!Objects.equals(this.skillAgility, other.skillAgility)) {
            return false;
        }
        if (!Objects.equals(this.skillCombat, other.skillCombat)) {
            return false;
        }
        if (!Objects.equals(this.skillIntellect, other.skillIntellect)) {
            return false;
        }
        if (!Objects.equals(this.skillWild, other.skillWild)) {
            return false;
        }
        if (!Objects.equals(this.skillWillpower, other.skillWillpower)) {
            return false;
        }
        if (!Objects.equals(this.stage, other.stage)) {
            return false;
        }
        if (!Objects.equals(this.tabooXp, other.tabooXp)) {
            return false;
        }
        if (!Objects.equals(this.vengeance, other.vengeance)) {
            return false;
        }
        if (!Objects.equals(this.victory, other.victory)) {
            return false;
        }
        return Objects.equals(this.xp, other.xp);
    }

    @Override
    public String toString() {
        return String.format("%s %s", id, name);
    }

}
