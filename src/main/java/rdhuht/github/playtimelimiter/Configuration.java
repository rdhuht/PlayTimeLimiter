package rdhuht.github.playtimelimiter;

public class Configuration {

    private final PlayTimeLimiter plugin;

    public Configuration(final PlayTimeLimiter plugin) {
        this.plugin = plugin;
    }

    /**
     * Ensures that the plugin configuration has been populated
     * with default values.
     */
    public void ensureDefaults() {
        if (!this.plugin.getConfig().isSet(Options.INITIAL_TIME)) {
            this.plugin.getConfig().set(Options.INITIAL_TIME, 2400);
            this.plugin.saveConfig();
        }

        if (!this.plugin.getConfig().isSet(Options.TIME_PER_DAY)) {
            this.plugin.getConfig().set(Options.TIME_PER_DAY, 2400);
            this.plugin.saveConfig();
        }

        if (!this.plugin.getConfig().isSet(Options.SECONDS_BETWEEN_CHECKS)) {
            this.plugin.getConfig().set(Options.SECONDS_BETWEEN_CHECKS, 2);
            this.plugin.saveConfig();
        }

        if (!this.plugin.getConfig().isSet(Options.SECONDS_BETWEEN_SAVES)) {
            this.plugin.getConfig().set(Options.SECONDS_BETWEEN_SAVES, 60);
            this.plugin.saveConfig();
        }

        if (!this.plugin.getConfig().isSet(Options.TIME_TRAVELS)) {
            this.plugin.getConfig().set(Options.TIME_TRAVELS, false);
            this.plugin.saveConfig();
        }

        if (!this.plugin.getConfig().isSet(Options.TIME_LEFT_5m)) {
            this.plugin.getConfig().set(Options.TIME_LEFT_5m, "5 minutes! 请保存代码！");
            this.plugin.saveConfig();
        }

        if (!this.plugin.getConfig().isSet(Options.TIME_LEFT_1m)) {
            this.plugin.getConfig().set(Options.TIME_LEFT_1m, "1 minute! 准备退出游戏！");
            this.plugin.saveConfig();
        }

        if (!this.plugin.getConfig().isSet(Options.TIME_LEFT_10s)) {
            this.plugin.getConfig().set(Options.TIME_LEFT_10s, "10s! See you next time!");
            this.plugin.saveConfig();
        }

        if (!this.plugin.getConfig().isSet(Options.RESET_HOUR)) {
            this.plugin.getConfig().set(Options.RESET_HOUR, 24);
            this.plugin.saveConfig();
        }

        if (!this.plugin.getConfig().isSet(Options.RESET_MINUTE)) {
            this.plugin.getConfig().set(Options.RESET_MINUTE, 0);
            this.plugin.saveConfig();
        }

		/*if (!getConfig().isSet("timeCap")) {
			getConfig().set("timeCap", true);
			saveConfig();
		}

		if (!getConfig().isSet("timeCapValue")) {
			getConfig().set("timeCapValue", 18000);
			saveConfig();
		}*/
    }

    /**
     * Gets the time playtime was started on the server.
     *
     * @return The time playtime was started
     */
    public int getTimeStarted() {
        return this.plugin.getConfig().getInt(Options.TIME_STARTED);
    }


    /**
     * Gets the reset time every day in hour.
     *
     * @return The reset hour
     */
    public int getResetHour() {
        return this.plugin.getConfig().getInt(Options.RESET_HOUR);
    }

    /**
     * Gets the reset time every day in minute.
     *
     * @return The reset minute
     */
    public int getResetMinute() {
        return this.plugin.getConfig().getInt(Options.RESET_MINUTE);
    }

    /**
     * Gets the daily play time.
     *
     * @return The daily seconds
     */
    public int getTimePerDay() {
        return this.plugin.getConfig().getInt(Options.TIME_PER_DAY);
    }

    /**
     * A psuedo-enum of all the configuration options in PlayTimeLimiter.
     */
    public static final class Options {

        /**
         * Time playtime was enabled (in seconds past Epoch).
         */
        public static final String TIME_STARTED = "timeStarted";

        /**
         * Initial playtime given (in seconds).
         *
         * <strong>Default</strong>: <em>28800</em>
         */
        public static final String INITIAL_TIME = "initialTime";

        /**
         * Daily playtime increment/reset (in seconds).
         *
         * <strong>Default</strong>: <em>3600</em>
         */
        public static final String TIME_PER_DAY = "timePerDay";

        /**
         * Time between each playtime check (in seconds).
         *
         * <strong>Default</strong>: <em>10</em>
         */
        public static final String SECONDS_BETWEEN_CHECKS = "secondsBetweenPlayTimeChecks";

        /**
         * Time between each playtime save (in seconds).
         *
         * <strong>Default</strong>: <em>600</em>
         */
        public static final String SECONDS_BETWEEN_SAVES = "secondsBetweenPlayTimeSaving";

        /**
         * Whether playtime should increment, rather than reset.
         *
         * <strong>Default</strong>: <em>false</em>
         */
        public static final String TIME_TRAVELS = "timeTravels";
        /**
         * 剩余时间，显示提醒内容
         */
        public static final String TIME_LEFT_5m = "timeleft5m";
        public static final String TIME_LEFT_1m = "timeleft1m";
        public static final String TIME_LEFT_10s = "timeleft10s";
        /**
         * 每天重置的时间点
         */
        public static final String RESET_HOUR = "resethour";
        public static final String RESET_MINUTE = "resetminute";

        private Options() {
        }

    }

}
