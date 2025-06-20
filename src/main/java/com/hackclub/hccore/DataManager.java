package com.hackclub.hccore;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class DataManager {

  private final HCCorePlugin plugin;
  @Getter
  private final String dataFolder;
  private final Map<UUID, PlayerData> onlinePlayers = new HashMap<>();

  public DataManager(HCCorePlugin plugin) {
    this.plugin = plugin;
    this.dataFolder = plugin.getDataFolder() + File.separator + "players";

    File folder = new File(this.dataFolder);
    folder.mkdirs();
  }

  public PlayerData getData(Player player) {
    return this.onlinePlayers.get(player.getUniqueId());
  }

  public PlayerData getData(OfflinePlayer offlinePlayer) {
    PlayerData data = this.onlinePlayers.get(offlinePlayer.getUniqueId());

    if (data == null) {
      data = new PlayerData(this.plugin, offlinePlayer);
    }

    return data;
  }

  public PlayerData findData(Predicate<? super PlayerData> predicate) {
    return this.onlinePlayers.values().stream().filter(predicate).findFirst().orElse(null);
  }

  public void registerPlayer(Player player) {
    this.onlinePlayers.put(player.getUniqueId(), new PlayerData(this.plugin, player));

    // Register player's team
    Scoreboard mainScoreboard = player.getServer().getScoreboardManager().getMainScoreboard();
    player.setScoreboard(mainScoreboard);
    // Unregister existing teams in the player's name
    Team playerTeam = mainScoreboard.getTeam(player.getName());
    if (playerTeam != null) {
      playerTeam.unregister();
    }
    playerTeam = mainScoreboard.registerNewTeam(player.getName());
    playerTeam.addEntry(player.getName());

    // Load in player data for use
    this.getData(player).load();
  }

  public void registerAll() {
    for (Player player : this.plugin.getServer().getOnlinePlayers()) {
      this.registerPlayer(player);
    }
  }

  public void unregisterPlayer(Player player) {
    this.getData(player).save();

    this.getData(player).getTeam().unregister();

    this.onlinePlayers.remove(player.getUniqueId());
  }

  public void unregisterAll() {
    for (Player player : this.plugin.getServer().getOnlinePlayers()) {
      this.unregisterPlayer(player);
    }
  }
}
