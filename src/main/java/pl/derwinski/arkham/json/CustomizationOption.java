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

import pl.derwinski.arkham.Copyable;
import pl.derwinski.arkham.Util;

/**
 *
 * @author morvael
 */
public class CustomizationOption implements Copyable<CustomizationOption> {

    private Integer xp;
    private String realTraits;
    private String realSlot;
    private String textChange;
    private Integer health;
    private Integer sanity;
    private Integer cost;
    private String realText;
    private String tags;
    private Integer position;
    private String choice;
    private Integer quantity;
    private CustomizationOptionCard card;
    private Integer deckLimit;

    public CustomizationOption() {

    }

    public Integer getXp() {
        return xp;
    }

    public void setXp(Integer xp) {
        this.xp = xp;
    }

    public String getRealTraits() {
        return realTraits;
    }

    public void setRealTraits(String realTraits) {
        this.realTraits = realTraits;
    }

    public String getRealSlot() {
        return realSlot;
    }

    public void setRealSlot(String realSlot) {
        this.realSlot = realSlot;
    }

    public String getTextChange() {
        return textChange;
    }

    public void setTextChange(String textChange) {
        this.textChange = textChange;
    }

    public Integer getHealth() {
        return health;
    }

    public void setHealth(Integer health) {
        this.health = health;
    }

    public Integer getSanity() {
        return sanity;
    }

    public void setSanity(Integer sanity) {
        this.sanity = sanity;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    public String getRealText() {
        return realText;
    }

    public void setRealText(String realText) {
        this.realText = realText;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getChoice() {
        return choice;
    }

    public void setChoice(String choice) {
        this.choice = choice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public CustomizationOptionCard getCard() {
        return card;
    }

    public void setCard(CustomizationOptionCard card) {
        this.card = card;
    }

    public Integer getDeckLimit() {
        return deckLimit;
    }

    public void setDeckLimit(Integer deckLimit) {
        this.deckLimit = deckLimit;
    }

    @Override
    public CustomizationOption copy() {
        CustomizationOption o = new CustomizationOption();
        o.xp = xp;
        o.realTraits = realTraits;
        o.realSlot = realSlot;
        o.textChange = textChange;
        o.health = health;
        o.sanity = sanity;
        o.cost = cost;
        o.realText = realText;
        o.tags = tags;
        o.position = position;
        o.choice = choice;
        o.quantity = quantity;
        o.card = Util.copy(card);
        o.deckLimit = deckLimit;
        return o;
    }

}
