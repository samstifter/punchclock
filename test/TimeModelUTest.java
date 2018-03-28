import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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

    @Test
    public void stopTime_timeNotStarted() {
        assertFalse(timeModel.stopTime());
    }

    @Test
    public void stopTime_timeStarted_timePairListSize() throws Exception {
        TimePair pair0 = new TimePair(0, 20);
        currSession.addTimePair(pair0);
        TimePair pair1 = new TimePair(40, 80);
        currSession.addTimePair(pair1);
        timeModel.setCurrentSession(currSession);
        timeModel.startTime();
        Thread.sleep(40);
        timeModel.stopTime();
        assertEquals(3, currSession.getTimePairList().size());
        timeModel.startTime();
        Thread.sleep(40);
        timeModel.stopTime();
        assertEquals(4, currSession.getTimePairList().size());
    }

    @Test
    public void stopTime_timeStarted() {
        timeModel.startTime();
        assertTrue(timeModel.stopTime());
    }

    @Test
    public void stopTime_illegalArgumentException() throws Exception {
        timeModel.setCurrentSession(currSession);
        timeModel.startTime();
        timeModel.setLastStart(Integer.MAX_VALUE);
        Thread.sleep(40);
        timeModel.stopTime();
        assertFalse(timeModel.stopTime());
    }

    @Test
    public void resetTime() throws Exception {
        timeModel.setCurrentSession(currSession);
        timeModel.startTime();
        Thread.sleep(40);
        timeModel.resetTime();
        assertNotEquals(timeModel.getCurrentSession(), currSession);
        assertEquals(0, timeModel.getLastStart());
        assertFalse(timeModel.isStarted());
    }

    @Test
    public void getCurrentSession() {
        timeModel.setCurrentSession(currSession);
        assertEquals(currSession, timeModel.getCurrentSession());
    }

    @Test
    public void addSession() {
        Session session0 = new Session();
        TimePair timePair0 = new TimePair(0, 20);
        List<TimePair> timePairList0 = new ArrayList<>();
        timePairList0.add(timePair0);
        session0.setTimePairList(timePairList0);

        Session session1 = new Session();
        TimePair timePair1 = new TimePair(40, 80);
        List<TimePair> timePairList1 = new ArrayList<>();
        timePairList1.add(timePair1);
        session1.setTimePairList(timePairList1);

        Session session2 = new Session();

        //session0 and session1 are valid, so they should be
        //factored into the size of the sessions
        timeModel.addSession(session0);
        timeModel.addSession(session1);
        //session2 is invalid, so it will not be added to the sessions
        timeModel.addSession(session2);

        assertEquals(2, timeModel.getSessions().size());
    }

    @Test
    public void setCurrentSessionName() {
        currSession.setSessionName("Name");
        timeModel.setCurrentSession(currSession);
        assertEquals("Name", timeModel.getCurrentSession().getSessionName());
        timeModel.setCurrentSessionName("NewName");
        assertEquals("NewName", timeModel.getCurrentSession().getSessionName());
    }

    @Test
    public void isStarted_true() {
        timeModel.startTime();
        assertTrue(timeModel.isStarted());
    }

    @Test
    public void isStarted_false() {
        assertFalse(timeModel.isStarted());
    }

    @Test
    public void getFormattedSessionList() {
        Session session0 = new Session();
        TimePair timePair0 = new TimePair(0, 20);
        List<TimePair> timePairList0 = new ArrayList<>();
        timePairList0.add(timePair0);
        session0.setTimePairList(timePairList0);

        Session session1 = new Session();
        TimePair timePair1 = new TimePair(40, 80);
        List<TimePair> timePairList1 = new ArrayList<>();
        timePairList1.add(timePair1);
        session1.setTimePairList(timePairList1);

        timeModel.addSession(session0);
        timeModel.addSession(session1);

        List<String> list = new ArrayList<String>();
        list.add(session0.toString());
        list.add(session1.toString());

        assertEquals(list, timeModel.getFormattedSessionList());
    }

    @Test
    public void loadSaveSessions() {
        assertTrue(timeModel.loadSavedSessions());
    }

    @Test
    public void getSessions() {
        Session session = new Session();
        TimePair timePair = new TimePair(0, 20);
        List<TimePair> timePairList = new ArrayList<>();
        timePairList.add(timePair);
        session.setTimePairList(timePairList);

        List<Session> sessions = new ArrayList<>();
        sessions.add(session);

        timeModel.addSession(session);

        assertEquals(sessions, timeModel.getSessions());
    }

    @Test
    public void setSessions() {
        Session session = new Session();
        TimePair timePair = new TimePair(0, 20);
        List<TimePair> timePairList = new ArrayList<>();
        timePairList.add(timePair);
        session.setTimePairList(timePairList);

        List<Session> sessions = new ArrayList<>();
        sessions.add(session);

        assertEquals(0, timeModel.getSessions().size());

        timeModel.setSessions(sessions);

        assertEquals(sessions, timeModel.getSessions());
    }

    @Test
    public void setDirectory() {
        assertEquals("output", timeModel.getDirectory());

        timeModel.setDirectory("newDirectory");

        assertEquals("newDirectory", timeModel.getDirectory());
    }

    @Test
    public void setLastStart() {
        timeModel.setLastStart(20);
        assertEquals(20, timeModel.getLastStart());
    }

    @Test
    public void setCurrentSession() {
        timeModel.setCurrentSession(currSession);
        assertEquals(currSession, timeModel.getCurrentSession());
    }

}
