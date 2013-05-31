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
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;

public class BukkitClient {
    private static final String BUKKIT_URL = "http://dev.bukkit.org/";
    private static final String BUKKIT_VERSION_URL = "http://dev.bukkit.org/game-versions.json";
    private final String projectUrl;
    private final String apiKey;

    public BukkitClient(String projectUrl, String apiKey) {
        this.projectUrl = projectUrl;
        this.apiKey = apiKey;
    }

    public boolean uploadFile(FileUploadDescriptor descriptor) {

        return true;
    }

    public static HashMap<Integer, CraftBukkitBuild> getBukkitVersions() {
        try {
            HttpClient httpClient = new HttpClient();
            HttpMethod httpMethod = new GetMethod(BUKKIT_VERSION_URL);
            httpClient.executeMethod(httpMethod);
            Gson gson = new Gson();

            final Type type = new TypeToken<HashMap<Integer, CraftBukkitBuild>>(){}.getType();
            HashMap<Integer, CraftBukkitBuild> buildMap = gson.fromJson(httpMethod.getResponseBodyAsString(), type);
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
