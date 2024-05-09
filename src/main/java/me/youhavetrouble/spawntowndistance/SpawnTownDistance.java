package me.youhavetrouble.spawntowndistance;

import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import com.palmergames.bukkit.towny.event.TownPreClaimEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.joml.Vector2d;


public final class SpawnTownDistance extends JavaPlugin implements Listener {

    private Vector2d spawnTownChunk = new Vector2d(0, 0);
    private int unclaimableDistance = 0;
    private String cancelMessage = "You cannot claim a town this close to spawn";

    private boolean preventBlockBreak, preventBlockPlace, preventMobSpawn;

    @Override
    public void onEnable() {
        reloadPlugin();
        getServer().getPluginManager().registerEvents(this, this);
    }

    private void reloadPlugin() {
        saveDefaultConfig();
        reloadConfig();
        this.spawnTownChunk = new Vector2d(
                getConfig().getDouble("spawn-town-chunk.x", 0),
                getConfig().getDouble("spawn-town-chunk.z", 0)
        );
        this.unclaimableDistance = getConfig().getInt("unclaimable-distance", 0);
        this.cancelMessage = getConfig().getString("cancel-message", "You cannot claim a town this close to spawn");

        this.preventBlockPlace = getConfig().getBoolean("prevent-block-place", true);
        this.preventBlockBreak = getConfig().getBoolean("prevent-block-break", true);
        this.preventMobSpawn = getConfig().getBoolean("prevent-mob-spawns", true);

    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClaim(TownPreClaimEvent event) {
        // ignore towny admins, they probably know what they're doing. Hopefully.
        if (event.getPlayer().hasPermission("towny.admin.claim")) return;

        Vector2d eventChunk = new Vector2d(event.getTownBlock().getX() >> 4, event.getTownBlock().getZ() >> 4);
        if (getDistanceBetweenChunks(spawnTownChunk, eventChunk) > unclaimableDistance) return;
        event.setCancelled(true);
        event.setCancelMessage(this.cancelMessage);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreakNearSpawn(BlockBreakEvent event) {
        Vector2d eventChunk = new Vector2d(event.getBlock().getChunk().getX(), event.getBlock().getChunk().getZ());
        if (getDistanceBetweenChunks(spawnTownChunk, eventChunk) > unclaimableDistance) return;
        if (event.getPlayer().hasPermission("spawntowndistance.bypass.breaking")) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlaceNearSpawn(BlockPlaceEvent event) {
        Vector2d eventChunk = new Vector2d(event.getBlock().getChunk().getX(), event.getBlock().getChunk().getZ());
        if (getDistanceBetweenChunks(spawnTownChunk, eventChunk) > unclaimableDistance) return;
        if (event.getPlayer().hasPermission("spawntowndistance.bypass.placing")) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMobSpawnNearSpawn(PreCreatureSpawnEvent event) {
        Vector2d eventChunk = new Vector2d(event.getSpawnLocation().getChunk().getX(), event.getSpawnLocation().getChunk().getZ());
        if (getDistanceBetweenChunks(spawnTownChunk, eventChunk) > unclaimableDistance) return;
        event.setCancelled(true);
    }

    /**
     * Get manhattan distance between two chunks
     */
    private int getDistanceBetweenChunks(Vector2d chunk1, Vector2d chunk2) {
        return (int) (Math.abs(chunk1.x - chunk2.x) + Math.abs(chunk1.y - chunk2.y));
    }




}
