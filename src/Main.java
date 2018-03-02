import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * 
 *
 */
public class Main extends Application {
	static TimeModel timeModel;
	static TimeView timeView;
	static TimeController timeController;

	Thread timer;
	private boolean startClicked = false;
	Text txt;

	public static void main(String[] args) {
		timeModel = new TimeModel();
		timeView = new TimeView();
		launch(args);
	}

	public void startTask() {
		Runnable task = new Runnable() {
			public void run() {
				runTask();
			}
		};
		Thread background = new Thread(task);
		background.setDaemon(true);
		background.start();
	}

	public void runTask() {
		while (true) {
			try {
				Platform.runLater(new Runnable() {

					int durationSeconds;
					int durationMinutes;
					int durationHours;

					@Override
					public void run() {
						durationSeconds = (int) timeController.getCurrentSessionElapsedTime() / 1000;
						durationMinutes = durationSeconds / 60;
						durationHours = durationMinutes / 60;

						String durationString = String.format("%d:%02d:%02d", durationHours, durationMinutes,
								durationSeconds);

						txt.setText(durationString);
					}
				});

				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// =====Setup Time Components=====
		timeController = new TimeController(timeModel, timeView);

		// =====Create UI Elements====
		txt = new Text("0:00:00");
		txt.setFont(new Font(45));

		Button startButton = new Button("Start");
		startButton.setFont(new Font(15));

		Button resetButton = new Button("Reset");
		resetButton.setFont(new Font(15));

		Button viewPrevious = new Button("View Previous Sessions");
		viewPrevious.setFont(new Font(15));

		// ====Define functionality====

		startButton.setOnAction(a -> {
			startClicked = !startClicked;
			if (startClicked) {
				startButton.setText("Pause");
				timeController.startTime();
				// timer.start();
			} else {
				startButton.setText("Start");
				timeController.stopTime();
				timeController.displayElapsedTimeInSeconds(timeModel);
			}
			startTask();
		});

		resetButton.setOnAction(a -> {
			startButton.setText("Start");
			timeController.stopTime();
			timeController.resetTime();
		});

		viewPrevious.setOnAction(a -> {
			Stage previousLogWindow = new Stage();
			previousLogWindow.setTitle("Previous Session Logs");

			// Add a text title inside the window
			Text title = new Text("Previous Session Logs");
			title.setFont(new Font(18));

			ObservableList<String> logList = FXCollections.observableArrayList(timeModel.getFormattedSessionList());
			ListView<String> logs = new ListView<String>(logList);

			// Set the size for the list
			logs.setPrefHeight(200);
			logs.setPrefWidth(300);

			CheckBox enableDateRange = new CheckBox("Export from a date range");

			DatePicker startDate = new DatePicker();
			startDate.setTooltip(new Tooltip("Start Date"));
			startDate.setDisable(true);
			startDate.setPrefWidth(100);

			DatePicker endDate = new DatePicker();
			endDate.setTooltip(new Tooltip("End Date"));
			endDate.setDisable(true);
			endDate.setPrefWidth(100);

			// Add a button to export the logs
			Button exportButton = new Button("Export Logs");

			// Export Button Handler
			exportButton.setOnAction(b -> {
				if (enableDateRange.isSelected()) {
					// Insert code for dated export
					LocalDate ld = startDate.getValue();
					Instant local = Instant.from(ld.atStartOfDay(ZoneId.systemDefault()));
					Date date1 = Date.from(local);
					long time1 = date1.getTime();
					LocalDate ld2 = startDate.getValue();
					Instant local2 = Instant.from(ld2.atStartOfDay(ZoneId.systemDefault()));
					Date date2 = Date.from(local2);
					long time2 = date2.getTime();
					TimePair timeRange = new TimePair(time1, time2);
					List<Session> sessions = timeModel.getSessions();
					StringBuilder sb = new StringBuilder();
					sb.append("Start Time:");
					sb.append(",");
					sb.append("End Time");
					sb.append(",");
					sb.append("Duration");
					sb.append("\n");
					for (Session session: sessions) {
						List<TimePair> pairs = session.getTimePairList();
						for (TimePair pair: pairs) {
							if ((pair.getStartTime() >= time1 || pair.getStartTime() <=time2) && (pair.getEndTime() >= time1 || pair.getEndTime() <= time2)) {
								Date timeBegin = new Date(pair.getStartTime());
								Date timeEnd = new Date(pair.getEndTime());
								DateFormat dateFormat = new SimpleDateFormat("EEEE MMMM dd yyyy hh:mm:ss a");
								String startTime = dateFormat.format(timeBegin);
								String endTime = dateFormat.format(timeEnd);
								List<Integer> duration = pair.getDuration();
								String durationTime = String.format("%d:%02d:%02d", duration.get(0), duration.get(1), duration.get(2));
								sb.append(startTime);
								sb.append(",");
								sb.append(endTime);
								sb.append(",");
								sb.append(durationTime);
								sb.append("\n");
							}
						}
					}
					String dir = "output/range" + getNumberOfExportedRangeFiles() + ".csv";
					File exportedFile = new File(dir);
					try {
						PrintWriter pw = new PrintWriter(exportedFile);
						pw.write(sb.toString());
						pw.close();
						Runtime.getRuntime().exec("explorer.exe /select," + exportedFile.getAbsolutePath());
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						exportedFile.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					// Implement Export Class
					try {
						timeController.writeToReadableFile();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}
			});

			enableDateRange.setOnAction(b -> {
				startDate.setDisable(!enableDateRange.isSelected());
				endDate.setDisable(!enableDateRange.isSelected());
			});

			// Make a layout, and add everything to it, centered
			VBox layout = new VBox(10);
			HBox dates = new HBox(15);
			dates.getChildren().addAll(startDate, endDate);
			dates.setAlignment(Pos.CENTER);
			layout.getChildren().addAll(title, logs, enableDateRange, dates, exportButton);
			layout.setAlignment(Pos.CENTER);
			previousLogWindow.setScene(new Scene(layout, 300, 400));
			previousLogWindow.show();
		});

		// ----Menu Bar---
		
		 MenuBar menuBar = new MenuBar(); 
		 Menu menuFile = new Menu("File");
		 MenuItem settings = new MenuItem("Settings");
		 settings.setOnAction(e -> {
			 List<String> choices = new ArrayList<>();
			 choices.add("slack");
			 choices.add("discord");
			 choices.add("eclipse");

			 ChoiceDialog<String> dialog = new ChoiceDialog<>("slack", choices);
			 dialog.setTitle("Auto-Timer Setup");
			 dialog.setHeaderText("Pick which application you wish to time");
			 dialog.setContentText("Choose application:");
			 
			 Optional<String> result = dialog.showAndWait();
			 
			 result.ifPresent(letter -> System.out.println("Your choice: " + letter));
		 });
		 
		 menuFile.getItems().addAll(settings);
		 menuBar.getMenus().addAll(menuFile);
		 

		// ====Create====

		primaryStage.setTitle("PunchClock");
		VBox verticalBox = new VBox(30);
		Scene scene = new Scene(verticalBox, 400, 400);
		scene.setFill(Color.OLDLACE);


		HBox controlButtons = new HBox(40);
		controlButtons.getChildren().addAll(startButton, resetButton);
		controlButtons.setAlignment(Pos.CENTER);

		verticalBox.getChildren().addAll(menuBar,txt, controlButtons, viewPrevious);
		verticalBox.setAlignment(Pos.TOP_CENTER);

		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	/**
	 * 
	 * @return
	 * 		number of range files
	 */
	public int getNumberOfExportedRangeFiles() {
		File out = new File("output");
		File[] filesInOutput = out.listFiles();
		int count = 0;
		for (File f: filesInOutput) {
			if (f.getName().contains("range")) {
				count++;
			}
		}
		return ++count;
	}

	@Override
	public void stop() {
		System.out.println("Stage is closing");
	}
}
