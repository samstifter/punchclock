import controller.TimeController;
import model.TimeModel;
import view.TimeView;

/**
 * The Main Class for Punch Clock, this handles initialization && starting the Application
 * @author A7
 *
 */
public class Main {
	
	private static TimeModel timeModel;
	private static TimeView timeView;
	private static TimeController timeController;
	
	private static String osVersion;

	public static void main(String[] args) {
		timeModel = new TimeModel();
		timeView = new TimeView();
		osVersion = System.getProperty("os.name");
		timeView.init(timeModel, timeController,osVersion);
		timeView.startApplication(args);
	}
	
}