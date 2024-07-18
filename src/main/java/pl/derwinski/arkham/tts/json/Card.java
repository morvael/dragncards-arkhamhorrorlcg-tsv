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
package pl.derwinski.arkham.tts.json;

import java.io.File;

/**
 *
 * @author morvael
 */
public class Card {

    private File file;
    private String cardID;
    private String nickname;
    private String description;
    private Boolean sidewaysCard;
    private GMNotes GMNotes;
    private CustomDecks customDecks;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getCardID() {
        return cardID;
    }

    public void setCardID(String cardID) {
        this.cardID = cardID;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getSidewaysCard() {
        return sidewaysCard;
    }

    public void setSidewaysCard(Boolean sidewaysCard) {
        this.sidewaysCard = sidewaysCard;
    }

    public GMNotes getGMNotes() {
        return GMNotes;
    }

    public void setGMNotes(GMNotes GMNotes) {
        this.GMNotes = GMNotes;
    }

    public CustomDecks getCustomDecks() {
        return customDecks;
    }

    public void setCustomDecks(CustomDecks customDecks) {
        this.customDecks = customDecks;
    }

    public boolean isValid() {
        if (file == null
                || cardID == null
                || cardID.length() == 0
                //|| nickname == null
                //|| nickname.length() == 0
                //|| description == null
                //|| description.length() == 0
                || sidewaysCard == null
                //|| GMNotes == null
                //|| GMNotes.isValid() == false
                || customDecks == null
                || getCustomDeck() == null) {
            return false;
        }
        if (file.toString().contains("AllPlayerCards") && (GMNotes == null || GMNotes.isValid() == false)) {
            return false;
        }
        return true;
    }

    public boolean isSkippable() {
        String id = getGMNotesId();
        if (id != null && (id.endsWith("-pf") || id.endsWith("-pb"))) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Card{");
        sb.append("file=").append(file);
        sb.append(", cardID=").append(cardID);
        sb.append(", nickname=").append(nickname);
        sb.append(", description=").append(description);
        sb.append(", sidewaysCard=").append(sidewaysCard);
        sb.append(", GMNotes=").append(GMNotes);
        sb.append(", customDecks=").append(customDecks);
        sb.append('}');
        return sb.toString();
    }

    public CustomDeck getCustomDeck() {
        return customDecks != null ? customDecks.getCustomDeck(cardID) : null;
    }

    public String getFaceKey() {
        CustomDeck cd = getCustomDeck();
        return String.format("%s_%s_%s_%s", cardID.substring(cardID.length() - 2), String.valueOf(cd.getNumWidth()), String.valueOf(cd.getNumHeight()), cd.getFaceKey());
    }

    public String getBackKey() {
        CustomDeck cd = getCustomDeck();
        if (cd.getUniqueBack() != null && cd.getUniqueBack() == false) {
            return cd.getBackKey();
        } else {
            return String.format("%s_%s_%s_%s", cardID.substring(cardID.length() - 2), String.valueOf(cd.getNumWidth()), String.valueOf(cd.getNumHeight()), cd.getBackKey());
        }
    }

    public String getGMNotesId() {
        if (GMNotes != null && GMNotes.getId() != null && GMNotes.getId().length() > 0) {
            return GMNotes.getId();
        } else {
            return null;
        }
    }

    public String getGMNotesType() {
        if (GMNotes != null && GMNotes.getType() != null && GMNotes.getType().length() > 0) {
            return GMNotes.getType().toLowerCase();
        } else {
            return null;
        }
    }

}
