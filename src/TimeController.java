import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
/**
 * The TimeController class is the Controller component of the MVC framework. It
 * acts as a Read more about MVC archecture <a href=
 * 'https://www.tutorialspoint.com/design_pattern/mvc_pattern.htm'>here</a>
 * 
 * @author A7
 *
 */
public class TimeController {
	private TimeModel timeModel;
	private TimeView timeView;

	/**
	 * @param timeModel
	 * @param timeView
	 */
	public TimeController(TimeModel timeModel, TimeView timeView) {
		this.timeModel = timeModel;
		this.timeView = timeView;
	}

	/**
	 * Starts the timer by recording the current time
	 */
	public void startTime() {
		timeModel.startTime();
	}

	/**
	 * 
	 * @return True if the time was started (aka time was started previously),
	 *         false otherwise
	 */
	public boolean stopTime() {
		return timeModel.stopTime();
	}

	/**
	 * Resets the TimeModel, removing any TimePairs and "stopping" the time Also
	 * writes the session to the file.
	 */
	public void resetTime() {
		timeModel.stopTime();
		this.saveCurrentSession();
		timeModel.resetTime();
	}

	/**
	 * Get the time of only the most recent session
	 * 
	 * @return the elapsed time of the most recent session
	 */
	public long getCurrentSessionElapsedTime() {
		return timeModel.getCurrentSessionTime();
	}

	/**
	 * Displays the total elapsed time to the console
	 * 
	 * @param timeModel,
	 *            the model you wish to display the time of
	 */
	public void displayElapsedTimeInSeconds(TimeModel timeModel) {
		timeView.displayElapsedTimeInSeconds(timeModel);
	}

	/**
	 * Writes the time pairs of the current session to a file in CSV format.
	 * 
	 * @return true if file is written, false otherwise.
	 */
	public boolean saveCurrentSession() {
		File outDir = new File("output");
		File outFile = new File("output/userdata.csv");

		List<TimePair> timePairList = timeModel.getCurrentSession().getTimePairList();

		// Make the directory if it doesn't exist.
		try {
			outDir.mkdir();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

		PrintWriter out;
		try {
			// Initialize Printwriter, uses filewriter so lines are appended
			// instead of overwritten.
			out = new PrintWriter(new FileWriter(outFile, true));
		} catch (IOException e) {
			// If an exception with opening the file happens, return false.
			System.err.println("Could not write file");
			e.printStackTrace();
			return false;
		}

		// Go through each pair of times
		for (TimePair tp : timePairList) {
			// Print both times, each one followed by a comma.
			out.printf("%d,%d,", tp.getStartTime(), tp.getEndTime());
		}
		// Add a new line at the end of the session.
		out.print("\n");

		out.close();
		return true;
	}
	
	/**
	 * Writes the time pairs of the current session to a file in CSV format.
	 * 
	 * @return true if file is written, false otherwise.
	 */
	public boolean saveSessionRange() {
		File outDir = new File("output");
		File outFile = new File("output/userdata.csv");

		List<TimePair> timePairList = timeModel.getCurrentSession().getTimePairList();

		// Make the directory if it doesn't exist.
		try {
			outDir.mkdir();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

		PrintWriter out;
		try {
			// Initialize Printwriter, uses filewriter so lines are appended
			// instead of overwritten.
			out = new PrintWriter(new FileWriter(outFile, true));
		} catch (IOException e) {
			// If an exception with opening the file happens, return false.
			System.err.println("Could not write file");
			e.printStackTrace();
			return false;
		}

		// Go through each pair of times
		for (TimePair tp : timePairList) {
			// Print both times, each one followed by a comma.
			out.printf("%d,%d,", tp.getStartTime(), tp.getEndTime());
		}
		// Add a new line at the end of the session.
		out.print("\n");

		out.close();
		return true;
	}

	/**
	 * @return
	 * @throws FileNotFoundException
	 */
	public boolean writeToReadableFile() throws FileNotFoundException {
		return timeModel.writeToReadableFile();
	}
	
	
	
}
