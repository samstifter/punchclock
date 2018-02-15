import java.util.ArrayList;
import java.util.List;

/**
 * The TimeModel class is the Model component of the MVC framework. 
 * It acts as a storage class for all data pertaining to the time 'structure'
 * Read more about MVC architecture <a href='https://www.tutorialspoint.com/design_pattern/mvc_pattern.htm'>here</a>
 * @author A7
 *
 */
public class TimeModel {
    private long lastStart;
    private boolean isStarted;
    private List<TimePair> timePairList;

    public TimeModel() {
        lastStart = 0;
        timePairList = new ArrayList<TimePair>();
    }

    /**
     *
     * @return Total Elapsed milliseconds between all starts/stops
     */
    public long getTotalElapsedTime() {
        long totalTime = 0;
        for(TimePair tp : timePairList) {
            totalTime += tp.getElapsedTime();
        }
        if(lastStart != 0)
            totalTime += getCurrentElapsedTime();
        return totalTime;
    }

    /**
     * Get the time of only the most recent session
     * @return the elapsed time of the most recent session
     */
    public long getCurrentElapsedTime() {
        return System.currentTimeMillis() - lastStart;
    }

    /**
     * Starts the timer by recording the current time
     */
    public void startTime() {
        lastStart = System.currentTimeMillis();
        isStarted = true;
    }

    /**
     *
     * @return True if the time was started (aka time was started previously), false otherwise
     */
    public boolean stopTime() {
        if(isStarted) {
            timePairList.add(new TimePair(lastStart,System.currentTimeMillis()));
            lastStart = 0;
            isStarted = false;
        }
        else {
            return false; // A Timer that is not started can not be stopped
        }
        return true;
    }

    /**
     * Resets the TimeModel, removing any TimePairs and "stopping" the time
     */
    public void resetTime() {
        isStarted = false;
        lastStart = 0;
        timePairList.clear();
    }
}