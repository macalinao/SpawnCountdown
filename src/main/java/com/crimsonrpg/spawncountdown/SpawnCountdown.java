/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crimsonrpg.spawncountdown;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The Spawn Countdown main plugin.
 */
public class SpawnCountdown extends JavaPlugin {

    public static Logger LOGGER = Logger.getLogger("Minecraft");

    private Map<Player, Integer> spawning = new HashMap<Player, Integer>();

    public void onDisable() {
        LOGGER.info("[SpawnCountdown] Plugin disabled.");
    }

    public void onEnable() {
        Bukkit.getPluginManager().registerEvent(Type.PLAYER_MOVE, new Cdpl(), Priority.Normal, this);
        LOGGER.info("[SpawnCountdown] Plugin enabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You are only allowed to use this command in-game.");
            return false;
        }
        Player player = (Player) sender;
        int task = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Countdown(10, player), 0L, 20L);
        spawning.put(player, task);
        return true;
    }

    private class Cdpl extends PlayerListener {

        @Override
        public void onPlayerMove(PlayerMoveEvent event) {
            Player p = event.getPlayer();
            if (spawning.containsKey(p)) {
                p.sendMessage(ChatColor.DARK_RED + "Spawn cancelled.");
                Bukkit.getScheduler().cancelTask(spawning.get(p));
                spawning.remove(p);
            }
        }

    }

    private class Countdown implements Runnable {

        private int start;

        private Player player;

        private boolean first = true;

        public Countdown(int start, Player player) {
            this.start = start;
            this.player = player;
        }

        public void run() {
            if (!first) {
                start -= 1;
            } else {
                first = false;
            }
            
            if (start == 0) {
                player.sendMessage(ChatColor.YELLOW + "Returning to spawn...");
                
                Location spawn = player.getWorld().getSpawnLocation();
                
                PlayerRespawnEvent mock = new PlayerRespawnEvent(player, spawn, false);
                Bukkit.getPluginManager().callEvent(mock);
                
                Location realSpawn = mock.getRespawnLocation();
                
                player.teleport(realSpawn);
                Bukkit.getScheduler().cancelTask(spawning.get(player));
                spawning.remove(player);
            } else {
                player.sendMessage(ChatColor.YELLOW.toString() + "Spawning in " + start + " seconds.");
            }
        }

    }

}
