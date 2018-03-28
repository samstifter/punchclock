import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;



public class TimeModelUTest {

    TimeModel timeModel;


    @Before
    public void setup() {
        timeModel = new TimeModel();
    }

    @Test
    public void getLastStart() {
        assertEquals(0, timeModel.getLastStart());
    }

    @Test
    public void getCurrentSessionTime_notStarted_noTotalTime() {
        assertEquals(0, timeModel.getCurrentSessionTime());
    }

}
