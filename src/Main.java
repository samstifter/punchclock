import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
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

	private List<String> appList = new ArrayList<String>();

	public static void main(String[] args) {
		timeModel = new TimeModel();
		timeView = new TimeView();
		launch(args);
	}

	public void startUpdateCurrentSessionVisibleTime() {
		Runnable task = new Runnable() {
			public void run() {
				updateCurrentSessionVisibleTime();
			}
		};
		Thread background = new Thread(task);
		background.setDaemon(true);
		background.start();
	}

	/**
	 * This is a background process that continiously updates the visible timer
	 */
	public void updateCurrentSessionVisibleTime() {
		while (true) {
			try {
				Platform.runLater(new Runnable() {

					int durationSeconds;
					int durationMinutes;
					int durationHours;

					@Override
					public void run() {
						int seconds = (int) timeController.getCurrentSessionElapsedTime() / 1000;
						durationSeconds = seconds % 60;
						durationMinutes = (seconds / 60) % 60;
						durationHours = (seconds / 60) / 60;

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

	public void startGetAppWindows() {
		Runnable task = new Runnable() {
			public void run() {
				getAppWindows();
			}
		};
		Thread background = new Thread(task);
		background.setDaemon(true);
		background.start();
	}

	/**
	 * Uses Powershell command to find a list of applications running processes
	 * that also have a visible window. Updates the appList
	 *
	 */
	public void getAppWindows() {
		while (true) {
			List<String> fetchedList = null;
			String listCommand = "powershell -command \" Get-Process | where {$_.mainWindowTitle} | Format-Table name";
			try {
				String line;
				int outLen = 79;
				Process p = Runtime.getRuntime().exec(listCommand);
				BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				line = input.readLine();
				line = input.readLine();
				line = input.readLine();
				fetchedList = new ArrayList<String>();
				fetchedList.add("NONE");
				while (line != null && outLen > 0) {
					line = input.readLine().trim().toLowerCase();
					outLen = line.length();
					if (outLen != 0) {
						fetchedList.add(line);
					}
				}
				input.close();
				appList = fetchedList;

				Thread.sleep(1000);
			} catch (Exception err) {
				err.printStackTrace();
			}
		}
	}

	@Override
	public void start(Stage primaryStage) {
		// =====Setup Time Components=====
		timeController = new TimeController(timeModel, timeView);

		// =====Create UI Elements====
		txt = new Text("0:00:00");
		txt.setFont(new Font(45));

		Button startButton = new Button("Start");
		startButton.setFont(new Font(15));

		Button resetButton = new Button("Reset");
		resetButton.setFont(new Font(15));

		Text applicationListTitle = new Text("Application to Track");

		ComboBox<String> applicationList = new ComboBox<String>();
		applicationList.setValue("NONE");

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
		});

		resetButton.setOnAction(a -> {
			startButton.setText("Start");
			timeController.stopTime();
			timeController.resetTime();
		});

		applicationList.setOnShowing(a -> {
			ObservableList<String> currentAppList = FXCollections
					.observableArrayList(appList.subList(0, appList.size()));
			applicationList.setItems(currentAppList);
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

			Button editButton = new Button("Edit");
			editButton.setDisable(true);

			// Delete button
			Button deleteButton = new Button("Delete");
			deleteButton.setDisable(true);

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

			logs.setOnMouseClicked(b -> {
				if (logs.getSelectionModel().getSelectedItem() != null) {
					editButton.setDisable(false);
					deleteButton.setDisable(false);
					// DEBUG System.out.println(logs.getSelectionModel().getSelectedIndex());
				}
			});

			// edit button handler
			editButton.setOnAction(b -> {
				Stage editLogWindow = new Stage();
				editLogWindow.setTitle("Edit Session");
				editLogWindow.initModality(Modality.APPLICATION_MODAL);

				Text editTitle = new Text("Edit Session");
				editTitle.setFont(new Font(18));

				Text hoursTitle = new Text("Hours");
				Text minutesTitle = new Text("Minutes");
				Text secondsTitle = new Text("Seconds");

				Session editingSession = timeModel.getSessions().get(logs.getSelectionModel().getSelectedIndex());
				int initialSeconds = editingSession.getDuration().get(2);
				int initialMinutes = editingSession.getDuration().get(1);
				int initialHours = editingSession.getDuration().get(0);

				// creating spinners
				SpinnerValueFactory<Integer> hourValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0,
						23, initialHours);
				hourValueFactory.setWrapAround(true);
				SpinnerValueFactory<Integer> minuteValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0,
						59, initialMinutes);
				minuteValueFactory.setWrapAround(true);
				SpinnerValueFactory<Integer> secondValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0,
						59, initialSeconds);
				secondValueFactory.setWrapAround(true);

				Spinner<Integer> hourSpinner = new Spinner<Integer>(hourValueFactory);
				hourSpinner.setPrefWidth(50);
				
				Spinner<Integer> minuteSpinner = new Spinner<Integer>(minuteValueFactory);
				minuteSpinner.setPrefWidth(50);
				
				Spinner<Integer> secondSpinner = new Spinner<Integer>(secondValueFactory);
				secondSpinner.setPrefWidth(50);

				// confirm button and its handler
				Button confirmEdit = new Button("Confirm Edit");

				confirmEdit.setOnAction(c -> {

					long newDuration = hourSpinner.getValue() * 3600000 + minuteSpinner.getValue() * 60000
							+ secondSpinner.getValue() * 1000;

					timeController.editSession(logs.getSelectionModel().getSelectedIndex(), newDuration);
					timeController.saveSessions();

					logs.setItems(FXCollections.observableArrayList(timeModel.getFormattedSessionList()));
					deleteButton.setDisable(true);
					editButton.setDisable(true);
					timeController.saveSessions();
					editLogWindow.close();
				});

				// setting up the layout for editLogWindow
				VBox layout = new VBox(35);
				layout.setAlignment(Pos.CENTER);
				
				GridPane spinners = new GridPane();
				spinners.setAlignment(Pos.CENTER);
				spinners.setHgap(25);
				
				spinners.add(hoursTitle, 0, 0);
				spinners.add(minutesTitle, 1, 0);
				spinners.add(secondsTitle, 2, 0);
				spinners.add(hourSpinner, 0, 1);
				spinners.add(minuteSpinner, 1, 1);
				spinners.add(secondSpinner, 2, 1);

				layout.getChildren().addAll(editTitle, spinners, confirmEdit);

				// starting up the scene
				editLogWindow.setScene(new Scene(layout, 300, 200));
				editLogWindow.setResizable(false);
				editLogWindow.show();

			});

			// delete button handler
			deleteButton.setOnAction(b -> {
				int logIndex = (logs.getSelectionModel().getSelectedIndex());
				timeController.deleteSession(logIndex);
				logs.setItems(FXCollections.observableArrayList(timeModel.getFormattedSessionList()));
				deleteButton.setDisable(true);
				editButton.setDisable(true);
				timeController.saveSessions();
			});

			// Export Button Handler
			exportButton.setOnAction(b -> {
				if (enableDateRange.isSelected()) {
					// Insert code for dated export
					timeController.writeToReadableFile(startDate.getValue(), endDate.getValue());
				} else {
					timeController.writeToReadableFile();
				}
			});

			enableDateRange.setOnAction(b -> {
				startDate.setDisable(!enableDateRange.isSelected());
				endDate.setDisable(!enableDateRange.isSelected());
			});

			// Make a layout, and add everything to it, centered
			VBox layout = new VBox(10);
			HBox dates = new HBox(15);
			HBox deleteEditButtons = new HBox(15);
			deleteEditButtons.getChildren().addAll(deleteButton, editButton);
			deleteEditButtons.setAlignment(Pos.CENTER);
			dates.getChildren().addAll(startDate, endDate);
			dates.setAlignment(Pos.CENTER);
			layout.getChildren().addAll(title, logs, deleteEditButtons, enableDateRange, dates, exportButton);
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

		VBox applicationSelect = new VBox(3);
		applicationSelect.getChildren().addAll(applicationListTitle, applicationList);
		applicationSelect.setAlignment(Pos.CENTER);

		verticalBox.getChildren().addAll(menuBar, txt, controlButtons, applicationSelect, viewPrevious);
		verticalBox.setAlignment(Pos.TOP_CENTER);

		// ====Start Background Threads====
		startUpdateCurrentSessionVisibleTime();
		startGetAppWindows();

		primaryStage.setScene(scene);
		primaryStage.show();

		// Close all other windows when primary stage gets closed.
		primaryStage.setOnCloseRequest(a -> {
			Platform.exit();
		});
	}

	@Override
	public void stop() {
		System.out.println("Stage is closing");
	}
}
