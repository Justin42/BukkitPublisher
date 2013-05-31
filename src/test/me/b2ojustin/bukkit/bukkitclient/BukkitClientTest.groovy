package me.b2ojustin.bukkit.bukkitclient

import me.b2ojustin.bukkit.bukkitclient.json.CraftBukkitBuild

class BukkitClientTest extends GroovyTestCase {
    public void testGetBukkitVersions() {
        HashMap<Integer, CraftBukkitBuild> buildMap = BukkitClient.getBukkitVersions();
        CraftBukkitBuild build = buildMap.get(313);
        assertEquals("CB 1.5.2-R0.1", build.name);
    }

    public void testIsValidVersion() {
        assertEquals(true, BukkitClient.isValidVersion("1.5.2-R0.1"));
    }
}
