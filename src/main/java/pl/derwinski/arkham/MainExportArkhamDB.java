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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import static pl.derwinski.arkham.Util.log;
import static pl.derwinski.arkham.Util.nvl;
import pl.derwinski.arkham.json.Card;
import pl.derwinski.arkham.json.Cards;
import pl.derwinski.arkham.json.configuration.Configuration;
import pl.derwinski.arkham.json.metadata.Metadata;

/**
 * Console program to convert ArkhamDB json into TSV file.
 *
 * @author morvael
 */
public final class MainExportArkhamDB {

    private Cards cards;
    private Configuration config;
    private Metadata meta;
    private boolean writeTab;

    public MainExportArkhamDB() {

    }

    private void writeBoolean(BufferedWriter bw, Boolean bool) throws Exception {
        writeString(bw, bool != null ? (bool ? "1" : "0") : null);
    }

    private void writeInteger(BufferedWriter bw, Integer number) throws Exception {
        writeString(bw, number != null ? number.toString() : null);
    }

    private void writeString(BufferedWriter bw, String text) throws Exception {
        if (writeTab) {
            bw.write('\t');
        } else {
            writeTab = true;
        }
        if (text != null) {
            bw.write(text.replace("’", "'").replace("·", "•").replace("“", "\"").replace("”", "\"").replace("–", "-").replace("…", "...").replace("[free]", "[fast]"));
        }
    }

    private void newLine(BufferedWriter bw) throws Exception {
        bw.newLine();
        writeTab = false;
    }

