import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 * The TimeController class is the Controller component of the MVC framework. 
 * It acts as a 
 * Read more about MVC archecture <a href='https://www.tutorialspoint.com/design_pattern/mvc_pattern.htm'>here</a>
 * @author A7
 *
 */
public class TimeController {
	private TimeModel timeModel;
	private TimeView timeView;
	
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
	 * @return True if the time was started (aka time was started previously), false otherwise
	 */
	public boolean stopTime() {
		return timeModel.stopTime();
	}
	
	/**
	 * Resets the TimeModel, removing any TimePairs and "stopping" the time
	 * Also writes the session to the file. 
	 */
	public void resetTime() {
		timeModel.stopTime();
		this.saveLoggedTime();
		timeModel.resetTime();
	}
	
	/**
	 * Get the total elapsed time over all sessions of this TimeModel
	 * @return Total Elapsed milliseconds between all starts/stops
	 */
	public long getTotalElapsedTime() {
		return timeModel.getCurrentElapsedTime();
	}
	
	/**
	 * Get the time of only the most recent session
	 * @return the elapsed time of the most recent session
	 */
	public long getCurrentElapsedTime() {
		return timeModel.getCurrentElapsedTime();
	}
	
	/**
	 * Displays the total elapsed time to the console
	 * @param timeModel, the model you wish to display the time of
	 */
	public void displayElapsedTimeInSeconds(TimeModel timeModel) {
		timeView.displayElapsedTimeInSeconds(timeModel);
	}
	
	/**
	 * Writes the time pairs of the current session to a file in CSV 
	 * format. 
	 * @return true if file is written, false otherwise.
	 */
	public boolean saveLoggedTime(){
		String filename = "userdata.csv";
		List<TimePair> timePairList = timeModel.getCurrentTimePairList();
		
		PrintWriter out;
		try {
			//Initialize Printwriter, uses filewriter so lines are appended instead of overwritten.
			out = new PrintWriter(new FileWriter(filename, true));
		} catch (IOException e) {
			// If an exception with opening the file happens, return false.
			return false;
		}
		
		//Go through each pair of times
		for (TimePair tp : timePairList){
			//Print both times, each one followed by a comma.
			out.printf("%d,%d,", tp.getStartTime(), tp.getEndTime());
		}
		//  Add a new line at the end of the session.
		out.print("\n");
	
		out.close();
		return true;
	}
	
	public boolean loadLoggedTime(Stage mainStage) {
		FileChooser fileChooser = new FileChooser();
		 fileChooser.setTitle("Open Resource File");
		 fileChooser.getExtensionFilters().addAll(
		         new ExtensionFilter("CSV Files", "*.csv"),
		         new ExtensionFilter("All Files", "*.*"));
		 File selectedFile = fileChooser.showOpenDialog(mainStage);
		 if (selectedFile != null) {
			Scanner key = null;
			try {
				key = new Scanner(selectedFile);
			} catch (FileNotFoundException e) {
				System.err.println("FileNotFoundException");
				e.printStackTrace();
			}
			 List<String> lines = new ArrayList<String>();
			 while(key.hasNextLine()) {
				 lines.add(key.nextLine());
			 }
			 
			 //Checks if all lines have 2 times
			 String[] times;
			 for(String line : lines) {
				 times = line.split(",");
				 System.out.println(times[0].isEmpty());
				 if(times.length != 2 && !times[0].isEmpty()) {
					 System.err.println("File Data Incorrect");
					 return false;
				 }
			 }
			 
			 //saves times as timepairs
			 for(String line : lines) {
				 times = line.split(",");
				 if(!times[0].isEmpty()) {
					 TimePair tp = new TimePair(Long.parseLong(times[0]),Long.parseLong(times[1]));
					 timeModel.addTimePair(tp);
				 }
			 }
			 return true;
		 }
		 System.err.println("File is Null");
		return false;
	}
}
