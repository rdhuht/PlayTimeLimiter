package rdhuht.github.playtimelimiter.threads;


import rdhuht.github.playtimelimiter.PlayTimeLimiter;

public class PlayTimeSaverTask implements Runnable {
    private final PlayTimeLimiter plugin;

    public PlayTimeSaverTask(PlayTimeLimiter instance) {
        this.plugin = instance;
    }

    public void run() {
        this.plugin.savePlayTime();
    }
}
