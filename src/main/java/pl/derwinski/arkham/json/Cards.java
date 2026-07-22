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
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import pl.derwinski.arkham.Language;
import pl.derwinski.arkham.Util;
import static pl.derwinski.arkham.Util.log;
import pl.derwinski.arkham.json.configuration.Configuration;
import pl.derwinski.arkham.json.metadata.Metadata;

/**
 *
 * @author morvael
 */
public final class Cards implements Iterable<Card> {

    private static final HashSet<String> unhandled = new HashSet<>();

    public static Cards loadCards(Language lng) throws Exception {
        return loadCards(lng, Configuration.loadConfiguration(), Metadata.loadMetadata(lng));
    }

    public static Cards loadCards(Language lng, Configuration configuration, Metadata metadata) throws Exception {
        var symbol = lng.name().toLowerCase();
        Util.downloadIfOld("https://api.arkham.build/v1/cache/cards/%s".formatted(symbol), "run/cards_%s.json".formatted(symbol));
        return loadCards(configuration, metadata, "run/cards_%s.json".formatted(symbol));
    }

    public static Cards loadCards(Configuration configuration, Metadata metadata, String path) throws Exception {
        var file = new File(path);
        var c = new JsonMapper().readTree(file).findValue("data");
        if (c != null) {
            return loadCards(configuration, metadata, c);
        } else {
            log("Error reading Cards file");
            return null;
        }
    }

    public static Cards loadCards(Configuration configuration, Metadata metadata, JsonNode c) throws Exception {
        if (c.isObject()) {
            var o = new Cards(configuration, metadata);
            var it = c.fieldNames();
            while (it.hasNext()) {
                var fieldName = it.next();
                switch (fieldName) {
                    case "all_card":
                        o.cards = Collections.unmodifiableList(Card.readCards(configuration, metadata, c.get(fieldName)));
                        break;
                    default:
                        if (unhandled.add(fieldName)) {
                            log("Unhandled field name in Cards: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType());
                        }
                        break;
                }
            }
            for (var card : o.cards) {
                o.map.put(card.getId(), card);
            }
            for (var card : o.cards) {
                if (card.getBackLinkId() != null) {
                    var backCard = o.map.get(card.getBackLinkId());
                    if (backCard != null) {
                        if (configuration.isFlipped(backCard)) {
                            backCard.flip(card);
                            o.map.put(card.getId(), card);
                            o.map.put(backCard.getId(), backCard);
                        } else if (configuration.isFlipped(card)) {
                            card.flip(backCard);
                            o.map.put(card.getId(), card);
                            o.map.put(backCard.getId(), backCard);
                        } else {
                            backCard.hide();
                        }
                    }
                }
            }
            return o;
        } else {
            if (c.isNull() == false) {
                log("Error reading Cards object: %s", c.asText());
            }
            return null;
        }
    }

    private final Configuration configuration;
    private final Metadata metadata;
    private final HashMap<String, Card> map = new HashMap<>();

    private List<Card> cards;

    private Cards(Configuration configuration, Metadata metadata) {
        this.configuration = configuration;
        this.metadata = metadata;
    }

    public List<Card> getCards() {
        return cards;
    }

    @Override
    public Iterator<Card> iterator() {
        return cards.iterator();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public Card getCard(String id) {
        return map.get(id);
    }

}
