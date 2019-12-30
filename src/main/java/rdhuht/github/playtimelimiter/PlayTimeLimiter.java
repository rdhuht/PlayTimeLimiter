package rdhuht.github.playtimelimiter;


import com.google.common.primitives.Ints;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import rdhuht.github.playtimelimiter.exceptions.UnknownPlayerException;
import rdhuht.github.playtimelimiter.metrics.MetricsLite;
import rdhuht.github.playtimelimiter.threads.PlayTimeCheckerTask;
import rdhuht.github.playtimelimiter.threads.PlayTimeSaverTask;
import rdhuht.github.playtimelimiter.threads.ShutdownThread;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;

import static rdhuht.github.playtimelimiter.Configuration.Options.*;


public class PlayTimeLimiter extends JavaPlugin {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final PlayTimeListener playerListener = new PlayTimeListener(this);
    private final Configuration configuration = new Configuration(this);
    private Map<String, Integer> timePlayed = new HashMap<>();
    private Map<String, Integer> timeLoggedIn = new HashMap<>();
    private Map<String, Boolean> seenWarningMessages = new HashMap<>();

    private boolean shutdownHookAdded = false;
    private boolean started = false;

    @Override
    public void onDisable() {
        // Save the playtime to file on plugin disable
        this.savePlayTime();
        // Remove tasks from Scheduler
        this.getServer().getScheduler().cancelTasks(this);
    }

