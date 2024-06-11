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

import java.util.ArrayList;

/**
 *
 * @author morvael
 */
public class Card implements Comparable<Card> {

    private String packCode;
    private String packName;
    private String typeCode;
    private String typeName;
    private String factionCode;
    private String factionName;
    private Integer position;
    private Boolean exceptional;
    private Boolean myriad;
    private String code;
    private String name;
    private String realName;
    private String subname;
    private String text;
    private String realText;
    private Integer quantity;
    private Integer skillWillpower;
    private Integer skillIntellect;
    private Integer skillCombat;
    private Integer skillAgility;
    private Integer health;
    private Boolean healthPerInvestigator;
    private Integer sanity;
    private Integer deckLimit;
    private String realSlot;
    private String traits;
    private String realTraits;
    private DeckRequirements deckRequirements;
    private ArrayList<DeckOption> deckOptions;
    private String flavor;
    private String illustrator;
    private Boolean unique;
    private Boolean permanent;
    private Boolean doubleSided;
    private String backText;
    private String backFlavor;
    private String octgnId;
    private String url;
    private String imagesrc;
    private String backimagesrc;
    private ArrayList<String> duplicatedBy;
    private ArrayList<String> alternatedBy;
    private Integer cost;
    private Integer xp;
    private String slot;
    private String subtypeCode;
    private String subtypeName;
    private ErrataDate errataDate;
    private Integer skillWild;
    private Restrictions restrictions;
    private String encounterCode;
    private String encounterName;
    private Integer encounterPosition;
    private Integer spoiler;
    private Integer enemyDamage;
    private Integer enemyHorror;
    private Integer enemyFight;
    private Integer enemyEvade;
    private Integer victory;
    private Integer shroud;
    private Integer clues;
    private Integer doom;
    private Integer stage;
    private String backName;
    private String tags;
    private String linkedToCode;
    private String linkedToName;
    private Card linkedCard;
    private Boolean hidden;
    private Boolean cluesFixed;
    private Boolean exile;
    private Integer vengeance;
    private String faction2Code;
    private String faction2Name;
    private ArrayList<BondedCard> bondedCards;
    private String bondedTo;
    private Integer bondedCount;
    private String alternateOfCode;
    private String alternateOfName;
    private String duplicateOfCode;
    private String duplicateOfName;
    private String faction3Code;
    private String faction3Name;
    private String customizationText;
    private String customizationChange;
    private ArrayList<CustomizationOption> customizationOptions;
    private Integer id;

    private Long sortOrder;

    public Card() {

    }

    public String getPackCode() {
        return packCode;
    }

    public void setPackCode(String packCode) {
        this.packCode = packCode;
    }

