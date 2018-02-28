import java.io.FileNotFoundException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
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
		/*
		 * MenuBar menuBar = new MenuBar(); Menu menuFile = new Menu("File");
		 * MenuItem newItem = new MenuItem("New Game");
		 * 
		 * menuFile.getItems().addAll(newItem);
		 * menuBar.getMenus().addAll(menuFile);
		 */

		// ====Create====

		primaryStage.setTitle("Title");
		VBox verticalBox = new VBox(30);
		Scene scene = new Scene(verticalBox, 400, 400);
		scene.setFill(Color.OLDLACE);

		HBox controlButtons = new HBox(40);
		controlButtons.getChildren().addAll(startButton, resetButton);
		controlButtons.setAlignment(Pos.CENTER);

		verticalBox.getChildren().addAll(txt, controlButtons, viewPrevious);
		verticalBox.setAlignment(Pos.CENTER);

		primaryStage.setScene(scene);
		primaryStage.show();
	}

	@Override
	public void stop() {
		System.out.println("Stage is closing");
	}
}
