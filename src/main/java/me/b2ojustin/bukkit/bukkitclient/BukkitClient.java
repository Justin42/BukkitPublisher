/*
 * This file is part of BukkitPublisher.
 *
 *     BukkitPublisher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     BukkitPublisher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with BukkitPublisher.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.b2ojustin.bukkit.bukkitclient;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.b2ojustin.bukkit.bukkitclient.json.CraftBukkitBuild;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class BukkitClient {
    private static final Logger logger = Logger.getLogger(BukkitClient.class.getName());
    private static final String BUKKIT_URL = "http://dev.bukkit.org/";
    private static final String BUKKIT_VERSION_URL = BUKKIT_URL + "game-versions.json";
    private static final String USER_AGENT = "BukkitAPIClient/0.0.1";
    private String apiKey = "";
    {

    }
    public static Logger getLogger() {
        return logger;
    }

    public BukkitClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public boolean uploadFile(FileUploadDescriptor descriptor) {
        CraftBukkitBuild build = getBukkitBuild(descriptor.getBukkitVersion());
        if(build == null) build = getLatestBuild();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost uploadPost = new HttpPost(descriptor.getProjectUrl() + "upload-file.json?api-key=" + apiKey);
            uploadPost.addHeader("User-agent", USER_AGENT);

            HashMap<String, String> formData = new HashMap<>();
            formData.put("name", descriptor.getName());
            formData.put("game_versions", String.valueOf(build.buildNumber));


            switch(descriptor.getReleaseType()) {
                case ALPHA: formData.put("file_type", "a"); break;
                case BETA: formData.put("file_type", "b"); break;
                case RELEASE: formData.put("file_type", "r"); break;
                default: formData.put("file_type", "a"); break;
            }

            // Add change logs
            StringBuilder changes = new StringBuilder("");
            for(String change : descriptor.getChanges()) {
                changes.append(change).append("\n");
            }
            if(changes.toString().isEmpty()) changes.append(" ");
            formData.put("change_log", changes.toString());
            formData.put("change_markup_type", "plain");


            StringBuilder caveats = new StringBuilder();
            for(String caveat : descriptor.getCaveats()) {
                caveats.append(caveat).append("\n");
            }
            formData.put("known_caveats", caveats.toString());
            formData.put("caveats_markup_type", "plain");

            // Add form data to httpost
            MultipartEntity entity = new MultipartEntity();
            for(Map.Entry<String, String> entry : formData.entrySet()) {
                entity.addPart(new FormBodyPart(entry.getKey(), new StringBody(entry.getValue(), ContentType.TEXT_PLAIN)));
            }

            // Add file data
            FileBody fileBody = new FileBody(descriptor.getFile(), ContentType.create("application/x-java-archive"), descriptor.getFileName());
            entity.addPart("file", fileBody);
            uploadPost.setEntity(entity);

            try(CloseableHttpResponse response = httpClient.execute(uploadPost)) {
                if(response.getStatusLine().getStatusCode() != 201) {
                    logger.warning(EntityUtils.toString(response.getEntity()));
                    logger.warning("File upload was unsucessful. " + response.getStatusLine().getReasonPhrase());
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        logger.info("File upload successful");
        return true;
    }

    public static HashMap<Integer, CraftBukkitBuild> getBukkitVersions() {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpMethod = new HttpGet(BUKKIT_VERSION_URL);
            CloseableHttpResponse response = httpClient.execute(httpMethod);
            Gson gson = new Gson();

            final Type type = new TypeToken<HashMap<Integer, CraftBukkitBuild>>(){}.getType();
            HashMap<Integer, CraftBukkitBuild> buildMap = gson.fromJson(EntityUtils.toString(response.getEntity()), type);
            for(Map.Entry<Integer, CraftBukkitBuild> entry : buildMap.entrySet()) {
                entry.getValue().buildNumber = entry.getKey();
            }
            httpMethod.releaseConnection();
            return buildMap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    public static boolean isValidVersion(String versionName) {
        Collection<CraftBukkitBuild> bukkitBuilds = getBukkitVersions().values();
        for(CraftBukkitBuild build : bukkitBuilds) {
            if(build.name.endsWith(versionName)) return true;
        }
        return false;
    }

    public static CraftBukkitBuild getLatestBuild() {
        HashMap<Integer, CraftBukkitBuild> buildMap = getBukkitVersions();
        int latestBuild = 0;
        for(int buildNumber : buildMap.keySet()) {
            if(buildNumber > latestBuild) latestBuild = buildNumber;
        }
        return buildMap.get(latestBuild);
    }

    public static CraftBukkitBuild getBukkitBuild(String version) {
        Collection<CraftBukkitBuild> bukkitBuilds = getBukkitVersions().values();
        for(CraftBukkitBuild build : bukkitBuilds) {
            if(build.name.endsWith(version)) {
                return build;
            }
        }
        return null;
    }
}
