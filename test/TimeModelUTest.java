import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;



public class TimeModelUTest {

    TimeModel timeModel;
    Session currSession;


    @Before
    public void setup() {
        timeModel = new TimeModel();
        currSession = new Session();
    }

    @Test
    public void getLastStart() {
        assertEquals(0, timeModel.getLastStart());
    }

    @Test
    public void getCurrentSessionTime_notStarted_noTotalTime() {
        assertEquals(0, timeModel.getCurrentSessionTime());
    }

    @Test
    public void getCurrentSessionTime_notStarted() {
        TimePair pair0 = new TimePair(0, 20);
        currSession.addTimePair(pair0);
        TimePair pair1 = new TimePair(40, 80);
        currSession.addTimePair(pair1);
        timeModel.setCurrentSession(currSession);
        assertEquals(60, timeModel.getCurrentSessionTime());
    }

    @Test
    public void getCurrentSessionTime_started() throws Exception {
        TimePair pair0 = new TimePair(0, 20);
        currSession.addTimePair(pair0);
        TimePair pair1 = new TimePair(40, 80);
        currSession.addTimePair(pair1);
        timeModel.setCurrentSession(currSession);
        timeModel.startTime();
        Thread.sleep(40);
        assertTrue(timeModel.getCurrentSessionTime() > 0);
        assertTrue(timeModel.getCurrentSessionTime() > 60);
    }

    @Test
    public void startTime_timeNotStarted() {
        assertTrue(timeModel.startTime());
    }

    @Test
    public void startTime_timeStarted() {
        timeModel.startTime();
        assertFalse(timeModel.startTime());
    }

}
