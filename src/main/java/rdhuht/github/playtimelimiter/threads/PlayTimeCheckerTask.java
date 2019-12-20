package rdhuht.github.playtimelimiter.threads;

import java.io.File;
import java.lang.reflect.Field;

import net.minecraft.server.v1_12_R1.ChatComponentText;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerListHeaderFooter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import rdhuht.github.playtimelimiter.PlayTimeLimiter;
import rdhuht.github.playtimelimiter.utils.FileUtils;
import rdhuht.github.playtimelimiter.utils.Timestamper;

import static rdhuht.github.playtimelimiter.Configuration.Options.*;


public class PlayTimeCheckerTask implements Runnable {
    private final PlayTimeLimiter plugin;

    public PlayTimeCheckerTask(PlayTimeLimiter instance) {
        this.plugin = instance;
    }

    PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();

    public void run() {
        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            int timeLeft = this.plugin.getTimeAllowedInSeconds(player
                    .getUniqueId());
            if (timeLeft <= 0) {
                FileUtils.appendStringToFile(
                        new File(this.plugin.getDataFolder(), "playtime.log"),
                        String.format(
                                "[%s] %s was kicked for exceeding play time",
                                Timestamper.now(), player.getName()));
                player.kickPlayer("You have exceeded the time allowed to play! 超时，下次再来吧!");
            } else if (timeLeft <= 10
                    && timeLeft >= 8
                    && !this.plugin.hasPlayerSeenMessage(player.getUniqueId(),
                    10)) {
                player.sendTitle("Time", ChatColor.GREEN + plugin.getConfig().getString(TIME_LEFT_10s), 10, 20, 10);
            } else if (timeLeft <= 60
                    && timeLeft >= 58
                    && !this.plugin.hasPlayerSeenMessage(player.getUniqueId(),
                    60)) {
                player.sendTitle("Time", ChatColor.GREEN + plugin.getConfig().getString(TIME_LEFT_1m), 10, 20, 10);
            } else if (timeLeft <= 300
                    && timeLeft >= 298
                    && !this.plugin.hasPlayerSeenMessage(player.getUniqueId(),
                    300)) {
                player.sendTitle("Time", ChatColor.GREEN + plugin.getConfig().getString(TIME_LEFT_5m), 10, 20, 10);
            }

        }
    }
}
