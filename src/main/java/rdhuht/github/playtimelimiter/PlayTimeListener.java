package rdhuht.github.playtimelimiter;

import java.io.File;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import rdhuht.github.playtimelimiter.utils.FileUtils;
import rdhuht.github.playtimelimiter.utils.Timestamper;

public class PlayTimeListener implements Listener {
    private final PlayTimeLimiter plugin;

    public PlayTimeListener(PlayTimeLimiter instance) {
        this.plugin = instance;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        FileUtils.appendStringToFile(new File(this.plugin.getDataFolder(),
                "playtime.log"), String.format("[%s] %s logged in",
                Timestamper.now(), event.getPlayer().getName()));
        if (this.plugin.getTimeAllowedInSeconds(event.getPlayer().getUniqueId()) <= 0) {
            FileUtils.appendStringToFile(new File(this.plugin.getDataFolder(), "playtime.log"),
                    String.format("[%s] %s was kicked for exceeding play time",
                            Timestamper.now(), event.getPlayer().getName()));
            event.getPlayer().kickPlayer("You have exceeded the time allowed to play!\n今天的时间已经用完，下次再来吧！#_#");
            // System.out.print(new PlayTimeLimiter().secondsUntilNextDay());
            // 距离明天还有多少时间
        }
        this.plugin.setPlayerLoggedIn(event.getPlayer().getUniqueId());
//        event.getPlayer().sendMessage(
//                "You have " + ChatColor.GREEN + plugin.secondsToDaysHoursSecondsString(
//                        plugin.getTimeAllowedInSeconds(event.getPlayer().getUniqueId())) + ChatColor.RESET
//                        + " of playtime left!");

        // scoreboard, showing the left time of players
        Player player = event.getPlayer();
        ScoreboardManager scoreboardManager = plugin.getServer().getScoreboardManager();
        Scoreboard scoreboard = scoreboardManager.getNewScoreboard();

        final Objective objective = scoreboard.registerNewObjective("timeLeft", "timeLeft");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        final Team timeLeft = scoreboard.registerNewTeam("timeLeft");
        timeLeft.addEntry("bonus");
        timeLeft.setSuffix("");
        timeLeft.setPrefix("");

        new BukkitRunnable() {
            @Override
            public void run() {
                int allowed = plugin.getTimeAllowedInSeconds(player.getUniqueId());
                String timeLeft = plugin.secondsToDaysHoursSecondsString(allowed);
                if (allowed < 5 * 60) {
                    objective.getScore("bonus").setScore(0);
                } else if (allowed < 10 * 60) {
                    objective.getScore("bonus").setScore(1);
                } else {
                    objective.getScore("bonus").setScore(2);
                }
                objective.setDisplayName(timeLeft);
            }
        }.runTaskTimer(plugin, 0, 20);
        player.setScoreboard(scoreboard);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        FileUtils.appendStringToFile(new File(this.plugin.getDataFolder(),
                "playtime.log"), String.format("[%s] %s logged out",
                Timestamper.now(), event.getPlayer().getName()));
        this.plugin.setPlayerLoggedOut(event.getPlayer().getUniqueId());
    }
}