    private String getString(Row row, int index) {
        try {
            var c = row.getCell(index);
            if (c == null) {
                return null;
            } else if (c.getCellType() == CellType.NUMERIC) {
                return Long.toString((long) row.getCell(index).getNumericCellValue());
            } else if (c.getCellType() == CellType.STRING) {
                var s = row.getCell(index).getStringCellValue().trim();
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

    private Integer getInteger(Row row, int index) {
        try {
            var c = row.getCell(index);
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

    private Boolean getBoolean(Row row, int index) {
        var i = getInteger(row, index);
        if (i == null) {
            return null;
        } else {
            return i != 0;
        }
    }

    private String getImageUrl(File imagesDir, String databaseId, Boolean front) throws Exception {
        if (front == null) {
            if (imagesDir.exists()) {
                var imageFile = new File(imagesDir, databaseId);
                if (imageFile.exists() == false) {
                    log("Missing image %s", databaseId);
                }
            }
            return databaseId;
        } else {
            var id = config.getImageMapping(databaseId);
            var imageUrl = String.format("card_images/%s/%s%s.webp", id.substring(0, 2), id, front ? "a" : "b");
            if (imagesDir.exists()) {
                var imageFile = new File(imagesDir, imageUrl);
                if (imageFile.exists() == false) {
                    log("Missing image %s for id %s %s", imageUrl, databaseId, front ? "front" : "back");
                }
            }
            return imageUrl;
        }
    }

    private void exportDefaultCards(File imagesDir, BufferedWriter bw, String predefinedPath) throws Exception {
        try (var wb = WorkbookFactory.create(new File(predefinedPath), null, true)) {
            var sheet = wb.getSheetAt(0);
            for (var row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }
                var idx = 0;
                var databaseId = getString(row, idx++);
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
                writeBoolean(bw, nvl(getBoolean(row, idx++), false)); //parallelContent
                writeString(bw, nvl(getString(row, idx++), databaseId)); //code
                writeInteger(bw, nvl(getInteger(row, idx++), 0)); //tabooId
                writeString(bw, nvl(getString(row, idx++), "None")); //tabooName
                writeInteger(bw, nvl(getInteger(row, idx++), 0)); //tabooXp
                writeBoolean(bw, getBoolean(row, idx++)); //action
                writeBoolean(bw, getBoolean(row, idx++)); //reaction
                writeBoolean(bw, getBoolean(row, idx++)); //free
                writeBoolean(bw, getBoolean(row, idx++)); //hasBonded
                writeString(bw, getString(row, idx++)); //text
                newLine(bw);
            }
        }
    }

    private void exportFrontSide(File imagesDir, BufferedWriter bw, Card c, boolean doubleSided, boolean linked) throws Exception {
        var cardBack = doubleSided || linked ? "multi_sided" : c.getCardBack();
        writeString(bw, c.getId()); //databaseId
        writeString(bw, c.getFrontFullName(true)); //name
        writeString(bw, getImageUrl(imagesDir, c.getImageId(true), true)); //imageUrl
        writeString(bw, cardBack); //cardBack
        writeString(bw, c.getTypeName()); //type
        writeString(bw, c.getSubtypeName()); //subtype
        writeString(bw, c.getPackName()); //packName
        writeInteger(bw, c.getDeckbuilderQuantity()); //deckbuilderQuantity
        writeString(bw, c.getPackCode()); //setUuid
        writeInteger(bw, c.getPosition(true)); //numberInPack
        writeString(bw, c.getEncounterName()); //encounterSet
        writeInteger(bw, c.getEncounterPosition()); //encounterNumber
        writeBoolean(bw, c.getIsUnique()); //unique
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
        writeBoolean(bw, c.isParallelContent()); //parallelContent
        writeString(bw, c.getCode()); //code
        writeInteger(bw, nvl(c.getTabooSetId(), 0)); //tabooId
        writeString(bw, nvl(meta.getTabooName(c.getTabooSetId()), "None")); //tabooName
        writeInteger(bw, nvl(c.getTabooXp(), 0)); //tabooXp
        writeBoolean(bw, c.getText() != null && c.getText().contains("[action]")); //action
        writeBoolean(bw, c.getText() != null && c.getText().contains("[reaction]")); //reaction
        writeBoolean(bw, c.getText() != null && (c.getText().contains("[free]") || c.getText().contains("[fast]"))); //free
        writeBoolean(bw, config.hasBonded(c)); //hasBonded
        writeString(bw, c.getText()); //text
        newLine(bw);
    }

    private void exportBackSide(File imagesDir, BufferedWriter bw, Card c) throws Exception {
        writeString(bw, c.getId()); //databaseId
        writeString(bw, c.getBackFullName(true)); //name
        writeString(bw, getImageUrl(imagesDir, c.getImageId(false), false)); //imageUrl
        writeString(bw, "multi_sided"); //cardBack
        writeString(bw, c.getTypeName()); //type
        writeString(bw, c.getSubtypeName()); //subtype
        writeString(bw, c.getPackName()); //packName
        writeInteger(bw, c.getDeckbuilderQuantity()); //deckbuilderQuantity
        writeString(bw, c.getPackCode()); //setUuid
        writeInteger(bw, c.getPosition(false)); //numberInPack
        writeString(bw, c.getEncounterName()); //encounterSet
        writeInteger(bw, c.getEncounterPosition()); //encounterNumber
        writeBoolean(bw, c.getIsUnique()); //unique
        writeBoolean(bw, c.getPermanent()); //permanent
        writeBoolean(bw, c.getExceptional()); //exceptional
        writeBoolean(bw, c.getMyriad()); //myriad
        writeString(bw, c.getFactions()); //faction
        writeString(bw, "Investigator".equals(c.getTypeName()) ? null : nvl(c.getBackTraits(), c.getTraits())); //traits
        writeString(bw, "B"); //side
        writeInteger(bw, null); //xp
        writeInteger(bw, null); //cost
        writeInteger(bw, null); //skillWillpower
        writeInteger(bw, null); //skillIntellect
        writeInteger(bw, null); //skillCombat
        writeInteger(bw, null); //skillAgility
        writeInteger(bw, null); //skillWild
        writeInteger(bw, null); //health
        writeBoolean(bw, false); //healthPerInvestigator
        writeInteger(bw, null); //sanity
        writeInteger(bw, null); //uses
        writeInteger(bw, null); //enemyDamage
        writeInteger(bw, null); //enemyHorror
        writeInteger(bw, null); //enemyFight
        writeInteger(bw, null); //enemyEvade
        writeInteger(bw, null); //shroud
        writeInteger(bw, null); //doom
        writeInteger(bw, null); //clues
        writeBoolean(bw, false); //cluesFixed
        writeInteger(bw, null); //victoryPoints
        writeInteger(bw, null); //vengeance
        writeInteger(bw, null); //stage
        writeBoolean(bw, c.isParallelContent()); //parallelContent
        writeString(bw, c.getCode()); //code
        writeInteger(bw, nvl(c.getTabooSetId(), 0)); //tabooId
        writeString(bw, nvl(meta.getTabooName(c.getTabooSetId()), "None")); //tabooName
        writeInteger(bw, nvl(c.getTabooXp(), 0)); //tabooXp
        writeBoolean(bw, c.getBackText() != null && c.getBackText().contains("[action]")); //action
        writeBoolean(bw, c.getBackText() != null && c.getBackText().contains("[reaction]")); //reaction
        writeBoolean(bw, c.getBackText() != null && (c.getBackText().contains("[free]") || c.getBackText().contains("[fast]"))); //free
        writeBoolean(bw, false); //hasBonded
        writeString(bw, c.getBackText()); //text
        newLine(bw);
    }

    private void exportLinked(File imagesDir, BufferedWriter bw, Card c, Card cc) throws Exception {
        writeString(bw, c.getId()); //databaseId: multi_sided must share
        writeString(bw, cc.getFrontFullName(true)); //name
        writeString(bw, getImageUrl(imagesDir, c.getImageId(false), false)); //imageUrl
        writeString(bw, "multi_sided"); //cardBack
        writeString(bw, cc.getTypeName()); //type
        writeString(bw, cc.getSubtypeName()); //subtype
        writeString(bw, cc.getPackName()); //packName
        writeInteger(bw, cc.getDeckbuilderQuantity()); //deckbuilderQuantity
        writeString(bw, cc.getPackCode()); //setUuid
        writeInteger(bw, cc.getPosition(false)); //numberInPack
        writeString(bw, cc.getEncounterName()); //encounterSet
        writeInteger(bw, cc.getEncounterPosition()); //encounterNumber
        writeBoolean(bw, cc.getIsUnique()); //unique
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
        writeBoolean(bw, c.isParallelContent()); //parallelContent
        writeString(bw, c.getCode()); //code
        writeInteger(bw, nvl(c.getTabooSetId(), 0)); //tabooId
        writeString(bw, nvl(meta.getTabooName(c.getTabooSetId()), "None")); //tabooName
        writeInteger(bw, nvl(c.getTabooXp(), 0)); //tabooXp
        writeBoolean(bw, cc.getText() != null && cc.getText().contains("[action]")); //action
        writeBoolean(bw, cc.getText() != null && cc.getText().contains("[reaction]")); //reaction
        writeBoolean(bw, cc.getText() != null && (cc.getText().contains("[free]") || cc.getText().contains("[fast]"))); //free
        writeBoolean(bw, false); //hasBonded
        writeString(bw, cc.getText()); //text
        newLine(bw);
    }

    private void exportCards(String predefinedPath, String path, String imagesPath) throws Exception {
        var file = new File(path);
        var imagesDir = new File(imagesPath);
        try (var fos = new FileOutputStream(file, false);
                var osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                var bw = new BufferedWriter(osw)) {
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
            writeString(bw, "parallelContent");
            writeString(bw, "code");
            writeString(bw, "tabooId");
            writeString(bw, "tabooName");
            writeString(bw, "tabooXp");
            writeString(bw, "action");
            writeString(bw, "reaction");
            writeString(bw, "free");
            writeString(bw, "hasBonded");
            writeString(bw, "text");
            newLine(bw);
            exportDefaultCards(imagesDir, bw, predefinedPath);
            for (var c : cards) {
                if (config.filter(c) == false) {
                    continue;
                }
                var doubleSided = c.getDoubleSided() != null && c.getDoubleSided();
                var linked = c.getBackLinkId() != null;
                if (doubleSided && linked) {
                    log("Double-sided and linked for %s", c.getId());
                }
                exportFrontSide(imagesDir, bw, c, doubleSided, linked);
                if (doubleSided) {
                    exportBackSide(imagesDir, bw, c);
                } else if (linked) {
                    var cc = cards.getCard(c.getBackLinkId());
                    if (cc == null) {
                        log("Missing linked %s for %s", c.getBackLinkId(), c.getId());
                    } else {
                        exportLinked(imagesDir, bw, c, cc);
                    }
                }
            }
            bw.flush();
        }
    }

    private void line(BufferedWriter bw, String s) throws Exception {
        bw.write(s);
        bw.newLine();
    }

    private boolean hasTrait(Card c, String[] traits) {
        if (c.getTraits() == null) {
            return false;
        }
        for (var trait : traits) {
            if (c.getTraits().contains(trait)) {
                return true;
            }
        }
        return false;
    }

    private void fillWeaknessMap(LinkedHashMap<String, ArrayList<String>> map, Card c, Integer qty, String... traits) {
        if (traits == null || traits.length == 0 || hasTrait(c, traits)) {
            var list = map.get(c.getPackCode());
            if (list == null) {
                list = new ArrayList<>();
                map.put(c.getPackCode(), list);
            }
            for (var i = 0; i < qty; i++) {
                list.add(c.getId());
            }
        }
    }

    private void generateWeaknessFunction(BufferedWriter bw, String name, LinkedHashMap<String, ArrayList<String>> map, boolean last) throws Exception {
        line(bw, String.format("        \"%s\": {", name));
        line(bw, "            \"args\": [\"$SET_UUID\"],");
        line(bw, "            \"code\": [");
        line(bw, String.format("                [\"VALIDATE_NOT_EMPTY\", \"$SET_UUID\", \"%s.SET_UUID\"],", name));
        line(bw, "                [\"COND\",");
        for (var e : map.entrySet()) {
            line(bw, String.format("                    [\"EQUAL\", \"$SET_UUID\", \"%s\"],", e.getKey()));
            line(bw, String.format("                    [\"LIST\", \"%s\"],", StringUtils.join(e.getValue(), "\", \"")));
        }
        line(bw, "                    [\"TRUE\"],");
        line(bw, "                    [\"LIST\"]");
        line(bw, "                ]");
        line(bw, "            ]");
        line(bw, String.format("        }%s", last ? "" : ","));
    }

    private void exportWeaknesses(String path) throws Exception {
        var file = new File(path);
        if (file.exists() == false) {
            file = new File("run/Core Weakness.json");
        }
        try (var fos = new FileOutputStream(file, false);
                var osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                var bw = new BufferedWriter(osw)) {
            var map = new LinkedHashMap<String, ArrayList<String>>();
            var mapMadnessInjuryPact = new LinkedHashMap<String, ArrayList<String>>();
            var mapMadnessPactCultistDetective = new LinkedHashMap<String, ArrayList<String>>();
            for (var c : cards) {
                if (config.filter(c) == false) {
                    continue;
                }
                if ("Basic Weakness".equals(c.getSubtypeName())) {
                    var qty = c.getDeckbuilderQuantity();
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

    private void exportBonded(String path) throws Exception {
        var file = new File(path);
        if (file.exists() == false) {
            file = new File("run/Core Bonded.json");
        }
        try (var fos = new FileOutputStream(file, false);
                var osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                var bw = new BufferedWriter(osw)) {
            var cardsWithBonded = new ArrayList<Card>();
            for (var c : cards) {
                if (config.filter(c) == false) {
                    continue;
                }
                if (config.hasBonded(c)) {
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
            for (var c : cardsWithBonded) {
                line(bw, String.format("                    [\"EQUAL\", \"$DATABASE_ID\", \"%s\"],", c.getId()));
                var bcs = config.getBonded(c);
                if (bcs.size() == 1) {
                    var bc = bcs.get(0);
                    line(bw, String.format("                    [\"DO_CREATE_MISSING_CARDS\", \"$TARGET_PLAYER\", \"%s\", \"%s\", %d, \"$TARGET_PLAYER_ASIDE\", true, null],", bc.getName().replace("\"", "\\\""), bc.getId(), bc.getQuantity()));
                } else {
                    line(bw, "                    [");
                    var size = bcs.size();
                    for (var i = 0; i < size; i++) {
                        var bc = bcs.get(i);
                        line(bw, String.format("                        [\"DO_CREATE_MISSING_CARDS\", \"$TARGET_PLAYER\", \"%s\", \"%s\", %d, \"$TARGET_PLAYER_ASIDE\", true, null]%s", bc.getName().replace("\"", "\\\""), bc.getId(), bc.getQuantity(), i + 1 == size ? "" : ","));
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

    private void exportMini(String path) throws Exception {
        var file = new File(path);
        if (file.exists() == false) {
            file = new File("run/Core Mini.json");
        }
        try (var fos = new FileOutputStream(file, false);
                var osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                var bw = new BufferedWriter(osw)) {
            line(bw, "{");
            line(bw, "    \"functions\": {");
            line(bw, "        \"GET_MINI_ID\": {");
            line(bw, "            \"args\": [\"$PREFIX\", \"$DATABASE_ID\"],");
            line(bw, "            \"code\": [");
            line(bw, "                [\"VALIDATE_NOT_EMPTY\", \"$PREFIX\", \"GET_MINI_ID.$PREFIX\"],");
            line(bw, "                [\"VALIDATE_NOT_EMPTY\", \"$DATABASE_ID\", \"GET_MINI_ID.DATABASE_ID\"],");
            line(bw, "                [\"COND\",");
            for (var c : cards) {
                if (c.getMiniCode() != null && c.getMiniImageId().equals(c.getId()) == false) {
                    line(bw, String.format("                    [\"EQUAL\", \"$DATABASE_ID\", \"%s\"],", c.getId()));
                    line(bw, String.format("                    \"{{$PREFIX}}%s\",", c.getMiniImageId()));
                }
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

    private boolean isTransfigurationCard(Card c) {
        return c != null
                && (c.getTabooSetId() == null || c.getTabooSetId() == 0)
                && c.getEncounterCode() == null
                && c.isParallel() == false
                && "Investigator".equals(c.getTypeName())
                && c.getDeckbuilderQuantity() > 0;
    }

    private String getTransfigurationCardName(Card c) {
        return String.format("%s (%s)", c.getName(), c.getId());
    }

    private LinkedHashMap<String, String> getTransfigurationNames() {
        var names = new LinkedHashMap<String, String>();
        for (var c : cards) {
            if (isTransfigurationCard(c)) {
                names.put(c.getId(), c.getName());
            }
        }
        return names;
    }

    private LinkedHashMap<String, String> getTransfigurationNamesReversed() {
        var names = new LinkedHashMap<String, String>();
        for (var c : cards) {
            if (isTransfigurationCard(c)) {
                var name = getTransfigurationCardName(c);
                if (names.get(name) == null) {
                    names.put(name, c.getId());
                }
            }
        }
        return names;
    }

    private boolean isRavenQuillCard(Card c) {
        return c != null
                && (c.getTabooSetId() == null || c.getTabooSetId() == 0)
                && "Asset".equals(c.getTypeName())
                && c.getTraits() != null
                && (c.getTraits().contains("Tome.") || c.getTraits().contains("Spell."));
    }

    private LinkedHashMap<String, String> getRavenQuillNames() {
        var names = new LinkedHashMap<String, String>();
        for (var c : cards) {
            if (isRavenQuillCard(c)) {
                names.put(c.getId(), c.getName());
            }
        }
        return names;
    }

    private TreeMap<String, String> getRavenQuillNamesReversed() {
        var names = new TreeMap<String, String>();
        for (var c : cards) {
            if (isRavenQuillCard(c) && names.get(c.getName()) == null) {
                names.put(c.getName(), c.getId());
            }
        }
        return names;
    }

    private void exportRavenQuill(String path) throws Exception {
        var file = new File(path);
        if (file.exists() == false) {
            file = new File("run/raven_quill.tsv");
        }
        try (var fos = new FileOutputStream(file, false);
                var osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                var bw = new BufferedWriter(osw)) {
            var ravenQuillNames = getRavenQuillNames();
            for (var e : ravenQuillNames.entrySet()) {
                line(bw, String.format("%s\t%s", e.getKey(), e.getValue()));
            }
            bw.flush();
        }
    }

    private void addTraitNames(Card c, Set<String> traits) {
        if (c != null && c.getTraits() != null) {
            var traitArray = c.getTraits().split("\\.");
            for (var trait : traitArray) {
                var t = trait.trim();
                if (t.length() > 0) {
                    traits.add(t);
                }
            }
        }
    }

    private TreeSet<String> getTraitNames() {
        var traits = new TreeSet<String>();
        for (var c : cards) {
            addTraitNames(c, traits);
        }
        return traits;
    }

    private void exportTraits(String path) throws Exception {
        var file = new File(path);
        if (file.exists() == false) {
            file = new File("run/traits.tsv");
        }
        try (var fos = new FileOutputStream(file, false);
                var osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                var bw = new BufferedWriter(osw)) {
            var traits = getTraitNames();
            for (var trait : traits) {
                line(bw, trait);
            }
            bw.flush();
        }
    }

    private TreeSet<String> getSkillNames() {
        var skills = new TreeSet<String>();
        skills.add("willpower");
        skills.add("intellect");
        skills.add("combat");
        skills.add("agility");
        return skills;
    }

    private void exportCustomizationGenerated(String path) throws Exception {
        var file = new File(path);
        if (file.exists() == false) {
            file = new File("run/Core Customization Generated.json");
        }
        try (var fos = new FileOutputStream(file, false);
                var osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                var bw = new BufferedWriter(osw)) {
            line(bw, "{");
            line(bw, "    \"functions\": {");
            var transfigurationNamesReversed = getTransfigurationNamesReversed();
            line(bw, "        \"GET_TRANSFIGURATION_OPTIONS_LIST\": {");
            line(bw, "            \"args\": [],");
            line(bw, "            \"code\": [");
            bw.write("                [\"LIST\"");
            bw.write(", \"None\", \"\"");
            for (var e : transfigurationNamesReversed.entrySet()) {
                bw.write(String.format(", \"%s\", \"%s\"", e.getKey().replace("\"", "\\\""), e.getValue().replace("\"", "\\\"")));
            }
            line(bw, "]");
            line(bw, "            ]");
            line(bw, "        },");
            var ravenQuillNamesReversed = getRavenQuillNamesReversed();
            line(bw, "        \"GET_RAVEN_QUILL_OPTIONS_LIST\": {");
            line(bw, "            \"args\": [],");
            line(bw, "            \"code\": [");
            bw.write("                [\"LIST\"");
            bw.write(", \"None\", \"\"");
            for (var e : ravenQuillNamesReversed.entrySet()) {
                bw.write(String.format(", \"%s\", \"%s\"", e.getKey().replace("\"", "\\\""), e.getValue().replace("\"", "\\\"")));
            }
            line(bw, "]");
            line(bw, "            ]");
            line(bw, "        },");
            var traits = getTraitNames();
            line(bw, "        \"GET_TRAIT_OPTIONS_LIST\": {");
            line(bw, "            \"args\": [],");
            line(bw, "            \"code\": [");
            bw.write("                [\"LIST\"");
            bw.write(", \"None\", \"\"");
            for (var e : traits) {
                bw.write(String.format(", \"%s\", \"%s\"", e.replace("\"", "\\\""), e.replace("\"", "\\\"")));
            }
            line(bw, "]");
            line(bw, "            ]");
            line(bw, "        },");
            var transfigurationNames = getTransfigurationNames();
            line(bw, "        \"GET_VALID_TRANSFIGURATION_CARD_NAME\": {");
            line(bw, "            \"args\": [\"$DATABASE_ID\"],");
            line(bw, "            \"code\": [");
            line(bw, "                [\"VALIDATE_NOT_NULL\", \"$DATABASE_ID\", \"GET_VALID_TRANSFIGURATION_CARD_NAME.DATABASE_ID\"],");
            line(bw, "                [\"COND\",");
            line(bw, "                    [\"EQUAL\", \"$DATABASE_ID\", \"\"],");
            line(bw, "                    \"?\",");
            line(bw, "                    [\"EQUAL\", \"$DATABASE_ID\", \" \"],");
            line(bw, "                    \"?\",");
            for (var e : transfigurationNames.entrySet()) {
                line(bw, String.format("                    [\"EQUAL\", \"$DATABASE_ID\", \"%s\"],", e.getKey().replace("\"", "\\\"")));
                line(bw, String.format("                    \"%s\",", e.getValue().replace("\"", "\\\"")));
            }
            line(bw, "                    [\"TRUE\"],");
            line(bw, "                    \"?\"");
            line(bw, "                ]");
            line(bw, "            ]");
            line(bw, "        },");
            var ravenQuillNames = getRavenQuillNames();
            line(bw, "        \"GET_VALID_RAVEN_QUILL_CARD_NAME\": {");
            line(bw, "            \"args\": [\"$DATABASE_ID\"],");
            line(bw, "            \"code\": [");
            line(bw, "                [\"VALIDATE_NOT_NULL\", \"$DATABASE_ID\", \"GET_VALID_RAVEN_QUILL_CARD_NAME.DATABASE_ID\"],");
            line(bw, "                [\"COND\",");
            line(bw, "                    [\"EQUAL\", \"$DATABASE_ID\", \"\"],");
            line(bw, "                    \"?\",");
            line(bw, "                    [\"EQUAL\", \"$DATABASE_ID\", \" \"],");
            line(bw, "                    \"?\",");
            for (var e : ravenQuillNames.entrySet()) {
                line(bw, String.format("                    [\"EQUAL\", \"$DATABASE_ID\", \"%s\"],", e.getKey().replace("\"", "\\\"")));
                line(bw, String.format("                    \"%s\",", e.getValue().replace("\"", "\\\"")));
            }
            line(bw, "                    [\"TRUE\"],");
            line(bw, "                    \"?\"");
            line(bw, "                ]");
            line(bw, "            ]");
            line(bw, "        },");
            line(bw, "        \"GET_VALID_TRAIT_NAME\": {");
            line(bw, "            \"args\": [\"$TRAIT_NAME\"],");
            line(bw, "            \"code\": [");
            line(bw, "                [\"VALIDATE_NOT_NULL\", \"$TRAIT_NAME\", \"GET_VALID_TRAIT_NAME.TRAIT_NAME\"],");
            line(bw, "                [\"COND\",");
            line(bw, "                    [\"EQUAL\", \"$TRAIT_NAME\", \"\"],");
            line(bw, "                    \"?\",");
            line(bw, "                    [\"EQUAL\", \"$TRAIT_NAME\", \" \"],");
            line(bw, "                    \"?\",");
            for (var trait : traits) {
                line(bw, String.format("                    [\"EQUAL\", \"$TRAIT_NAME\", \"%s\"],", trait.replace("\"", "\\\"")));
                line(bw, String.format("                    \"%s\",", trait.replace("\"", "\\\"")));
            }
            line(bw, "                    [\"TRUE\"],");
            line(bw, "                    \"?\"");
            line(bw, "                ]");
            line(bw, "            ]");
            line(bw, "        },");
            line(bw, "        \"GET_VALID_SKILL_NAME\": {");
            line(bw, "            \"args\": [\"$SKILL_NAME\"],");
            line(bw, "            \"code\": [");
            line(bw, "                [\"VALIDATE_NOT_NULL\", \"$SKILL_NAME\", \"GET_VALID_SKILL_NAME.SKILL_NAME\"],");
            line(bw, "                [\"COND\",");
            line(bw, "                    [\"EQUAL\", \"$SKILL_NAME\", \"\"],");
            line(bw, "                    \"?\",");
            line(bw, "                    [\"EQUAL\", \"$SKILL_NAME\", \" \"],");
            line(bw, "                    \"?\",");
            var skills = getSkillNames();
            for (var skill : skills) {
                line(bw, String.format("                    [\"EQUAL\", \"$SKILL_NAME\", \"%s\"],", skill.replace("\"", "\\\"")));
                line(bw, String.format("                    \"%s\",", skill.replace("\"", "\\\"")));
            }
            line(bw, "                    [\"TRUE\"],");
            line(bw, "                    \"?\"");
            line(bw, "                ]");
            line(bw, "            ]");
            line(bw, "        }");
            line(bw, "    }");
            line(bw, "}");
            bw.flush();
        }
    }

    private LinkedHashMap<String, ArrayList<Card>> getCardsWithErrata() {
        var map = new LinkedHashMap<String, ArrayList<Card>>();
        for (var c : cards) {
            if (c.getTabooSetId() != null && c.getTabooSetId() > 0) {
                var list = map.get(c.getCode());
                if (list == null) {
                    list = new ArrayList<>();
                    map.put(c.getCode(), list);
                }
                list.add(0, c);
            }
        }
        return map;
    }

    private void exportTaboo(String path) throws Exception {
        var file = new File(path);
        if (file.exists() == false) {
            file = new File("run/Core Taboo.json");
        }
        try (var fos = new FileOutputStream(file, false);
                var osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                var bw = new BufferedWriter(osw)) {
            line(bw, "{");
            line(bw, "    \"functions\": {");
            line(bw, "        \"GET_DATABASE_ID_FOR_TABOO\": {");
            line(bw, "            \"args\": [\"$CODE\", \"$TABOO_VALUE\"],");
            line(bw, "            \"code\": [");
            line(bw, "                [\"VALIDATE_NOT_EMPTY\", \"$CODE\", \"GET_DATABASE_ID_FOR_TABOO.CODE\"],");
            line(bw, "                [\"VALIDATE_GE0\", \"$TABOO_VALUE\", \"GET_DATABASE_ID_FOR_TABOO.TABOO_VALUE\"],");
            line(bw, "                [\"COND\",");
            var map = getCardsWithErrata();
            for (var e : map.entrySet()) {
                line(bw, "                    [\"EQUAL\", \"$CODE\", \"%s\"],".formatted(e.getKey()));
                line(bw, "                    [\"COND\",");
                for (var c : e.getValue()) {
                    line(bw, "                        [\"GREATER_EQUAL\", \"$TABOO_VALUE\", %d],".formatted(c.getTabooSetId()));
                    line(bw, "                        \"%s\",".formatted(c.getId()));
                }
                line(bw, "                        [\"TRUE\"],");
                line(bw, "                        \"%s\"".formatted(e.getKey()));
                line(bw, "                    ],");
            }
            line(bw, "                    [\"TRUE\"],");
            line(bw, "                    \"{{$CODE}}\"");
            line(bw, "                ]");
            line(bw, "            ]");
            line(bw, "        }");
            line(bw, "    }");
            line(bw, "}");
            bw.flush();
        }
    }

    private void testImages(String language, String imagesPath) throws Exception {
        var imagesDir = new File(imagesPath);
        var languageDir = new File(imagesDir, language);
        if (languageDir.exists() == false) {
            log("Missing language %s", language);
            return;
        }
        var base = imagesDir.getAbsoluteFile().getCanonicalPath();
        var sourceFiles = new HashSet<String>();
        var originalFiles = FileUtils.listFiles(imagesDir, new String[]{"webp"}, true);
        for (var f : originalFiles) {
            var relativePath = f.getAbsoluteFile().getCanonicalPath().replace(base, "");
            if (config.isIgnoredPath(relativePath) == false) {
                sourceFiles.add(relativePath);
            }
        }
        base = languageDir.getAbsoluteFile().getCanonicalPath();
        var languageFiles = FileUtils.listFiles(languageDir, new String[]{"webp"}, true);
        var unwanted = new ArrayList<String>();
        for (var f : languageFiles) {
            var relativePath = f.getAbsoluteFile().getCanonicalPath().replace(base, "");
            if (sourceFiles.remove(relativePath) == false) {
                unwanted.add(relativePath);
            }
        }
        var missing = new ArrayList<>(sourceFiles);
        if (missing.isEmpty() == false) {
            log("Missing %s files:", language);
            missing.sort(null);
            for (var s : missing) {
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
        cards = Cards.loadCards();
        config = cards.getConfiguration();
        meta = cards.getMetadata();
        exportCards("run/predefined.xlsx", "run/arkhamhorrorlcg.tsv", "../../cards/arkham/dragncards-arkhamhorrorlcg-plugin/images");
        exportWeaknesses("../../cards/arkham/dragncards-arkhamhorrorlcg-plugin/jsons/Core Weakness.json");
        exportBonded("../../cards/arkham/dragncards-arkhamhorrorlcg-plugin/jsons/Core Bonded.json");
        exportMini("../../cards/arkham/dragncards-arkhamhorrorlcg-plugin/jsons/Core Mini.json");
        exportRavenQuill("../dragncards-arkhamhorrorlcg-php/raven_quill.tsv");
        exportTraits("../dragncards-arkhamhorrorlcg-php/traits.tsv");
        exportCustomizationGenerated("../../cards/arkham/dragncards-arkhamhorrorlcg-plugin/jsons/Core Customization Generated.json");
        exportTaboo("../../cards/arkham/dragncards-arkhamhorrorlcg-plugin/jsons/Core Taboo.json");
        //testImages("es", "../../cards/arkham/dragncards-arkhamhorrorlcg-plugin/images");
    }

    public static void main(String[] args) {
        try {
            new MainExportArkhamDB().run();
        } catch (Exception ex) {
            log(ex);
        }
    }

}
