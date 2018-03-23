import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

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
	public boolean startTime() {
		return timeModel.startTime();
	}

	/**
	 * 
	 * @return True if the time was started (aka time was started previously),
	 *         false otherwise
	 */
	public boolean stopTime() {
		return timeModel.stopTime();
	}

	public void setSessionName(String sessionName) {
		this.timeModel.setCurrentSessionName(sessionName);
	}

	/**
	 * Resets the TimeModel, removing any TimePairs and "stopping" the time Also
	 * writes the session to the file.
	 */
	public void resetTime() {
		timeModel.stopTime();
		timeModel.resetTime();
		this.saveSessions();
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

	public boolean editSession(int index, long newDurationMillis) {
		try {
			timeModel.getSessions().get(index).getTimePairList();
			List<TimePair> list = new ArrayList<TimePair>();
			long startTime = timeModel.getSessions().get(index).getTimePairList().get(0).getStartTime();
			long endTime = startTime + newDurationMillis;
			list.add(new TimePair(startTime, endTime));
			timeModel.getSessions().get(index).setTimePairList(list);
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
		return true;
	}

	public boolean deleteSession(int index) {
		try {
			timeModel.getSessions().remove(index);
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
		return true;
	}

	/**
	 * Writes the time pairs of the current session to a file in CSV format.
	 * 
	 * @return true if file is written, false otherwise.
	 */
	public boolean saveSessions() {
		return timeModel.saveSessions();
	}
	
	public boolean writeToReadableFile(LocalDate start, LocalDate end){
		return timeModel.writeToReadableFile(start, end);
	}
	
	/**
	 * @return
	 */
	public boolean writeToReadableFile() {
		return timeModel.writeToReadableFile();
	}
	
	

}
