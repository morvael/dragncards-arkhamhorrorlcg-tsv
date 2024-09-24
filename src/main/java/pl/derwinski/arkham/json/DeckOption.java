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
import pl.derwinski.arkham.Copyable;
import pl.derwinski.arkham.Util;

/**
 *
 * @author morvael
 */
public class DeckOption implements Copyable<DeckOption> {

    private ArrayList<String> faction;
    private DeckOptionLevel level;
    private Integer limit;
    private String error;
    private Boolean not;
    private ArrayList<String> trait;
    private ArrayList<String> tag;
    private DeckOptionAtLeast atLeast;
    private ArrayList<String> uses;
    private ArrayList<String> text;
    private String name;
    private ArrayList<String> factionSelect;
    private ArrayList<String> type;
    private ArrayList<String> deckSizeSelect;
    private ArrayList<String> slot;
    private ArrayList<DeckOption> optionSelect;
    private String id;
    private Boolean permanent;
    private DeckOptionLevel baseLevel;
    private Integer size;

    public DeckOption() {

    }

    public ArrayList<String> getFaction() {
        return faction;
    }

    public void setFaction(ArrayList<String> faction) {
        this.faction = faction;
    }

    public DeckOptionLevel getLevel() {
        return level;
    }

    public void setLevel(DeckOptionLevel level) {
        this.level = level;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Boolean getNot() {
        return not;
    }

    public void setNot(Boolean not) {
        this.not = not;
    }

    public ArrayList<String> getTrait() {
        return trait;
    }

    public void setTrait(ArrayList<String> trait) {
        this.trait = trait;
    }

    public ArrayList<String> getTag() {
        return tag;
    }

    public void setTag(ArrayList<String> tag) {
        this.tag = tag;
    }

    public DeckOptionAtLeast getAtLeast() {
        return atLeast;
    }

    public void setAtLeast(DeckOptionAtLeast atLeast) {
        this.atLeast = atLeast;
    }

    public ArrayList<String> getUses() {
        return uses;
    }

    public void setUses(ArrayList<String> uses) {
        this.uses = uses;
    }

    public ArrayList<String> getText() {
        return text;
    }

    public void setText(ArrayList<String> text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getFactionSelect() {
        return factionSelect;
    }

    public void setFactionSelect(ArrayList<String> factionSelect) {
        this.factionSelect = factionSelect;
    }

    public ArrayList<String> getType() {
        return type;
    }

    public void setType(ArrayList<String> type) {
        this.type = type;
    }

    public ArrayList<String> getDeckSizeSelect() {
        return deckSizeSelect;
    }

    public void setDeckSizeSelect(ArrayList<String> deckSizeSelect) {
        this.deckSizeSelect = deckSizeSelect;
    }

    public ArrayList<String> getSlot() {
        return slot;
    }

    public void setSlot(ArrayList<String> slot) {
        this.slot = slot;
    }

    public ArrayList<DeckOption> getOptionSelect() {
        return optionSelect;
    }

    public void setOptionSelect(ArrayList<DeckOption> optionSelect) {
        this.optionSelect = optionSelect;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getPermanent() {
        return permanent;
    }

    public void setPermanent(Boolean permanent) {
        this.permanent = permanent;
    }

    public DeckOptionLevel getBaseLevel() {
        return baseLevel;
    }

    public void setBaseLevel(DeckOptionLevel baseLevel) {
        this.baseLevel = baseLevel;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    @Override
    public DeckOption copy() {
        DeckOption o = new DeckOption();
        o.faction = Util.simpleListCopy(faction);
        o.level = Util.copy(level);
        o.limit = limit;
        o.error = error;
        o.not = not;
        o.trait = Util.simpleListCopy(trait);
        o.tag = Util.simpleListCopy(tag);
        o.atLeast = Util.copy(atLeast);
        o.uses = Util.simpleListCopy(uses);
        o.text = Util.simpleListCopy(text);
        o.name = name;
        o.factionSelect = Util.simpleListCopy(factionSelect);
        o.type = Util.simpleListCopy(type);
        o.deckSizeSelect = Util.simpleListCopy(deckSizeSelect);
        o.slot = Util.simpleListCopy(slot);
        o.optionSelect = Util.copy(optionSelect);
        o.id = id;
        o.permanent = permanent;
        o.baseLevel = Util.copy(baseLevel);
        o.size = size;
        return o;
    }

}
