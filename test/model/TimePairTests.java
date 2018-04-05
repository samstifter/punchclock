package model;
import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class TimePairTests {

	TimePair timePair;
	long start;
	long end;

	@Before
	public void setup() {
		start = Long.parseLong("1521811081008");
		end = Long.parseLong("1521814899008");
		timePair = new TimePair(start, end);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidFormat() {
		start = Long.parseLong("1521811081008");
		end = Long.parseLong("1521814899008");
		timePair = new TimePair(end, start);
	}

	@Test
	public void testGetStart() {
		assertEquals(start, timePair.getStartTime());
	}

	@Test
	public void testGetEnd() {
		assertEquals(end, timePair.getEndTime());
	}

	@Test
	public void testGetElapsedTime() {
		assertEquals(end - start, timePair.getElapsedTime());
	}

	@Test
	public void testGetDuration() {
		ArrayList<Integer> expected = new ArrayList<>();

		int durationSeconds = (int) timePair.getElapsedTime() / 1000;
		int durationMinutes = durationSeconds / 60;
		int durationHours = durationMinutes / 60;

		// Cap times appropriately
		durationSeconds %= 60;
		durationMinutes %= 60;

		expected.add(durationHours);
		expected.add(durationMinutes);
		expected.add(durationSeconds);

		assertEquals(expected, timePair.getDuration());
	}

	@Test
	public void testToString() {
		// Get Date and format it
		Date startDate = new Date(timePair.getStartTime());
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		// Get total duration
		int seconds = (int) timePair.getElapsedTime() / 1000;
		int minutes = seconds / 60;
		int hours = minutes / 60;

		// Cap times appropriately
		seconds %= 60;
		minutes %= 60;

		StringBuilder builder = new StringBuilder();
		builder.append(formatter.format(startDate));
		builder.append(" - ");
		builder.append(String.format("%d:%02d:%02d", hours, minutes, seconds));
		String expected = builder.toString();

		assertEquals(expected, timePair.toString());
	}
}
