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
import java.util.LinkedHashMap;
import pl.derwinski.arkham.Copyable;
import pl.derwinski.arkham.Util;

/**
 *
 * @author morvael
 */
public class DeckRequirements implements Copyable<DeckRequirements> {

    private Integer size;
    private LinkedHashMap<String, LinkedHashMap<String, String>> card;
    private ArrayList<DeckRequirementsRandom> random;

    public DeckRequirements() {

    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getCard() {
        return card;
    }

    public void setCard(LinkedHashMap<String, LinkedHashMap<String, String>> card) {
        this.card = card;
    }

    public ArrayList<DeckRequirementsRandom> getRandom() {
        return random;
    }

    public void setRandom(ArrayList<DeckRequirementsRandom> random) {
        this.random = random;
    }

    @Override
    public DeckRequirements copy() {
        DeckRequirements o = new DeckRequirements();
        o.size = size;
        o.card = Util.simpleMapMapCopy(card);
        o.random = Util.copy(random);
        return o;
    }

}
