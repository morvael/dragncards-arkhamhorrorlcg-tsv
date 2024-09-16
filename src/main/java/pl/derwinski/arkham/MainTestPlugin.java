package pl.derwinski.arkham;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import static pl.derwinski.arkham.Util.log;

/**
 *
 * @author morvael
 */
public class MainTestPlugin {

    private final LinkedHashMap<String, File> actionLists = new LinkedHashMap<>();
    private final LinkedHashMap<String, File> functions = new LinkedHashMap<>();
    private final LinkedHashMap<String, File> prompts = new LinkedHashMap<>();
    private final LinkedHashMap<String, File> labels = new LinkedHashMap<>();
    private final LinkedHashMap<String, File> preBuiltDecks = new LinkedHashMap<>();
    private final LinkedHashMap<String, File> rules = new LinkedHashMap<>();

    private static final HashSet<String> ignoredFunctions = new HashSet<>();

    static {
        ignoredFunctions.add("\"DISAPPEARANCE_AT_THE_TWILIGHT_ESTATE_PRE_INIT\"");
        ignoredFunctions.add("\"DISAPPEARANCE_AT_THE_TWILIGHT_ESTATE_SETUP_GAVRIELLA\"");
        ignoredFunctions.add("\"DISAPPEARANCE_AT_THE_TWILIGHT_ESTATE_SETUP_JEROME\"");
        ignoredFunctions.add("\"DISAPPEARANCE_AT_THE_TWILIGHT_ESTATE_SETUP_VALENTINO\"");
        ignoredFunctions.add("\"DISAPPEARANCE_AT_THE_TWILIGHT_ESTATE_SETUP_PENNY\"");
    }

    private void processNames(File f, String name, JsonNode n, LinkedHashMap<String, File> map) throws Exception {
        Iterator<String> it = n.fieldNames();
        while (it.hasNext()) {
            String fieldName = String.format("\"%s\"", it.next());
            if (map.containsKey(fieldName) == false) {
                map.put(fieldName, f);
            } else {
                System.out.println(String.format("Duplicate %s %s in %s and %s", name, fieldName, map.get(fieldName).getName(), f.getName()));
            }
        }
    }

    private void processFile(JsonMapper mapper, File f) throws Exception {
        JsonNode c = mapper.readTree(f);
        if (c.isObject()) {
            Iterator<String> it = c.fieldNames();
            while (it.hasNext()) {
                String fieldName = it.next();
                switch (fieldName) {
                    case "actionLists":
                        processNames(f, fieldName, c.get(fieldName), actionLists);
                        break;
                    case "functions":
                        processNames(f, fieldName, c.get(fieldName), functions);
                        break;
                    case "prompts":
                        processNames(f, fieldName, c.get(fieldName), prompts);
                        break;
                    case "labels":
                        processNames(f, fieldName, c.get(fieldName), labels);
                        break;
                    case "preBuiltDecks":
                        processNames(f, fieldName, c.get(fieldName), preBuiltDecks);
                        break;
                    case "automation":
                        JsonNode automationNode = c.get(fieldName);
                        if (automationNode.has("gameRules")) {
                            processNames(f, "rules", automationNode.get("gameRules"), rules);
                        } else if (automationNode.has("cards")) {
                            JsonNode cardsNode = automationNode.get("cards");
                            Iterator<String> itCards = cardsNode.fieldNames();
                            while (itCards.hasNext()) {
                                String databaseId = itCards.next();
                                JsonNode cardNode = cardsNode.get(databaseId);
                                if (cardNode.has("rules")) {
                                    processNames(f, "rules", cardNode.get("rules"), rules);
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private static final Pattern CAMPAIGN_FILE = Pattern.compile("^S[0-9]+ [^.]+\\.json$");

    private void processUsedNames(File f, String name, String content, LinkedHashMap<String, File> map, HashSet<String> ignored) throws Exception {
        for (Map.Entry<String, File> e : map.entrySet()) {
            if (content.contains(e.getKey())
                    && e.getValue() != f
                    && (ignored == null || ignored.contains(e.getKey()) == false)
                    && e.getValue().getName().startsWith("S")
                    && e.getValue().getName().startsWith("Scenarios") == false
                    && CAMPAIGN_FILE.matcher(e.getValue().getName()).matches() == false) {
                System.out.println(String.format("Reused %s %s in %s and %s", name, e.getKey(), e.getValue().getName(), f.getName()));
            }
        }
    }

    private void processScenarioFile(File f) throws Exception {
        String content = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
        processUsedNames(f, "actionLists", content, actionLists, null);
        processUsedNames(f, "functions", content, functions, ignoredFunctions);
        processUsedNames(f, "prompts", content, prompts, null);
        processUsedNames(f, "labels", content, labels, null);
        processUsedNames(f, "preBuiltDecks", content, preBuiltDecks, null);
        processUsedNames(f, "rules", content, rules, null);
    }

    public void run() throws Exception {
        JsonMapper mapper = JsonMapper.builder()
                .configure(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION, true)
                .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
                .build();

        File dir = new File("../../cards/arkham/dragncards-arkhamhorrorlcg-plugin/jsons");
        Collection<File> files = FileUtils.listFiles(dir, new String[]{"json"}, true);
        for (File f : files) {
            processFile(mapper, f);
        }
        System.out.println(String.format("actionLists: %d", actionLists.size()));
        System.out.println(String.format("functions: %d", functions.size()));
        System.out.println(String.format("prompts: %d", prompts.size()));
        System.out.println(String.format("labels: %d", labels.size()));
        System.out.println(String.format("preBuiltDecks: %d", preBuiltDecks.size()));
        System.out.println(String.format("rules: %d", rules.size()));
        for (File f : files) {
            if (f.getName().startsWith("S") && f.getName().startsWith("Scenarios") == false) {
                processScenarioFile(f);
            }
        }
    }

    public static void main(String[] args) {
        try {
            new MainTestPlugin().run();
        } catch (Exception ex) {
            log(ex);
        }
    }

}
