package pl.derwinski.arkham;

import java.util.LinkedHashMap;

/**
 *
 * @author morvael
 */
public class Translation {

    private final LinkedHashMap<String, Integer> translations = new LinkedHashMap<>();

    public Translation() {

    }

    public Translation register(String realTrait) {
        var n = translations.getOrDefault(realTrait, 0) + 1;
        translations.put(realTrait, n);
        return this;
    }

    public LinkedHashMap<String, Integer> getTranslations() {
        return translations;
    }

    public String getMostPopular() {
        String trait = null;
        int max = 0;
        for (var e : translations.entrySet()) {
            if (e.getValue() > max) {
                trait = e.getKey();
                max = e.getValue();
            }
        }
        return trait;
    }

    public String format() {
        var sb = new StringBuilder();
        if (translations.isEmpty() == false) {
            for (var e : translations.entrySet()) {
                sb.append("\"");
                sb.append(e.getKey());
                sb.append("\" x");
                sb.append(e.getValue());
                sb.append(", ");
            }
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }

}
