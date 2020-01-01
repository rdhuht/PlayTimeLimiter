package rdhuht.github.playtimelimiter.threads;

import java.io.File;

import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerListHeaderFooter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import rdhuht.github.playtimelimiter.PlayTimeLimiter;
import rdhuht.github.playtimelimiter.exceptions.UnknownPlayerException;
import rdhuht.github.playtimelimiter.utils.FileUtils;
import rdhuht.github.playtimelimiter.utils.Timestamper;

import static rdhuht.github.playtimelimiter.Configuration.Options.*;


public class PlayTimeCheckerTask implements Runnable {
    private final PlayTimeLimiter plugin;

    public PlayTimeCheckerTask(PlayTimeLimiter instance) {
        this.plugin = instance;
    }

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
                player.kickPlayer("You have exceeded the time allowed to play! 超时，下次再来吧! 【管理员重新登陆即可！】");
            } else if (timeLeft <= 10
                    && timeLeft >= 8
                    && !this.plugin.hasPlayerSeenMessage(player.getUniqueId(),
                    10)) {
                //如果时间少于10s，op权限的时间自动重置
                player.sendTitle("Left time 剩余时间:", ChatColor.GREEN + plugin.getConfig().getString(TIME_LEFT_10s), 10, 20, 10);
            } else if (timeLeft <= 60
                    && timeLeft >= 58
                    && !this.plugin.hasPlayerSeenMessage(player.getUniqueId(),
                    60)) {
                player.sendTitle("Left time 剩余时间:", ChatColor.GREEN + plugin.getConfig().getString(TIME_LEFT_1m), 10, 20, 10);
            } else if (timeLeft <= 300
                    && timeLeft >= 298
                    && !this.plugin.hasPlayerSeenMessage(player.getUniqueId(),
                    300)) {
                player.sendTitle("Left time 剩余时间:", ChatColor.GREEN + plugin.getConfig().getString(TIME_LEFT_5m), 10, 20, 10);
            }
        }
    }
}
