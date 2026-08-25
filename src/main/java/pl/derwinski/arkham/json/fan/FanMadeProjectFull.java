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
package pl.derwinski.arkham.json.fan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.File;
import java.util.HashSet;
import pl.derwinski.arkham.Util;
import static pl.derwinski.arkham.Util.log;
import pl.derwinski.arkham.json.configuration.Configuration;
import pl.derwinski.arkham.json.metadata.Metadata;

/**
 *
 * @author morvael
 */
public final class FanMadeProjectFull {

    private static final HashSet<String> unhandled = new HashSet<>();

    public static FanMadeProjectFull loadFanMadeProjectFull(FanMadeProject project) throws Exception {
        var url = project.getMeta().getURL();
        var path = "run/%s.json".formatted(project.getMeta().getName());
        Util.downloadIfOld(url, path);
        return loadFanMadeProjectFull(project.getConfiguration(), project.getMetadata(), path);
    }

    public static FanMadeProjectFull loadFanMadeProjectFull(Configuration configuration, Metadata metadata, String path) throws Exception {
        var file = new File(path);
        var c = new JsonMapper().readTree(file);
        if (c != null) {
            return loadFanMadeProjectFull(configuration, metadata, c);
        } else {
            log("Error reading FanMadeProjectFull file");
            return null;
        }
    }

    public static FanMadeProjectFull loadFanMadeProjectFull(Configuration configuration, Metadata metadata, JsonNode c) throws Exception {
        if (c.isObject()) {
            var o = new FanMadeProjectFull(configuration, metadata);
            var it = c.fieldNames();
            while (it.hasNext()) {
                var fieldName = it.next();
                switch (fieldName) {
                    case "meta":
                        o.meta = FanMadeProjectMeta.readFanMadeProjectMeta(configuration, metadata, c.get(fieldName));
                        break;
                    case "data":
                        o.data = FanMadeProjectData.readFanMadeProjectData(configuration, metadata, configuration.getCustomSetId(o.meta.getCode()), c.get(fieldName));
                        break;
                    default:
                        if (unhandled.add(fieldName)) {
                            log("Unhandled field name in FanMadeProjectFull: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType());
                        }
                        break;

                }
            }
            return o;
        } else {
            if (c.isNull() == false) {
                log("Error reading FanMadeProjectFull object: %s", c.asText());
            }
            return null;
        }
    }

    private final Configuration configuration;
    private final Metadata metadata;
    private FanMadeProjectMeta meta;
    private FanMadeProjectData data;

    public FanMadeProjectFull(Configuration configuration, Metadata metadata) {
        this.configuration = configuration;
        this.metadata = metadata;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public FanMadeProjectMeta getMeta() {
        return meta;
    }

    public FanMadeProjectData getData() {
        return data;
    }

    @Override
    public String toString() {
        return meta != null ? meta.toString() : "null";
    }

}