    public String getPackName() {
        return packName;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getFactionCode() {
        return factionCode;
    }

    public void setFactionCode(String factionCode) {
        this.factionCode = factionCode;
    }

    public String getFactionName() {
        return factionName;
    }

    public void setFactionName(String factionName) {
        this.factionName = factionName;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Boolean getExceptional() {
        return exceptional;
    }

    public void setExceptional(Boolean exceptional) {
        this.exceptional = exceptional;
    }

    public Boolean getMyriad() {
        return myriad;
    }

    public void setMyriad(Boolean myriad) {
        this.myriad = myriad;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getSubname() {
        return subname;
    }

    public void setSubname(String subname) {
        this.subname = subname;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getRealText() {
        return realText;
    }

    public void setRealText(String realText) {
        this.realText = realText;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getSkillWillpower() {
        return skillWillpower;
    }

    public void setSkillWillpower(Integer skillWillpower) {
        this.skillWillpower = skillWillpower;
    }

    public Integer getSkillIntellect() {
        return skillIntellect;
    }

    public void setSkillIntellect(Integer skillIntellect) {
        this.skillIntellect = skillIntellect;
    }

    public Integer getSkillCombat() {
        return skillCombat;
    }

    public void setSkillCombat(Integer skillCombat) {
        this.skillCombat = skillCombat;
    }

    public Integer getSkillAgility() {
        return skillAgility;
    }

    public void setSkillAgility(Integer skillAgility) {
        this.skillAgility = skillAgility;
    }

    public Integer getHealth() {
        return health;
    }

    public void setHealth(Integer health) {
        this.health = health;
    }

    public Boolean getHealthPerInvestigator() {
        return healthPerInvestigator;
    }

    public void setHealthPerInvestigator(Boolean healthPerInvestigator) {
        this.healthPerInvestigator = healthPerInvestigator;
    }

    public Integer getSanity() {
        return sanity;
    }

    public void setSanity(Integer sanity) {
        this.sanity = sanity;
    }

    public Integer getDeckLimit() {
        return deckLimit;
    }

    public void setDeckLimit(Integer deckLimit) {
        this.deckLimit = deckLimit;
    }

    public String getRealSlot() {
        return realSlot;
    }

    public void setRealSlot(String realSlot) {
        this.realSlot = realSlot;
    }

    public String getTraits() {
        return traits;
    }

    public void setTraits(String traits) {
        this.traits = traits;
    }

    public String getRealTraits() {
        return realTraits;
    }

    public void setRealTraits(String realTraits) {
        this.realTraits = realTraits;
    }

    public DeckRequirements getDeckRequirements() {
        return deckRequirements;
    }

    public void setDeckRequirements(DeckRequirements deckRequirements) {
        this.deckRequirements = deckRequirements;
    }

    public ArrayList<DeckOption> getDeckOptions() {
        return deckOptions;
    }

    public void setDeckOptions(ArrayList<DeckOption> deckOptions) {
        this.deckOptions = deckOptions;
    }

    public String getFlavor() {
        return flavor;
    }

    public void setFlavor(String flavor) {
        this.flavor = flavor;
    }

    public String getIllustrator() {
        return illustrator;
    }

    public void setIllustrator(String illustrator) {
        this.illustrator = illustrator;
    }

    public Boolean getUnique() {
        return unique;
    }

    public void setUnique(Boolean unique) {
        this.unique = unique;
    }

    public Boolean getPermanent() {
        return permanent;
    }

    public void setPermanent(Boolean permanent) {
        this.permanent = permanent;
    }

    public Boolean getDoubleSided() {
        return doubleSided;
    }

    public void setDoubleSided(Boolean doubleSided) {
        this.doubleSided = doubleSided;
    }

    public String getBackText() {
        return backText;
    }

    public void setBackText(String backText) {
        this.backText = backText;
    }

    public String getBackFlavor() {
        return backFlavor;
    }

    public void setBackFlavor(String backFlavor) {
        this.backFlavor = backFlavor;
    }

    public String getOctgnId() {
        return octgnId;
    }

    public void setOctgnId(String octgnId) {
        this.octgnId = octgnId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImagesrc() {
        return imagesrc;
    }

    public void setImagesrc(String imagesrc) {
        this.imagesrc = imagesrc;
    }

    public String getBackimagesrc() {
        return backimagesrc;
    }

    public void setBackimagesrc(String backimagesrc) {
        this.backimagesrc = backimagesrc;
    }

    public ArrayList<String> getDuplicatedBy() {
        return duplicatedBy;
    }

    public void setDuplicatedBy(ArrayList<String> duplicatedBy) {
        this.duplicatedBy = duplicatedBy;
    }

    public ArrayList<String> getAlternatedBy() {
        return alternatedBy;
    }

    public void setAlternatedBy(ArrayList<String> alternatedBy) {
        this.alternatedBy = alternatedBy;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    public Integer getXp() {
        return xp;
    }

    public void setXp(Integer xp) {
        this.xp = xp;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public String getSubtypeCode() {
        return subtypeCode;
    }

    public void setSubtypeCode(String subtypeCode) {
        this.subtypeCode = subtypeCode;
    }

    public String getSubtypeName() {
        return subtypeName;
    }

    public void setSubtypeName(String subtypeName) {
        this.subtypeName = subtypeName;
    }

    public ErrataDate getErrataDate() {
        return errataDate;
    }

    public void setErrataDate(ErrataDate errataDate) {
        this.errataDate = errataDate;
    }

    public Integer getSkillWild() {
        return skillWild;
    }

    public void setSkillWild(Integer skillWild) {
        this.skillWild = skillWild;
    }

    public Restrictions getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(Restrictions restrictions) {
        this.restrictions = restrictions;
    }

    public String getEncounterCode() {
        return encounterCode;
    }

    public void setEncounterCode(String encounterCode) {
        this.encounterCode = encounterCode;
    }

    public String getEncounterName() {
        return encounterName;
    }

    public void setEncounterName(String encounterName) {
        this.encounterName = encounterName;
    }

    public Integer getEncounterPosition() {
        return encounterPosition;
    }

    public void setEncounterPosition(Integer encounterPosition) {
        this.encounterPosition = encounterPosition;
    }

    public Integer getSpoiler() {
        return spoiler;
    }

    public void setSpoiler(Integer spoiler) {
        this.spoiler = spoiler;
    }

    public Integer getEnemyDamage() {
        return enemyDamage;
    }

    public void setEnemyDamage(Integer enemyDamage) {
        this.enemyDamage = enemyDamage;
    }

    public Integer getEnemyHorror() {
        return enemyHorror;
    }

    public void setEnemyHorror(Integer enemyHorror) {
        this.enemyHorror = enemyHorror;
    }

    public Integer getEnemyFight() {
        return enemyFight;
    }

    public void setEnemyFight(Integer enemyFight) {
        this.enemyFight = enemyFight;
    }

    public Integer getEnemyEvade() {
        return enemyEvade;
    }

    public void setEnemyEvade(Integer enemyEvade) {
        this.enemyEvade = enemyEvade;
    }

    public Integer getVictory() {
        return victory;
    }

    public void setVictory(Integer victory) {
        this.victory = victory;
    }

    public Integer getShroud() {
        return shroud;
    }

    public void setShroud(Integer shroud) {
        this.shroud = shroud;
    }

    public Integer getClues() {
        return clues;
    }

    public void setClues(Integer clues) {
        this.clues = clues;
    }

    public Integer getDoom() {
        return doom;
    }

    public void setDoom(Integer doom) {
        this.doom = doom;
    }

    public Integer getStage() {
        return stage;
    }

    public void setStage(Integer stage) {
        this.stage = stage;
    }

    public String getBackName() {
        return backName;
    }

    public void setBackName(String backName) {
        this.backName = backName;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getLinkedToCode() {
        return linkedToCode;
    }

    public void setLinkedToCode(String linkedToCode) {
        this.linkedToCode = linkedToCode;
    }

    public String getLinkedToName() {
        return linkedToName;
    }

    public void setLinkedToName(String linkedToName) {
        this.linkedToName = linkedToName;
    }

    public Card getLinkedCard() {
        return linkedCard;
    }

    public void setLinkedCard(Card linkedCard) {
        this.linkedCard = linkedCard;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public Boolean getCluesFixed() {
        return cluesFixed;
    }

    public void setCluesFixed(Boolean cluesFixed) {
        this.cluesFixed = cluesFixed;
    }

    public Boolean getExile() {
        return exile;
    }

    public void setExile(Boolean exile) {
        this.exile = exile;
    }

    public Integer getVengeance() {
        return vengeance;
    }

    public void setVengeance(Integer vengeance) {
        this.vengeance = vengeance;
    }

    public String getFaction2Code() {
        return faction2Code;
    }

    public void setFaction2Code(String faction2Code) {
        this.faction2Code = faction2Code;
    }

    public String getFaction2Name() {
        return faction2Name;
    }

    public void setFaction2Name(String faction2Name) {
        this.faction2Name = faction2Name;
    }

    public ArrayList<BondedCard> getBondedCards() {
        return bondedCards;
    }

    public void setBondedCards(ArrayList<BondedCard> bondedCards) {
        this.bondedCards = bondedCards;
    }

    public String getBondedTo() {
        return bondedTo;
    }

    public void setBondedTo(String bondedTo) {
        this.bondedTo = bondedTo;
    }

    public Integer getBondedCount() {
        return bondedCount;
    }

    public void setBondedCount(Integer bondedCount) {
        this.bondedCount = bondedCount;
    }

    public String getAlternateOfCode() {
        return alternateOfCode;
    }

    public void setAlternateOfCode(String alternateOfCode) {
        this.alternateOfCode = alternateOfCode;
    }

    public String getAlternateOfName() {
        return alternateOfName;
    }

    public void setAlternateOfName(String alternateOfName) {
        this.alternateOfName = alternateOfName;
    }

    public String getDuplicateOfCode() {
        return duplicateOfCode;
    }

    public void setDuplicateOfCode(String duplicateOfCode) {
        this.duplicateOfCode = duplicateOfCode;
    }

    public String getDuplicateOfName() {
        return duplicateOfName;
    }

    public void setDuplicateOfName(String duplicateOfName) {
        this.duplicateOfName = duplicateOfName;
    }

    public String getFaction3Code() {
        return faction3Code;
    }

    public void setFaction3Code(String faction3Code) {
        this.faction3Code = faction3Code;
    }

    public String getFaction3Name() {
        return faction3Name;
    }

    public void setFaction3Name(String faction3Name) {
        this.faction3Name = faction3Name;
    }

    public String getCustomizationText() {
        return customizationText;
    }

    public void setCustomizationText(String customizationText) {
        this.customizationText = customizationText;
    }

    public String getCustomizationChange() {
        return customizationChange;
    }

    public void setCustomizationChange(String customizationChange) {
        this.customizationChange = customizationChange;
    }

    public ArrayList<CustomizationOption> getCustomizationOptions() {
        return customizationOptions;
    }

    public void setCustomizationOptions(ArrayList<CustomizationOption> customizationOptions) {
        this.customizationOptions = customizationOptions;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    private long getSortOrder() {
        if (sortOrder == null) {
            if (code == null) {
                System.out.println("No code for " + name);
                return 0;
            }
            if (Character.isLetter(code.charAt(code.length() - 1))) {
                sortOrder = Long.parseLong(code.substring(0, code.length() - 1)) * 100L + (Character.toLowerCase(code.charAt(code.length() - 1)) - (int) 'a');
            } else {
                sortOrder = Long.parseLong(code) * 100L;
            }
        }
        return sortOrder;
    }

    @Override
    public int compareTo(Card o) {
        return Long.compare(getSortOrder(), o.getSortOrder());
    }

    public String getFullName(boolean showSubname) {
        if (showSubname && subname != null && subname.length() > 0) {
            return String.format("%s: %s", name, subname);
        } else {
            return name;
        }
    }

    public String getCardFront() {
        if (imagesrc == null || imagesrc.length() == 0) {
            return "missing.png";
        } else if (imagesrc.startsWith("/bundles/cards/")) {
            return imagesrc.substring(15);
        } else {
            return String.format("https://arkhamdb.com%s", imagesrc);
        }
    }

    public String getCardBack() {
        if (backimagesrc == null || backimagesrc.length() == 0) {
            return "missing.png";
        } else if (backimagesrc.startsWith("/bundles/cards/")) {
            return backimagesrc.substring(15);
        } else {
            return String.format("https://arkhamdb.com%s", backimagesrc);
        }
    }

    public String getDefaultCardBack() {
        if ((spoiler != null && spoiler == 1) || "Story".equals(typeName)) {
            return "encounter";
        } else {
            return "player";
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
        if ("encounter".equals(getDefaultCardBack())) {
            return 0;
        } else if (deckLimit != null) {
            return deckLimit;
        } else if (quantity != null) {
            return quantity;
        } else {
            System.out.println("No quantity for " + code);
            return null;
        }
    }

}
