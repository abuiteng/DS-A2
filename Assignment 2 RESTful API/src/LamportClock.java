/**
 * LamportClock provides a simple implementation of a logical clock
 * for synchronizing events across distributed systems. It maintains
 * an integer counter that can be incremented or updated based on
 * received clock values.
 */
public class LamportClock {
    private int clock; // Current value of the clock

    //Constructor to initialize the LamportClock.
    public LamportClock() {
        clock = 0;
    }

    //Increments the clock by one.
    public synchronized void tick() {
        clock++;
    }

    /**
     * Updates the clock based on a received clock value.
     * Ensures the current clock is greater than the maximum of
     * its current value and the received value, incrementing by one.
     */
    public synchronized void receiveAction(int received) {
        clock = Math.max(clock, received) + 1;
    }

    //Returns the current value of the clock.
    public synchronized int getClock() {
        return clock;
    }
}
