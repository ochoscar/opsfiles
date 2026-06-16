package com.ochoscar.operations.similarity;

public class Progress {

    private final String label;
    private final long total;
    private final long startMillis;
    private long current = 0;
    private long lastPrintMillis = 0;

    public Progress(String label, long total) {
        this.label = label;
        this.total = total;
        this.startMillis = System.currentTimeMillis();
        render();
    }

    public synchronized void tick() {
        current++;
        long now = System.currentTimeMillis();
        if (current >= total || now - lastPrintMillis >= 500) {
            render();
            lastPrintMillis = now;
        }
    }

    public synchronized void newline() {
        System.out.println();
        lastPrintMillis = 0;
    }

    private void render() {
        long elapsed = System.currentTimeMillis() - startMillis;
        double pct = total == 0 ? 100.0 : 100.0 * current / total;
        String eta;
        if (current > 0 && current < total) {
            long etaMs = (long) (elapsed * ((double) (total - current) / current));
            eta = formatDuration(etaMs);
        } else {
            eta = "--:--:--";
        }
        System.out.printf("\r  %s: %d/%d (%.1f%%) elapsed %s ETA %s   ",
                label, current, total, pct,
                formatDuration(elapsed), eta);
        if (current >= total) {
            System.out.println();
        }
    }

    private static String formatDuration(long ms) {
        long s = ms / 1000;
        return String.format("%02d:%02d:%02d", s / 3600, (s / 60) % 60, s % 60);
    }
}
