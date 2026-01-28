package server.services;

import java.util.concurrent.*;

public class GameTimer {
    private long gameStartTime;
    private final long durationMillis;
    private final Runnable timeoutCallback; // L'azione da fare quando scade (rotateGame)
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public GameTimer(long durationMinutes, Runnable timeoutCallback) {
        this.durationMillis = durationMinutes * 60 * 1000;
        this.timeoutCallback = timeoutCallback;
    }

    public void start() {
        // Avvia il task che eseguirà la rotazione ogni X minuti
        scheduler.scheduleAtFixedRate(timeoutCallback, durationMillis, durationMillis, TimeUnit.MILLISECONDS);
    }

    public long getRemainingSeconds() {
        long elapsed = System.currentTimeMillis() - gameStartTime;
        return Math.max(0, (durationMillis - elapsed) / 1000);
    }

    public boolean isExpired() {
        return (System.currentTimeMillis() - gameStartTime) > durationMillis;
    }

    // Per resettare il timestamp quando inizia una nuova partita
    public void resetStartTime() {
        this.gameStartTime = System.currentTimeMillis();
    }
}
