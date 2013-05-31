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

public class BukkitClient {
    private static final String BUKKIT_URL = "http://dev.bukkit.org/";
    private String projectName;
    private String apiKey = "";

    public BukkitClient(String projectName, String apiKey) {
        this.projectName = projectName;
        this.apiKey = apiKey;
        if(this.apiKey == null) this.apiKey = "";
    }
}
