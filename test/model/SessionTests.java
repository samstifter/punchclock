package model;
import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class SessionTests {

	TimePair timePair;
	long start;
	long end;

	Session session;

	@Before
	public void setup() {
		start = Long.parseLong("1521811081008");
		end = Long.parseLong("1521814899008");
		timePair = new TimePair(start, end);

	}

	@Test
	public void testEmptyContructors() {
		session = new Session();
		assertTrue(session.getSessionName().equals(""));
		assertTrue(session.getTimePairList().isEmpty());
		assertEquals(0, session.getTotalTime());
	}

	@Test
	public void testNameConstructor() {
		String name = "testing";
		session = new Session(name);
		assertEquals(name, session.getSessionName());
		assertTrue(session.getTimePairList().isEmpty());
		assertEquals(0, session.getTotalTime());
	}

	@Test
	public void testSessionNaming() {
		session = new Session();
		String name = "testing";
		assertTrue(session.getSessionName().equals(""));
		session.setSessionName(name);
		assertEquals(name, session.getSessionName());
	}

	@Test
	public void testAddTimePairs() {
		session = new Session();
		session.addTimePair(timePair);
		assertEquals(1, session.getTimePairList().size());
		assertTrue(session.getTimePairList().contains(timePair));

		// Test adding a null timepair
		List<TimePair> list = session.getTimePairList().subList(0, session.getTimePairList().size());
		session.addTimePair(null);
		assertEquals(list, session.getTimePairList());
	}

	@Test
	public void testSetSessionList() {
		session = new Session();
		assertTrue(session.getTimePairList().isEmpty());
		List<TimePair> list = new ArrayList<>();
		list.add(timePair);
		session.setTimePairList(list);
		assertFalse(session.getTimePairList().isEmpty());
		assertEquals(list, session.getTimePairList());
	}

	@Test
	public void testGetTotalTime() {
		session = new Session();
		session.addTimePair(timePair);
		session.addTimePair(timePair);
		long time = 2 * timePair.getElapsedTime();
		assertEquals(time, session.getTotalTime());
	}

	@Test
	public void testToString() {
		session = new Session();
		session.addTimePair(timePair);

		// Get Date and format it
		Date startDate = new Date(timePair.getStartTime());
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		// Get total duration
		int seconds = (int) session.getTotalTime() / 1000;
		int minutes = seconds / 60;
		int hours = minutes / 60;

		// Cap them appropriately
		seconds = seconds % 60;
		minutes = minutes % 60;

		StringBuilder builder = new StringBuilder();
		builder.append(formatter.format(startDate));
		builder.append(" - ");
		builder.append(String.format("%d:%02d:%02d", hours, minutes, seconds));
		builder.append(" - ");
		builder.append(session.getSessionName());
		String expected = builder.toString();

		assertEquals(expected, session.toString());
	}

}
