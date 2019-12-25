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
        event.getPlayer().sendMessage(
                "You have " + ChatColor.GREEN + plugin.secondsToDaysHoursSecondsString(
                        plugin.getTimeAllowedInSeconds(event.getPlayer().getUniqueId())) + ChatColor.RESET
                        + " of playtime left!");

        Player player = event.getPlayer();
        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
//        String title = plugin.getServer().getServerName();
        String title = plugin.getServer().getName();

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
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        FileUtils.appendStringToFile(new File(this.plugin.getDataFolder(),
                "playtime.log"), String.format("[%s] %s logged out",
                Timestamper.now(), event.getPlayer().getName()));
        this.plugin.setPlayerLoggedOut(event.getPlayer().getUniqueId());
    }
}
