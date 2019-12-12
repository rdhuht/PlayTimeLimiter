package rdhuht.github.playtimelimiter.threads;


import rdhuht.github.playtimelimiter.PlayTimeLimiter;

public class ShutdownThread extends Thread {
    private final PlayTimeLimiter plugin;

    public ShutdownThread(PlayTimeLimiter plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        this.plugin.savePlayTime(true); // Force save playtime when server is
        // shut down
    }
}
