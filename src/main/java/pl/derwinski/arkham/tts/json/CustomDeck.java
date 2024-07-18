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

/**
 *
 * @author morvael
 */
public class CustomDeck {

    private String id;
    private String faceURL;
    private String backURL;
    private Integer numWidth;
    private Integer numHeight;
    private Boolean backIsHidden;
    private Boolean uniqueBack;
    private Integer type;

    private String faceKey;
    private String backKey;

    public CustomDeck() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFaceURL() {
        return faceURL;
    }

    public void setFaceURL(String faceURL) {
        this.faceURL = faceURL;
    }

    public String getBackURL() {
        return backURL;
    }

    public void setBackURL(String backURL) {
        this.backURL = backURL;
    }

    public Integer getNumWidth() {
        return numWidth;
    }

    public void setNumWidth(Integer numWidth) {
        this.numWidth = numWidth;
    }

    public Integer getNumHeight() {
        return numHeight;
    }

    public void setNumHeight(Integer numHeight) {
        this.numHeight = numHeight;
    }

    public Boolean getBackIsHidden() {
        return backIsHidden;
    }

    public void setBackIsHidden(Boolean backIsHidden) {
        this.backIsHidden = backIsHidden;
    }

    public Boolean getUniqueBack() {
        return uniqueBack;
    }

    public void setUniqueBack(Boolean uniqueBack) {
        this.uniqueBack = uniqueBack;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CustomDeck{");
        sb.append("faceURL=").append(faceURL);
        sb.append(", backURL=").append(backURL);
        sb.append(", numWidth=").append(numWidth);
        sb.append(", numHeight=").append(numHeight);
        sb.append(", backIsHidden=").append(backIsHidden);
        sb.append(", uniqueBack=").append(uniqueBack);
        sb.append(", type=").append(type);
        sb.append('}');
        return sb.toString();
    }

    private String getKey(String url) {
        StringBuilder sb = new StringBuilder();
        int len = url.length();
        for (int i = 0; i < len; i++) {
            if (Character.isLetterOrDigit(url.charAt(i))) {
                sb.append(url.charAt(i));
            }
        }
        sb.append(".png");
        return sb.toString();
    }

    public String getFaceKey() {
        if (faceKey == null) {
            if (faceURL != null && faceURL.length() > 0) {
                faceKey = getKey(faceURL);
            } else {
                faceKey = String.format("%s_F.png", id);
            }
        }
        return faceKey;
    }

    public String getBackKey() {
        if (backKey == null) {
            if (backURL != null && backURL.length() > 0) {
                backKey = getKey(backURL);
            } else {
                backKey = String.format("%s_B.png", id);
            }
        }
        return backKey;
    }

}
