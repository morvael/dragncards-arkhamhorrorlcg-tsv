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
package pl.derwinski.arkham.tts;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.luciad.imageio.webp.WebPWriteParam;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import org.apache.commons.io.FileUtils;
import org.imgscalr.Scalr;
import pl.derwinski.arkham.Util;
import static pl.derwinski.arkham.Util.download;
import static pl.derwinski.arkham.Util.log;
import static pl.derwinski.arkham.Util.readBoolean;
import static pl.derwinski.arkham.Util.readInteger;
import static pl.derwinski.arkham.Util.readStringList;
import static pl.derwinski.arkham.Util.readStringRaw;
import pl.derwinski.arkham.tts.json.Card;
import pl.derwinski.arkham.tts.json.CustomDeck;
import pl.derwinski.arkham.tts.json.CustomDecks;
import pl.derwinski.arkham.tts.json.GMNotes;

/**
 * Console program to parse TTS Mod repositories, read card data from there,
 * download tiled images and generate individual card images from them.
 *
 * @author morvael
 */
public class MainExportTTS {

    protected final HashMap<String, String> commonBacks;
    protected final HashSet<String> falsePositive;
    protected final HashSet<String> miniCards;
    protected final HashSet<String> skipOverrides;
    protected final HashSet<String> excluded;

    protected final HashSet<String> unhandledCustomDeck = new HashSet<>();

    protected ImageWriter writer;
    protected WebPWriteParam writeParam;

    public MainExportTTS() throws Exception {
        commonBacks = Util.readConfigMap("run/commonBacks.txt");
        falsePositive = Util.readConfigSet("run/falsePositive.txt");
        miniCards = Util.readConfigSet("run/miniCards.txt");
        skipOverrides = Util.readConfigSet("run/skipOverrides.txt");
        excluded = Util.readConfigSet("run/excluded.txt");
    }

