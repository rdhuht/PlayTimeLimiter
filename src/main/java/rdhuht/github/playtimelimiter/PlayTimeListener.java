package rdhuht.github.playtimelimiter;

import java.io.File;
import java.lang.reflect.Field;

import net.minecraft.server.v1_12_R1.ChatComponentText;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerListHeaderFooter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import rdhuht.github.playtimelimiter.exceptions.UnknownPlayerException;
import rdhuht.github.playtimelimiter.utils.FileUtils;
import rdhuht.github.playtimelimiter.utils.Timestamper;

public class PlayTimeListener implements Listener {
    private final PlayTimeLimiter plugin;

    public PlayTimeListener(PlayTimeLimiter instance) {
        this.plugin = instance;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // 先记录进入时间
        FileUtils.appendStringToFile(new File(this.plugin.getDataFolder(),
                "playtime.log"), String.format("[%s] %s logged in",
                Timestamper.now(), player.getName()));
        // 玩家登陆游戏，检查剩余时间
        // 剩余时间不够，踢出服务器并记录踢出的时间点和退出时间
        if (this.plugin.getTimeAllowedInSeconds(event.getPlayer().getUniqueId()) <= 0) {
            FileUtils.appendStringToFile(new File(this.plugin.getDataFolder(), "playtime.log"),
                    String.format("[%s] %s was kicked for exceeding play time",
                            Timestamper.now(), event.getPlayer().getName()));
            event.getPlayer().kickPlayer("You have exceeded the time allowed to play!\n今天的时间已经用完，下次再来吧！#_#");
            FileUtils.appendStringToFile(new File(this.plugin.getDataFolder(),
                    "playtime.log"), String.format("[%s] %s logged out",
                    Timestamper.now(), event.getPlayer().getName()));
            this.plugin.setPlayerLoggedOut(event.getPlayer().getUniqueId());
        } else if (this.plugin.getTimeAllowedInSeconds(event.getPlayer().getUniqueId()) > 0) {
            // 剩余时间如果够的话，告诉玩家还剩多少时间
            event.getPlayer().sendMessage(
                    "You have " + ChatColor.GREEN + plugin.secondsToDaysHoursSecondsString(
                            plugin.getTimeAllowedInSeconds(event.getPlayer().getUniqueId())) + ChatColor.RESET
                            + " of playtime left!");

            PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
            String title = plugin.getServer().getServerName();
            // 在玩家的tablist上显示剩余时间
            new BukkitRunnable() {
                @Override
                public void run() {
                    int allowed = plugin.getTimeAllowedInSeconds(player.getUniqueId());
                    String timeLeft = plugin.secondsToDaysHoursSecondsString(allowed);
                    try {
                        Field a = packet.getClass().getDeclaredField("a");
                        a.setAccessible(true);
                        Field b = packet.getClass().getDeclaredField("b");
                        b.setAccessible(true);
                        Object header1 = new ChatComponentText(title);
                        Object footer = new ChatComponentText(ChatColor.GOLD + timeLeft);
                        a.set(packet, header1);
                        b.set(packet, footer);

                        if (Bukkit.getOnlinePlayers().size() == 0) return;
                        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }.runTaskTimer(plugin, 0, 20);
            this.plugin.setPlayerLoggedIn(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Configuration conf = new Configuration(plugin);
        Player player = event.getPlayer();
        if (player.isOp()) {
            FileUtils.appendStringToFile(new File(this.plugin.getDataFolder(),
                    "playtime.log"), String.format("[%s] %s playedTime reset",
                    Timestamper.now(), "[OP: " + player.getName() + ']'));
            if (!plugin.hasStarted()) {
                player.sendMessage(ChatColor.RED + "Playtime hasn't started yet!");
            } else {
                try {
                    plugin.setPlayTime(player.getUniqueId(), 0); // 恢复每日的游戏时间
                } catch (UnknownPlayerException e) {
                    e.printStackTrace();
                    player.sendMessage(ChatColor.RED + e.getMessage());
                }
            }
        }
        FileUtils.appendStringToFile(new File(this.plugin.getDataFolder(),
                "playtime.log"), String.format("[%s] %s logged out",
                Timestamper.now(), event.getPlayer().getName()));
        this.plugin.setPlayerLoggedOut(event.getPlayer().getUniqueId());
    }
}