    @Override
    public void onEnable() {
        if (!this.shutdownHookAdded) {
            this.shutdownHookAdded = true;
            try {
                Runtime.getRuntime().addShutdownHook(new ShutdownThread(this));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 注册事件
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(playerListener, this);

        // 注册命令
        PlayTimeCommand playTimeCommand = new PlayTimeCommand(this);
        getCommand("playtime").setExecutor(playTimeCommand);
        getCommand("playtime").setTabCompleter(playTimeCommand);

        // 配置文件
        this.started = this.getConfig().isSet(Configuration.Options.TIME_STARTED);
        this.configuration.ensureDefaults();

        // 插件开始运行
        this.getLogger().info(
                String.format("Server started at %s which was %s ago!",
                        this.configuration.getTimeStarted(),
                        this.secondsToDaysHoursSecondsString(
                                Ints.checkedCast(Instant.now().getEpochSecond()) - this.configuration.getTimeStarted()
                        )
                )
        );

        // PlayTimeLimiter v{} is enabled!
        final PluginDescriptionFile descriptor = this.getDescription();
        getLogger().info("PlayTimeLimiter v" + descriptor.getVersion() + " is enabled!");

        // 加载记录玩耍时间文件
        this.loadPlayTime();

        // Tasks
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this,
                new PlayTimeSaverTask(this), 20,
                getConfig().getInt(SECONDS_BETWEEN_SAVES) * 20);

        this.getServer().getScheduler().scheduleSyncRepeatingTask(this,
                new PlayTimeCheckerTask(this), 20,
                getConfig().getInt(SECONDS_BETWEEN_CHECKS) * 20);

        // Task3 每天固定时间删除json文件里的hashmap数据
        int h = this.configuration.getResetHour();
        int m = this.configuration.getResetMinute();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, h);
        calendar.set(Calendar.MINUTE, m);
        calendar.set(Calendar.SECOND, 0);
        Date start_time = calendar.getTime();
        Timer timer = new Timer();

        this.getLogger().info("Daily RESET TIME SET AT >>> " + h + ':' + m + " <<<");
        this.getLogger().info("每日  重置   时间  为 >>> " + h + '点' + m + "分 <<<");
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                System.out.println("----------------------------");
                getLogger().info("Daily RESET TIME NOW >>> " + h + ':' + m + " <<<");
                getLogger().info("现在重置时间！大赦天下啦！ >>> " + h + '点' + m + "分 <<<");
                System.out.println("----------------------------");
                resetAllPlayTime();
            }
        }, start_time, 1000 * 60 * 60 * 24);

        // Metrics
        try {
            final MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        } catch (
                final IOException ex) {
            this.getLogger().log(Level.INFO, "Failed to send Metrics data!", ex);
        }
    }

    public int secondsUntilNextDay() {
        final int timeStarted = this.configuration.getTimeStarted();
        int secondsSince = (int) ((System.currentTimeMillis() / 1000) - timeStarted);

        while (secondsSince >= 86400) {
            secondsSince -= 86400;
        }

        return secondsSince;
    }

    public String secondsToDaysHoursSecondsString(int secondsToConvert) {
        final int hours = secondsToConvert / 3600;
        final int minutes = (secondsToConvert % 3600) / 60;
        final int seconds = secondsToConvert % 60;
        return String.format("%02d : %02d : %02d", hours,
                minutes, seconds);
    }

    public int getTimeAllowedInSeconds() {
        int timeStarted = this.configuration.getTimeStarted();
        int secondsSince = (int) ((System.currentTimeMillis() / 1000) - timeStarted);
        int secondsAllowed = 0;

        // Add the initial time we give the player at the beginning
        secondsAllowed += getConfig().getInt(INITIAL_TIME);

        // Then for each day including the first day (24 hours realtime) add the
        // set amount of
        // seconds to the time allowed
        while (secondsSince >= 0) {
            if (getConfig().getBoolean(TIME_TRAVELS)) {
                secondsAllowed += getConfig().getInt(TIME_PER_DAY);
            } else {
                secondsAllowed = getConfig().getInt(TIME_PER_DAY);
            }
            secondsSince -= 86400;
        }

        return secondsAllowed;
    }

    public int getTimeAllowedInSeconds(UUID uuid) {
        int secondsAllowed = this.getTimeAllowedInSeconds();

        // Remove the amount of time the player has played to get their time
        // allowed
        secondsAllowed -= getPlayerPlayTime(uuid);

        return secondsAllowed;
    }

    public void addPlayTime(UUID uuid, int seconds)
            throws UnknownPlayerException {
        if (this.timePlayed.containsKey(uuid.toString())) {
            this.timePlayed.put(uuid.toString(), this.timePlayed.get(uuid.toString())
                    - seconds);
        } else {
            throw new UnknownPlayerException(uuid);
        }
    }

    public void removePlayTime(UUID uuid, int seconds)
            throws UnknownPlayerException {
        if (this.timePlayed.containsKey(uuid.toString())) {
            this.timePlayed.put(uuid.toString(), this.timePlayed.get(uuid.toString())
                    + seconds);
        } else {
            throw new UnknownPlayerException(uuid);
        }
    }

    public void setPlayTime(UUID uuid, int seconds) throws UnknownPlayerException {
        if (this.timePlayed.containsKey(uuid.toString())) {
            this.timePlayed.put(uuid.toString(), seconds);
        } else {
            throw new UnknownPlayerException(uuid);
        }
    }

    //重制所有玩家的玩耍时间
    public void resetAllPlayTime() {
        timePlayed.clear();
    }


    public int getPlayerPlayTime(UUID uuid) {
        int timePlayed = 0;
        if (this.timePlayed.containsKey(uuid.toString())) {
            timePlayed += this.timePlayed.get(uuid.toString());
        }
        if (this.timeLoggedIn.containsKey(uuid.toString())) {
            timePlayed += (int) ((System.currentTimeMillis() / 1000) - this.timeLoggedIn
                    .get(uuid.toString()));
        }
        return timePlayed;
    }

    public void setPlayerLoggedIn(UUID uuid) {
        if (!this.timePlayed.containsKey(uuid.toString())) {
            this.timePlayed.put(uuid.toString(), 0);
            this.savePlayTime();
        }
        this.timeLoggedIn.put(uuid.toString(),
                (int) (System.currentTimeMillis() / 1000));
    }

    public void setPlayerLoggedOut(UUID uuid) {
        setPlayerLoggedOut(uuid.toString());
    }

    private void setPlayerLoggedOut(String uuid) {
        if (this.timeLoggedIn.containsKey(uuid)) {
            int timePlayed = (int) ((System.currentTimeMillis() / 1000) - this.timeLoggedIn
                    .get(uuid));
            if (this.timePlayed.containsKey(uuid)) {
                timePlayed += this.timePlayed.get(uuid);
            }
            if (timePlayed > this.getTimeAllowedInSeconds()) {
                timePlayed = this.getTimeAllowedInSeconds();
            }
            this.timePlayed.put(uuid, timePlayed);
            this.timeLoggedIn.remove(uuid);
            getLogger().info(
                    "Player " + uuid + " played for a total of " + timePlayed
                            + " seconds!");
            this.savePlayTime();
        }
        if (this.seenWarningMessages.containsKey(uuid + ":10")) {
            this.seenWarningMessages.remove(uuid + ":10");
        }
        if (this.seenWarningMessages.containsKey(uuid + ":60")) {
            this.seenWarningMessages.remove(uuid + ":60");
        }
        if (this.seenWarningMessages.containsKey(uuid + ":300")) {
            this.seenWarningMessages.remove(uuid + ":300");
        }
    }

    public boolean hasPlayerSeenMessage(UUID uuid, int time) {
        if (this.seenWarningMessages.containsKey(uuid.toString() + ":" + time)) {
            return this.seenWarningMessages.get(uuid.toString() + ":" + time);
        } else {
            return false;
        }
    }

    public void sentPlayerWarningMessage(UUID uuid, int time) {
        this.seenWarningMessages.put(uuid.toString() + ":" + time, true);
    }

    public boolean start() {
        if (this.started) {
            return false;
        } else {
            this.started = true;
//            String initial = (getConfig().getInt(INITIAL_TIME) / 60 / 60) + "";
            String perday = (getConfig().getInt(TIME_PER_DAY) / 60) + "";
            getServer().broadcastMessage(
                    ChatColor.GREEN + "PlayTimeLimiter has now started!\nYou have " + perday + " minute/s of playtime per day!");
            getConfig().set("timeStarted", Ints.checkedCast(Instant.now().getEpochSecond()));
            saveConfig();
            return true;
        }
    }

    public boolean stop() {
        if (!this.started) {
            return false;
        } else {
            this.started = false;
            return true;
        }
    }

    public boolean hasStarted() {
        return this.started;
    }

    public void loadPlayTime() {
        if (!hasStarted()) {
            return;
        }
        File file = new File(getDataFolder(), "playtime.json");
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        if (!file.exists()) {
            getLogger().warning(
                    "playtime.json file missing! Not loading in values");
            return;
        }
        getLogger().info("Loading data from playtime.json");
        FileReader fileReader;
        try {
            fileReader = new FileReader(file);
            java.lang.reflect.Type type = new TypeToken<Map<String, Integer>>() {
            }.getType();
            this.timePlayed = GSON.fromJson(fileReader, type);
            fileReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void savePlayTime() {
        this.savePlayTime(false);
    }

    public void savePlayTime(boolean force) {
        if (!hasStarted()) {
            return;
        }

        if (force) {
            for (String key : this.timeLoggedIn.keySet()) {
                this.setPlayerLoggedOut(key);
            }
        }
        File file = new File(getDataFolder(), "playtime.json");
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        getLogger().info("Saving data to playtime.json");
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            bw.write(GSON.toJson(this.timePlayed));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (bw != null) {
                bw.close();
            }
            if (fw != null) {
                fw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}