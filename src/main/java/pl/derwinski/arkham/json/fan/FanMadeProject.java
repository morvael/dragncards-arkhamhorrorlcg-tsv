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
import java.util.ArrayList;
import java.util.HashSet;
import static pl.derwinski.arkham.Util.log;
import static pl.derwinski.arkham.Util.readString;
import pl.derwinski.arkham.json.configuration.Configuration;
import pl.derwinski.arkham.json.metadata.Metadata;

/**
 *
 * @author morvael
 */
public class FanMadeProject {

    private static final HashSet<String> unhandled = new HashSet<>();

    public static ArrayList<FanMadeProject> readFanMadeProjects(Configuration configuration, Metadata metadata, JsonNode c) throws Exception {
        if (c.isArray()) {
            var result = new ArrayList<FanMadeProject>();
            for (var i = 0; i < c.size(); i++) {
                var o = readFanMadeProject(configuration, metadata, c.get(i));
                result.add(o);
            }
            return result;
        } else {
            if (c.isNull() == false) {
                log("Error reading FanMadeProject array: %s", c.asText());
            }
            return null;
        }
    }

    private static FanMadeProject readFanMadeProject(Configuration configuration, Metadata metadata, JsonNode c, FanMadeProject o) throws Exception {
        var it = c.fieldNames();
        while (it.hasNext()) {
            var fieldName = it.next();
            switch (fieldName) {
                case "bucket_path":
                    o.bucketPath = readString(c, fieldName);
                    break;
                case "id":
                    o.id = readString(c, fieldName);
                    break;
                case "meta":
                    o.meta = FanMadeProjectMeta.readFanMadeProjectMeta(configuration, metadata, c.get(fieldName));
                    break;
                default:
                    if (unhandled.add(fieldName)) {
                        log("Unhandled field name in FanMadeProject: %s (%s : %s)", fieldName, c.get(fieldName), c.get(fieldName).getNodeType());
                    }
                    break;
            }
        }
        return o;
    }

    public static FanMadeProject readFanMadeProject(Configuration configuration, Metadata metadata, JsonNode c) throws Exception {
        if (c.isObject()) {
            return readFanMadeProject(configuration, metadata, c, new FanMadeProject(configuration, metadata));
        } else {
            if (c.isNull() == false) {
                log("Error reading FanMadeProject object: %s", c.asText());
            }
            return null;
        }
    }

    private final Configuration configuration;
    private final Metadata metadata;
    private String bucketPath;
    private String id;
    private FanMadeProjectMeta meta;

    public FanMadeProject(Configuration configuration, Metadata metadata) {
        this.configuration = configuration;
        this.metadata = metadata;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public String getBucketPath() {
        return bucketPath;
    }

    public String getId() {
        return id;
    }

    public FanMadeProjectMeta getMeta() {
        return meta;
    }

    @Override
    public String toString() {
        return meta != null ? meta.toString() : id;
    }

    public FanMadeProjectFull loadFullProject() throws Exception {
        return FanMadeProjectFull.loadFanMadeProjectFull(this);
    }

}
