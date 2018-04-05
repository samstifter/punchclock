package controller;
import org.junit.Before;
import org.junit.Test;

import model.Session;
import model.TimeModel;
import model.TimePair;
import view.TimeView;

import static org.junit.Assert.*;

import java.util.List;

public class TimeControllerTests {
	TimeController timeController;
	TimeModel timeModel;

	@Before
	public void setup() {
		timeModel = new TimeModel();
		timeController = new TimeController(timeModel, new TimeView());

		timeModel.startTime();

		Session session1 = new Session();
		session1.addTimePair(new TimePair(Long.parseLong("1521725169360"), Long.parseLong("1521725175481")));
		timeModel.addSession(session1);

		Session session2 = new Session();
		session2.addTimePair(new TimePair(Long.parseLong("1521725243750"), Long.parseLong("1521725248422")));
		session2.addTimePair(new TimePair(Long.parseLong("1521725249483"), Long.parseLong("1521725255359")));
		timeModel.addSession(session2);

		Session session3 = new Session();
		session3.addTimePair(new TimePair(Long.parseLong("1521726913313"), Long.parseLong("1521726921392")));
		timeModel.addSession(session3);
	}

	@Test
	public void testEditSession() {
		List<Session> sessionList = timeModel.getSessions();

		int index = sessionList.size() - 1;
		assertFalse(sessionList.get(index).getTotalTime() == 5000);
		timeController.editSession(index, 5000);
		assertEquals(5000, sessionList.get(index).getTotalTime());

		index = sessionList.size() - 2;
		assertFalse(sessionList.get(index).getTotalTime() == 5000);
		timeController.editSession(index, 5000);
		assertEquals(5000, sessionList.get(index).getTotalTime());
	}

	@Test
	public void testDeleteSession() {
		List<Session> sessionList = timeModel.getSessions();

		int indexRemove = sessionList.size() - 1;
		Session toRemove = sessionList.get(indexRemove);
		timeController.deleteSession(indexRemove);
		assertFalse(sessionList.contains(toRemove));

		indexRemove = sessionList.size() - 1;
		toRemove = sessionList.get(indexRemove);
		timeController.deleteSession(indexRemove);
		assertFalse(sessionList.contains(toRemove));

		indexRemove = sessionList.size() - 1;
		toRemove = sessionList.get(indexRemove);
		timeController.deleteSession(indexRemove);
		assertFalse(sessionList.contains(toRemove));

		timeModel.getSessions().clear();
		assertFalse("Cannot remove from an empty list", timeController.deleteSession(0));
		assertFalse("Cannot remove from an empty list", timeController.deleteSession(1));

	}
}
