/**
 * Copyright 2013-2015 by ATLauncher and Contributors
 * <p>
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
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