    protected CustomDeck readCustomDeck(String id, JsonNode c) throws Exception {
        if (c.isObject()) {
            CustomDeck customDeck = new CustomDeck();
            customDeck.setId(id);
            Iterator<String> it = c.fieldNames();
            while (it.hasNext()) {
                String fieldName = it.next();
                switch (fieldName) {
                    case "FaceURL":
                        customDeck.setFaceURL(readStringRaw(c.get(fieldName)));
                        break;
                    case "BackURL":
                        customDeck.setBackURL(readStringRaw(c.get(fieldName)));
                        break;
                    case "NumWidth":
                        customDeck.setNumWidth(readInteger(c.get(fieldName)));
                        break;
                    case "NumHeight":
                        customDeck.setNumHeight(readInteger(c.get(fieldName)));
                        break;
                    case "BackIsHidden":
                        customDeck.setBackIsHidden(readBoolean(c.get(fieldName)));
                        break;
                    case "UniqueBack":
                        customDeck.setUniqueBack(readBoolean(c.get(fieldName)));
                        break;
                    case "Type":
                        customDeck.setType(readInteger(c.get(fieldName)));
                        break;
                    default:
                        if (unhandledCustomDeck.add(fieldName)) {
                            log("Unhandled field name in CustomDeck: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType());
                        }
                        break;
                }
            }
            return customDeck;
        } else {
            if (c.isNull() == false) {
                log("Error reading CustomDeck field: %s", c.asText());
            }
            return null;
        }
    }

    protected CustomDecks readCustomDecks(JsonNode c) throws Exception {
        if (c.isObject()) {
            CustomDecks customDecks = new CustomDecks();
            customDecks.setMap(new LinkedHashMap<>());
            Iterator<String> it = c.fieldNames();
            while (it.hasNext()) {
                String fieldName = it.next();
                customDecks.getMap().put(fieldName, readCustomDeck(fieldName, c.get(fieldName)));
            }
            return customDecks;
        } else {
            if (c.isNull() == false) {
                log("Error reading CustomDecks field: %s", c.asText());
            }
            return null;
        }
    }

    protected GMNotes readGMNotes(JsonNode c) throws Exception {
        if (c.isObject()) {
            GMNotes gmNotes = new GMNotes();
            Iterator<String> it = c.fieldNames();
            while (it.hasNext()) {
                String fieldName = it.next();
                switch (fieldName) {
                    case "id":
                        gmNotes.setId(readStringRaw(c.get(fieldName)));
                        break;
                    case "alternate_ids":
                        gmNotes.setAlternateIds(readStringList(c.get(fieldName)));
                        break;
                    case "type":
                        gmNotes.setType(readStringRaw(c.get(fieldName)));
                        break;
                    default:
                        //other fields are ignored
                        break;
                }
            }
            return gmNotes;
        } else if (c instanceof MissingNode) {
            return null;
        } else {
            if (c.isNull() == false) {
                log("Error reading GMNotes field: %s", c.asText());
            }
            return null;
        }
    }

    protected File getExistingFile(File dir, String path) {
        do {
            File f = new File(dir, path);
            if (f.exists()) {
                return f;
            }
            dir = dir.getParentFile();
        } while (dir != null);
        return null;
    }

    protected Card readCard(JsonMapper mapper, File file, CustomDecks parentCustomDecks, JsonNode c, String state) throws Exception {
        if (c.isObject()) {
            Card card = new Card();
            card.setDescription("");
            card.setSidewaysCard(false);
            card.setFile(file);
            Iterator<String> it = c.fieldNames();
            while (it.hasNext()) {
                String fieldName = it.next();
                switch (fieldName) {
                    case "CardID":
                        card.setCardID(readStringRaw(c.get(fieldName)));
                        break;
                    case "Nickname":
                        card.setNickname(readStringRaw(c.get(fieldName)));
                        break;
                    case "Description":
                        card.setDescription(readStringRaw(c.get(fieldName)));
                        break;
                    case "SidewaysCard":
                        card.setSidewaysCard(readBoolean(c.get(fieldName)));
                        break;
                    case "GMNotes":
                        try {
                            card.setGMNotes(readGMNotes(mapper.readTree(readStringRaw(c.get(fieldName)))));
                        } catch (JsonParseException ex) {
                            log("Error parsing JSON from Card.GMNotes field \"%s\" in %s (%s)", readStringRaw(c.get(fieldName)), file.getAbsoluteFile().getCanonicalPath(), ex.getMessage());
                        }
                        break;
                    case "GMNotes_path":
                        try {
                            File f = getExistingFile(file.getParentFile(), c.get(fieldName).textValue());
                            card.setGMNotes(readGMNotes(mapper.readTree(FileUtils.readFileToString(f, StandardCharsets.UTF_8))));
                        } catch (Exception ex) {
                            log("Error parsing JSON from Card.GMNotes_path field \"%s\" in %s (%s)", c.get(fieldName).textValue(), file.getAbsoluteFile().getCanonicalPath(), ex.getMessage());
                        }
                        break;
                    case "CustomDeck":
                        card.setCustomDecks(readCustomDecks(c.get(fieldName)));
                        break;
                    default:
                        //other fields are ignored
                        break;
                }
            }
            if (card.getCustomDecks() == null) {
                card.setCustomDecks(parentCustomDecks);
            }
            if (card.getGMNotes() != null && card.getGMNotes().getAlternateIds() != null && card.getGMNotes().getAlternateIds().isEmpty() == false) {
                ArrayList<String> alternateIds = card.getGMNotes().getAlternateIds();
                String promoId = null;
                String revisedId = null;
                for (String id : alternateIds) {
                    if (id.startsWith("9")) {
                        promoId = id;
                    } else {
                        revisedId = id;
                    }
                }
                if ("3".equals(state)) {
                    if (promoId != null) {
                        card.getGMNotes().setId(promoId);
                    }
                } else if ("2".equals(state)) {
                    if (revisedId != null) {
                        card.getGMNotes().setId(revisedId);
                    } else if (promoId != null) {
                        card.getGMNotes().setId(promoId);
                    }
                }
            }
            return card;
        } else {
            if (c.isNull() == false) {
                log("Error reading Card field: %s", c.asText());
            }
            return null;
        }
    }

    protected void searchForCards(JsonMapper mapper, File file, ArrayList<Card> result, int[] count, CustomDecks parentCustomDecks, JsonNode node, String state) throws Exception {
        if (node.isObject()) {
            JsonNode name = node.get("Name");
            if (name != null && name.isTextual() && ("Card".equals(name.textValue()) || "CardCustom".equals(name.textValue()))) {
                Card card = readCard(mapper, file, parentCustomDecks, node, state);
                if (card.isValid()) {
                    if (card.isSkippable() == false) {
                        result.add(card);
                    }
                    count[0]++;
                } else {
                    count[1]++;
                    log("Invalid card (%s) in %s", card.toString(), file.getAbsoluteFile().getCanonicalPath());
                }
            }
            JsonNode containedObjects = node.get("ContainedObjects");
            if (containedObjects != null && containedObjects.isArray()) {
                CustomDecks customDecks = null;
                JsonNode customDeck = node.get("CustomDeck");
                if (customDeck != null) {
                    customDecks = readCustomDecks(customDeck);
                }
                int size = containedObjects.size();
                for (int i = 0; i < size; i++) {
                    searchForCards(mapper, file, result, count, customDecks, containedObjects.get(i), null);
                }
            }
            JsonNode states = node.get("States");
            if (states != null && states.isObject()) {
                Iterator<String> it = states.fieldNames();
                while (it.hasNext()) {
                    String fieldName = it.next();
                    searchForCards(mapper, file, result, count, parentCustomDecks, states.get(fieldName), fieldName);
                }
            }
        }
    }

    protected void loadTTSCards(JsonMapper mapper, File file, ArrayList<Card> result) throws Exception {
        int[] count = new int[]{0, 0};
        searchForCards(mapper, file, result, count, null, mapper.readTree(file), null);
        if (count[0] == 0 && count[1] > 0) {
            log("No valid cards in %s", file.getAbsoluteFile().getCanonicalPath());
        }
    }

    protected boolean isExcluded(File f) throws IOException {
        String s = f.getAbsoluteFile().getCanonicalPath();
        for (String ex : excluded) {
            if (s.contains(ex)) {
                return true;
            }
        }
        return false;
    }

    protected ArrayList<CardSide> loadTTSCards(String... paths) throws Exception {
        ArrayList<CardSide> result = new ArrayList<>();
        if (paths != null && paths.length > 0) {
            //load everything from every path
            JsonFactory factory = new JsonFactoryBuilder()
                    .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
                    .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
                    .build();
            JsonMapper mapper = new JsonMapper(factory);
            ArrayList<Card> tmpList = new ArrayList<>();
            for (String path : paths) {
                File file = new File(path);
                if (file.exists()) {
                    if (file.isDirectory()) {
                        Collection<File> list = FileUtils.listFiles(file, new String[]{"json"}, true);
                        if (list != null && list.isEmpty() == false) {
                            for (File f : list) {
                                if (isExcluded(f) == false) {
                                    loadTTSCards(mapper, f, tmpList);
                                }
                            }
                        }
                    } else if (file.isFile() && isExcluded(file) == false) {
                        loadTTSCards(mapper, file, tmpList);
                    }
                }
            }
            //group by keys (same output images)
            LinkedHashMap<String, ArrayList<CardSide>> keyMap = new LinkedHashMap<>();
            for (Card c : tmpList) {
                String faceKey = c.getFaceKey();
                ArrayList<CardSide> list = keyMap.get(faceKey);
                if (list == null) {
                    list = new ArrayList<>();
                    keyMap.put(faceKey, list);
                }
                list.add(new CardSide(c, false));

                String backKey = c.getBackKey();
                if (commonBacks.containsKey(backKey) == false) {
                    list = keyMap.get(backKey);
                    if (list == null) {
                        list = new ArrayList<>();
                        keyMap.put(backKey, list);
                    }
                    list.add(new CardSide(c, true));
                }
            }
            //analyze and remove unwanted cards
            for (Map.Entry<String, ArrayList<CardSide>> e : keyMap.entrySet()) {
                //if there's at least one card with gmnotes id,
                //remove all cards without gmnotes id and all cards sharing the same gmnotes id, except one
                HashSet<String> ids = new HashSet<>();
                for (CardSide cs : e.getValue()) {
                    if (cs.getCard().getGMNotesId() != null) {
                        ids.add(cs.getCard().getGMNotesId());
                    }
                }
                if (ids.isEmpty() == false) {
                    ids.clear();
                    Iterator<CardSide> it = e.getValue().iterator();
                    while (it.hasNext()) {
                        CardSide cs = it.next();
                        if (cs.getCard().getGMNotesId() == null || ids.add(cs.getCard().getGMNotesId()) == false) {
                            it.remove();
                        }
                    }
                }
                //remove all backs same as faces
                HashSet<String> faceKeys = new HashSet<>();
                HashSet<String> backKeys = new HashSet<>();
                for (CardSide cs : e.getValue()) {
                    if (cs.isBack() == false) {
                        faceKeys.add(cs.getCard().getFaceKey());
                    } else {
                        backKeys.add(cs.getCard().getBackKey());
                    }
                }
                if (faceKeys.isEmpty() == false && backKeys.isEmpty() == false) {
                    backKeys.retainAll(faceKeys);
                    if (backKeys.isEmpty() == false) {
                        Iterator<CardSide> it = e.getValue().iterator();
                        while (it.hasNext()) {
                            CardSide cs = it.next();
                            if (cs.isBack() && backKeys.contains(cs.getCard().getBackKey())) {
                                it.remove();
                            }
                        }
                    }
                }
                //if there's at least one card with card id + id,
                //remove all cards sharing the same card id + id, except one
                HashSet<String> cardIds = new HashSet<>();
                for (CardSide cs : e.getValue()) {
                    cardIds.add(String.format("%s_%s", cs.getCard().getCardID(), cs.getCard().getGMNotesId()));
                }
                if (cardIds.isEmpty() == false) {
                    cardIds.clear();
                    Iterator<CardSide> it = e.getValue().iterator();
                    while (it.hasNext()) {
                        CardSide cs = it.next();
                        if (cardIds.add(String.format("%s_%s", cs.getCard().getCardID(), cs.getCard().getGMNotesId())) == false) {
                            it.remove();
                        }
                    }
                }
                if (e.getValue().size() > 1 && falsePositive.contains(e.getKey()) == false) {
                    log("Multiple card sides (%d) for %s", e.getValue().size(), e.getKey());
                    for (CardSide cs : e.getValue()) {
                        log(">> %s: %s", cs.isBack() ? "B" : "F", cs.getCard());
                    }
                }
                if (e.getValue().size() > 1) {
                    e.getValue().sort(null);
                }
                result.addAll(e.getValue());
            }
        }
        return result;
    }

    protected void downloadTTSImages(ArrayList<CardSide> cardSides, File tmpDir) throws Exception {
        for (CardSide cs : cardSides) {
            CustomDeck cd = cs.getCard().getCustomDeck();
            if (cd != null) {
                if (cs.isBack() == false) {
                    download(cd.getFaceURL(), new File(tmpDir, cd.getFaceKey()));
                } else {
                    download(cd.getBackURL(), new File(tmpDir, cd.getBackKey()));
                }
            }
        }
    }

    protected final HashMap<String, SoftReference<BufferedImage>> imageCache = new HashMap<>();

    protected BufferedImage readImage(File inFile) throws Exception {
        String key = inFile.getAbsoluteFile().getCanonicalPath();
        BufferedImage bi = null;
        SoftReference<BufferedImage> ref = imageCache.get(key);
        if (ref != null) {
            bi = ref.get();
        }
        if (bi == null) {
            bi = ImageIO.read(inFile);
            imageCache.put(key, new SoftReference<>(bi));
        }
        return bi;
    }

    protected void exportTTSCard(Card c, CustomDeck cd, File inFile, File outFile, int resX, int resY, boolean back) throws Exception {
        try {
            if (outFile.exists()) {
                if (skipOverrides.contains(outFile.getName())) {
                    return;
                }
                String replace = ".webp";
                int number = 1;
                while (outFile.exists()) {
                    String newReplace = String.format("-v%d.webp", number++);
                    outFile = new File(outFile.getParentFile(), outFile.getName().replace(replace, newReplace));
                    replace = newReplace;
                }
                if (number > 1) {
                    log("Override protection for %s: %s", back ? c.getBackKey() : c.getFaceKey(), outFile.getName());
                }
            }
            @SuppressWarnings("null")
            int numWidth = cd.getNumWidth() != null ? cd.getNumWidth() : 1;
            @SuppressWarnings("null")
            int numHeight = cd.getNumHeight() != null ? cd.getNumHeight() : 1;
            int index = Integer.parseInt(c.getCardID().substring(c.getCardID().length() - 2));
            if (back && cd.getUniqueBack() != null && cd.getUniqueBack() == false) {
                numWidth = 1;
                numHeight = 1;
                index = 0;
            }
            int size = numWidth * numHeight;
            BufferedImage bi = readImage(inFile);
            int tileWidth = bi.getWidth() / numWidth;
            int tileHeight = bi.getHeight() / numHeight;
            if (index < size) {
                int tileY = index / numWidth;
                int tileX = index - (tileY * numWidth);
                BufferedImage bi2 = Scalr.crop(bi, tileX * tileWidth, tileY * tileHeight, tileWidth, tileHeight);
                bi2 = Scalr.resize(bi2, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_EXACT, resX, resY);
                if (c.getSidewaysCard() != null && c.getSidewaysCard()) {
                    bi2 = Scalr.rotate(bi2, Scalr.Rotation.CW_270);
                }
                if (outFile.getParentFile().exists() == false) {
                    outFile.getParentFile().mkdirs();
                }
                if (writer == null) {
                    writer = ImageIO.getImageWritersByMIMEType("image/webp").next();
                }
                if (writeParam == null) {
                    writeParam = new WebPWriteParam(writer.getLocale());
                    writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    writeParam.setCompressionType("Lossy");
                    writeParam.setCompressionQuality(0.95f);
                }
                try (FileImageOutputStream fios = new FileImageOutputStream(outFile)) {
                    writer.setOutput(fios);
                    writer.write(null, new IIOImage(bi2, null, null), writeParam);
                }
            }
        } catch (Exception ex) {
            log("Exception during exportTTSCard for %s: inFile %s, outFile %s", c.getCardID(), inFile.getName(), outFile.getName());
            log(ex);
        }
    }

    protected void exportTTSCards(ArrayList<CardSide> cardSides, File tmpDir) throws Exception {
        File sortedDir = new File(tmpDir, "sorted");
        File unsortedDir = new File(tmpDir, "unsorted");

        for (CardSide cs : cardSides) {
            Card c = cs.getCard();
            CustomDeck cd = c.getCustomDeck();
            File inFile = new File(tmpDir, cs.isBack() ? cd.getBackKey() : cd.getFaceKey());
            String databaseId = c.getGMNotesId();
            String type = c.getGMNotesType();
            String side;
            if (type != null && type.contains("location")) {
                side = cs.isBack() ? "a" : "b";
            } else {
                side = cs.isBack() ? "b" : "a";
            }
            File outFile;
            int resX = 750;
            int resY = 1050;
            if (databaseId != null) {
                //Hank Samson bonded card - override id and output only fronts of those cards
                if (databaseId.equals("10015-b1")) {
                    if (cs.isBack()) {
                        continue;
                    }
                    databaseId = "10016";
                    side = "a";
                } else if (databaseId.equals("10015-b2")) {
                    if (cs.isBack()) {
                        continue;
                    }
                    databaseId = "10016";
                    side = "b";
                }
                if (databaseId.equals("88023")) {
                    if (cs.isBack()) {
                        continue;
                    }
                    if ("The Heist".equals(c.getNickname())) {
                        side = "b";
                    }
                }
                if (databaseId.endsWith("-m")) {
                    //mini investigator
                    resX = 484;
                    resY = 744;
                    outFile = new File(sortedDir, String.format("mini_investigators/%s%s.webp", databaseId.replace("-m", ""), side));
                } else if (databaseId.endsWith("-t-c")) {
                    outFile = new File(sortedDir, String.format("customizable/t%s%s.webp", databaseId.replace("-t-c", ""), side));
                } else if (databaseId.endsWith("-c")) {
                    outFile = new File(sortedDir, String.format("customizable/%s%s.webp", databaseId.replace("-c", ""), side));
                } else if (databaseId.endsWith("-t")) {
                    outFile = new File(sortedDir, String.format("taboo/t%s%s.webp", databaseId.replace("-t", ""), side));
                } else if (databaseId.endsWith("-p")) {
                    outFile = new File(sortedDir, String.format("paralell/%s%s.webp", databaseId.replace("-p", ""), side));
                } else {
                    outFile = new File(sortedDir, String.format("card_images/%s%s.webp", databaseId, side));
                }
            } else {
                if (miniCards.contains(inFile.getName())) {
                    resX = 484;
                    resY = 744;
                }
                outFile = new File(unsortedDir, String.format("%s/%s%s_%s.webp", c.getFile().getName().replace(".json", ""), c.getCardID(), side, cs.isBack() ? c.getBackKey() : c.getFaceKey()));
            }
            exportTTSCard(c, cd, inFile, outFile, resX, resY, cs.isBack());
        }
    }

    public void run() throws Exception {
        if (Util.checkExistence("run/repos/SCED", "Clone https://github.com/argonui/SCED or its fork into %s.")
                && Util.checkExistence("run/repos/SCED-downloads", "Clone https://github.com/Chr1Z93/SCED-downloads or its fork into %s.")) {
            ArrayList<CardSide> cardSides = loadTTSCards("run/repos/SCED/objects/AllPlayerCards.15bb07",
                    "run/repos/SCED-downloads/decomposed/campaign",
                    "run/repos/SCED-downloads/decomposed/scenario");
            log("Loaded %d TTS card sides", cardSides.size());
            File tmpDir = new File("run/tts");
            downloadTTSImages(cardSides, tmpDir);
            exportTTSCards(cardSides, tmpDir);
        }
    }

    public static void main(String[] args) {
        try {
            new MainExportTTS().run();
        } catch (Exception ex) {
            log(ex);
        }
    }

}
