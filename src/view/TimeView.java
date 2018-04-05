package view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
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
import java.util.Scanner;

import controller.TimeController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Session;
import model.TimeModel;
import model.TimePair;

/**
 * The TimeView class is the View component of the MVC framework. 
 * It should only contain methods that pertain to the displaying of info
 * The model should be be a parameter of the methods that require it and a copy of the model should NOT be kept as an instance var
 * This may not be needed as the project progresses or may just be updated to display more tastefully
 * Read more about MVC archecture <a href='https://www.tutorialspoint.com/design_pattern/mvc_pattern.htm'>here</a>
 * @author A7
 *
 */
public class TimeView extends Application {
	
	//Text
	private Text timerText;
	private Text miniTimerText;

	//Application List for AutoTiming
	private List<String> appList = new ArrayList<String>();

	//Currently Tracked App for AutoTiming
	private String trackingApp = "NONE";
	
	//Directory for Saving
	private String newDirPath = "output";
	
	//Application States
	private boolean saveButtonReady = false;
	private boolean showAppTrackingHelp = true;
	private boolean showAppTrackingError = false;
	private boolean miniTimerVisible = false;
	
	//Offset for MiniTimer when being dragged
	private double xOffset = 0;
    private double yOffset = 0;
    
    //Layouts
    private VBox miniLayout = new VBox(5);
    
    //MVC Instances
    private static TimeModel timeModel;
	private static TimeView timeView;
	private static TimeController timeController;
	
	//Operating System - To determine if Application can run
	private static String osVersion;

	/**
	 * Initialize the Application
	 * @param timeModel
	 * @param timeController
	 * @param osVersion
	 */
    public void init(TimeModel timeModel, TimeController timeController, String osVersion) {
    	TimeView.timeModel = timeModel;
    	TimeView.timeController = timeController;
    	TimeView.osVersion = osVersion;
    }
    
    /**
     * Start the Application
     * @param args
     */
    public void startApplication(String[] args) {
    	launch(args);
    }
    
	/**
	 * Starts a new thread to update the time in the window
	 */
	private void startUpdateCurrentSessionVisibleTime() {
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
	 * This is a background process that continuously updates the visible timer
	 */
	private void updateCurrentSessionVisibleTime() {
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

						timerText.setText(durationString);
						miniTimerText.setText(durationString);
						//miniTimerText.setFill(Color.GREEN);
					}
				});

				Thread.sleep(50);
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	/**
	 * Starts a new thread to get the list of currently running applications
	 */
	private void startGetAppWindows() {
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
	 * Uses Powershell command to find a list of applications running processes that
	 * also have a visible window. Updates the appList
	 */
	private void getAppWindows() {
		while (true) {
			List<String> fetchedList = new ArrayList<String>();
			fetchedList.add("NONE");
			String listCommand = "powershell -command \" Get-Process | where {$_.mainWindowTitle} | Format-Table name";
			try {
				String line;
				int outLen = 79;
				Process p = Runtime.getRuntime().exec(listCommand);
				BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				line = input.readLine();
				line = input.readLine();
				line = input.readLine();
				while (line != null && outLen > 0) {
					line = input.readLine().trim().toLowerCase();
					outLen = line.length();
					if (outLen != 0) {
						fetchedList.add(line);
					}
				}
				input.close();
				appList = fetchedList.subList(0, fetchedList.size());

			} catch (Exception err) {
				// Unable to run the powershell
				showAppTrackingError = true;
				return;
			}

			if (!appList.contains(trackingApp)) {
				timeController.stopTime();
				this.setUIColor(Color.BLACK,"black");
			} else {
				if (trackingApp != null && !trackingApp.equals("NONE")) {
					timeController.startTime();
					this.setUIColor(Color.GREEN,"green");
				}
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Thread is interrupted, just continue as normal. 
			}
		}
	}

	@Override
	public void start(Stage primaryStage) {
		// =====Setup Time Components=====
		timeController = new TimeController(timeModel, timeView);

		// Set up Stages
		Stage viewMiniTimerWindow = new Stage();

		// =====Create UI Elements====
		timerText = new Text("0:00:00");
		timerText.setFont(new Font(45));

		miniTimerText = new Text("0:00:00");
		miniTimerText.setFont(new Font(45));

		Button startButton = new Button("Start");
		startButton.setFont(new Font(15));

		Button saveButton = new Button("Save");
		saveButton.setFont(new Font(15));
		saveButton.setDisable(true);
		
		Button clearButton = new Button("Clear");
		clearButton.setFont(new Font(15));
		clearButton.setDisable(true);

		CheckBox enableAppTracking = new CheckBox("Enable Application Tracking");

		Text applicationListTitle = new Text("Application to Track");
		Text logNameTitle = new Text("Set Session Name");

		ComboBox<String> applicationList = new ComboBox<String>();
		applicationList.setValue("NONE");
		applicationList.setDisable(true);

		TextField sessionName = new TextField();
		sessionName.setPrefSize(150, 5);
		
		Alert fileWriteAlert = new Alert(AlertType.ERROR, "File was unable to be written.");
		fileWriteAlert.setHeaderText("File Save Error");

		// ====Define functionality====

		startButton.setOnAction(a -> {
			saveButtonReady = true;
			if (!timeModel.isStarted()) {
				this.setUIColor(Color.GREEN,"green");
				startButton.setText("Pause");
				timeController.startTime();
				enableAppTracking.setDisable(true);
			} else {
				this.setUIColor(Color.BLACK,"black");
				startButton.setText("Start");
				timeController.stopTime();
				timeController.displayElapsedTimeInSeconds(timeModel);
				enableAppTracking.setDisable(false);
			}
			saveButton.setDisable(false);
			clearButton.setDisable(false);
		});

		saveButton.setOnAction(a -> {
			this.setUIColor(Color.BLACK,"black");
			startButton.setText("Start");
			applicationList.setValue("NONE");
			timeController.stopTime();
			timeController.endCurrentSession();
			if (!saveSessions()) {
				fileWriteAlert.showAndWait();
				return;
			}
			sessionName.clear();
			saveButton.setDisable(true);
			clearButton.setDisable(true);
		});
		
		clearButton.setOnAction(a -> {
			this.setUIColor(Color.BLACK,"black");
			startButton.setText("Start");
			applicationList.setValue("NONE");
			timeController.stopTime();
			timeController.resetTime();
			sessionName.clear();
			saveButton.setDisable(true);
			clearButton.setDisable(true);
		});

		sessionName.setOnKeyReleased(a -> {
			timeController.setSessionName(sessionName.getText());
		});

		applicationList.setOnShowing(a -> {
			if (!appList.contains(trackingApp)) {
				applicationList.setValue("NONE");
			}
			ObservableList<String> currentAppList = FXCollections
					.observableArrayList(appList);
			applicationList.setItems(currentAppList);
		});

		enableAppTracking.setOnAction(a -> {
			if (enableAppTracking.isSelected()) {
				if (showAppTrackingError) {
					Alert appTrackingError = new Alert(AlertType.ERROR, "Application tracking could not be started. App tracking is disabled.");
					appTrackingError.showAndWait();
					enableAppTracking.setDisable(true);
					enableAppTracking.setSelected(false);
				} else {
					if (showAppTrackingHelp) {
						Alert appTrackingInfo = new Alert(AlertType.INFORMATION, 
								"Application tracking will keep the timer running as long as the selected application is running. " + 
								"Re-opening the appplication, as long as it is still selected in the dropdown, will start the timer again.");
						appTrackingInfo.setHeaderText("Application Tracking");
						appTrackingInfo.setTitle("Application Tracking Info");
						appTrackingInfo.showAndWait();
						showAppTrackingHelp = false;
					}
					startButton.setDisable(true);
					applicationList.setDisable(false);
				}
			} else {
				applicationList.setValue("NONE");
				timeController.stopTime();
				startButton.setDisable(false);
				applicationList.setDisable(true);
				this.setUIColor(Color.BLACK,"black");
			}
		});

		applicationList.setOnAction(a -> {
			trackingApp = applicationList.getValue();
			if (trackingApp != "NONE") {
				saveButton.setDisable(false);
				clearButton.setDisable(false);
			} else {
				timeController.stopTime();
				this.setUIColor(Color.BLACK,"black");
			}
		});

		// ====Menu Bar====
		MenuBar menuBar = new MenuBar();
		Menu menuWindow = new Menu("Window");
		MenuItem previousLogs = new MenuItem("View Previous Logs");
		MenuItem graphs = new MenuItem("View Graphs");
		MenuItem miniTimer = new MenuItem("Show MiniTimer");
		
		previousLogs.setOnAction(a -> {
			previousLogWindow();
		});
		
		graphs.setOnAction(a -> {
			graphsWindow();
		});

		miniTimer.setOnAction(a -> {
			if (miniTimerVisible) {
				miniTimerVisible = false;
				miniTimer.setText("Show MiniTimer");
				viewMiniTimerWindow.hide();
			}
			else {
				miniTimerVisible = true;
				viewMiniTimerWindow.show();
				miniTimer.setText("Hide MiniTimer");
				
				//Set Color Only when window is visible
				if(timeModel.isStarted()) {
					this.setUIColor(Color.GREEN,"green");
				}
				else {
					this.setUIColor(Color.BLACK,"black");
				}
			}		
		});

		menuWindow.getItems().addAll(previousLogs, graphs, miniTimer);
		menuBar.getMenus().addAll(menuWindow);

		// ====Create====

		primaryStage.setTitle("PunchClock");
		primaryStage.setResizable(false);
		VBox verticalBox = new VBox(30);
		Scene scene = new Scene(verticalBox, 400, 400);
		scene.setFill(Color.OLDLACE);

		HBox controlButtons = new HBox(40);
		controlButtons.getChildren().addAll(startButton, saveButton, clearButton);
		controlButtons.setAlignment(Pos.CENTER);

		HBox logNames = new HBox(10);
		logNames.getChildren().addAll(logNameTitle, sessionName);
		logNames.setAlignment(Pos.CENTER);

		VBox applicationSelect = new VBox(3);
		applicationSelect.getChildren().addAll(applicationListTitle, applicationList);
		applicationSelect.setAlignment(Pos.CENTER);

		verticalBox.getChildren().addAll(menuBar, timerText, logNames, controlButtons, enableAppTracking,
				applicationSelect);
		verticalBox.setAlignment(Pos.TOP_CENTER);

		// ====Start Background Thread for Timer====
		startUpdateCurrentSessionVisibleTime();
		
		// Check if OS is windows
		if (osVersion.equals("Windows 10")) {
			startGetAppWindows();
		} else {
			Alert osAlert = new Alert(AlertType.WARNING, "Application Tracking is only compatible with Windows 10. Application tracking will not be enabled.");
			osAlert.showAndWait();
			enableAppTracking.setDisable(true);
		}
		
		// Load save file
		loadSavedSessions();
		
		// Initialize Mini Timer
		miniTimerWindow(viewMiniTimerWindow);
		
		primaryStage.setScene(scene);
		primaryStage.show();

		// Close all other windows when primary stage gets closed.
		primaryStage.setOnCloseRequest(a -> {
			Platform.exit();
		});
	}

	/**
	 * Shows the previous recorded sessions
	 */
	private void previousLogWindow() {
		Stage previousLogWindow = new Stage();
		previousLogWindow.initModality(Modality.APPLICATION_MODAL);
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
		startDate.setPrefWidth(125);

		DatePicker endDate = new DatePicker();
		endDate.setTooltip(new Tooltip("End Date"));
		endDate.setDisable(true);
		endDate.setPrefWidth(125);

		// Add a button to export the logs
		Button exportButton = new Button("Export Logs");

		logs.setOnMouseClicked(b -> {
			if (logs.getSelectionModel().getSelectedItem() != null) {
				editButton.setDisable(false);
				deleteButton.setDisable(false);
				// DEBUG
				// System.out.println(logs.getSelectionModel().getSelectedIndex());
			}
		});

		//Button directoryExportButton = new Button("Change Export Directory");
		//directoryExportButton.setText("Change Export Directory");

		// edit button handler
		editButton.setOnAction(b -> {
			Stage editLogWindow = new Stage();
			editLogWindow.setTitle("Edit Session");
			editLogWindow.initModality(Modality.APPLICATION_MODAL);

			Text editTitle = new Text("Edit Session");
			editTitle.setFont(new Font(18));

			Text editSessionNameTitle = new Text("Set Session Name:");
			editSessionNameTitle.setFont(new Font(15));

			Text hoursTitle = new Text("Hours");
			Text minutesTitle = new Text("Minutes");
			Text secondsTitle = new Text("Seconds");

			Session editingSession = timeModel.getSessions().get(logs.getSelectionModel().getSelectedIndex());
			int initialSeconds = editingSession.getDuration().get(2);
			int initialMinutes = editingSession.getDuration().get(1);
			int initialHours = editingSession.getDuration().get(0);

			// creating spinners
			SpinnerValueFactory<Integer> hourValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23,
					initialHours);
			hourValueFactory.setWrapAround(true);
			SpinnerValueFactory<Integer> minuteValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59,
					initialMinutes);
			minuteValueFactory.setWrapAround(true);
			SpinnerValueFactory<Integer> secondValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59,
					initialSeconds);
			secondValueFactory.setWrapAround(true);

			Spinner<Integer> hourSpinner = new Spinner<Integer>(hourValueFactory);
			hourSpinner.setPrefWidth(75);

			Spinner<Integer> minuteSpinner = new Spinner<Integer>(minuteValueFactory);
			minuteSpinner.setPrefWidth(75);

			Spinner<Integer> secondSpinner = new Spinner<Integer>(secondValueFactory);
			secondSpinner.setPrefWidth(75);

			// creating text field for edit session name
			TextField editSessionName = new TextField(editingSession.getSessionName()); // logs.getSelectionModel()
			editSessionName.setPrefSize(150, 5);

			// confirm button and its handler
			Button confirmEdit = new Button("Confirm Edit");

			confirmEdit.setOnAction(c -> {
				// changing the session name
				editingSession.setSessionName(editSessionName.getText());

				// setting the spinner value to the session
				long newDuration = hourSpinner.getValue() * 3600000 + minuteSpinner.getValue() * 60000
						+ secondSpinner.getValue() * 1000;

				timeController.editSession(logs.getSelectionModel().getSelectedIndex(), newDuration);

				logs.setItems(FXCollections.observableArrayList(timeModel.getFormattedSessionList()));
				deleteButton.setDisable(true);
				editButton.setDisable(true);
				saveSessions();
				editLogWindow.close();
			});

			// setting up the layout for editLogWindow
			VBox layout = new VBox(25);
			layout.setAlignment(Pos.CENTER);

			HBox editSessionBox = new HBox(10);
			editSessionBox.getChildren().addAll(editSessionNameTitle, editSessionName);
			editSessionBox.setAlignment(Pos.CENTER);

			GridPane spinners = new GridPane();
			spinners.setAlignment(Pos.CENTER);
			spinners.setHgap(25);

			spinners.add(hoursTitle, 0, 0);
			spinners.add(minutesTitle, 1, 0);
			spinners.add(secondsTitle, 2, 0);
			spinners.add(hourSpinner, 0, 1);
			spinners.add(minuteSpinner, 1, 1);
			spinners.add(secondSpinner, 2, 1);

			layout.getChildren().addAll(editTitle, editSessionBox, spinners, confirmEdit);

			// starting up the scene
			editLogWindow.setScene(new Scene(layout, 300, 200));
			editLogWindow.setResizable(false);
			editLogWindow.show();

		});

		/*
		directoryExportButton.setOnAction(b -> {
			DirectoryChooser chooser = new DirectoryChooser();
			chooser.setTitle("Select Directory");
			File defaultDirectory = new File("output");
			chooser.setInitialDirectory(defaultDirectory);
			File selectedDirectory = chooser.showDialog(previousLogWindow);
			if (selectedDirectory != null) {
				newDirPath = selectedDirectory.getAbsolutePath();
			}
		});
		*/

		// delete button handler
		deleteButton.setOnAction(b -> {
			int logIndex = (logs.getSelectionModel().getSelectedIndex());
			Alert deleteAlert = new Alert(AlertType.CONFIRMATION, "Are you sure you want to delete this session?");
			deleteAlert.setHeaderText("Delete Confirmation");
			deleteAlert.showAndWait().ifPresent(response -> {
				if (response == ButtonType.OK) {
					if(!timeController.deleteSession(logIndex)){
						Alert deleteErrorAlert = new Alert(AlertType.ERROR, "Failed to delete the specified session");
						deleteErrorAlert.setHeaderText("Delete Error");
						deleteErrorAlert.showAndWait();
					}
					logs.setItems(FXCollections.observableArrayList(timeModel.getFormattedSessionList()));
					deleteButton.setDisable(true);
					editButton.setDisable(true);
					saveSessions();
				}
			});
		});

		// Export Button Handler
		exportButton.setOnAction(b -> {
			Alert successfulExportAlert = new Alert(AlertType.CONFIRMATION, "File Export was Successsful.");
			successfulExportAlert.setTitle("Export Status");
			successfulExportAlert.setHeaderText("Success");
			
			ButtonType showFile = new ButtonType("View");
			ButtonType ok = new ButtonType("Ok", ButtonData.OK_DONE);
			if(osVersion.toLowerCase().contains("windows")) {
				successfulExportAlert.getButtonTypes().setAll(showFile, ok);
			}
			else {
				successfulExportAlert.getButtonTypes().setAll(ok);
			}
			Optional<ButtonType> result = null;
			
			
			//Choose the directory in which to export
			DirectoryChooser chooser = new DirectoryChooser();
			chooser.setTitle("Select Directory");
			File defaultDirectory = new File("output");
			chooser.setInitialDirectory(defaultDirectory);
			File selectedDirectory = chooser.showDialog(previousLogWindow);
			if (selectedDirectory != null) {
				newDirPath = selectedDirectory.getAbsolutePath();
			}
			
			if (enableDateRange.isSelected()) {
				if (startDate.getValue() == null || endDate.getValue() == null) {
					Alert emptyDateAlert = new Alert(AlertType.ERROR, "At least one of your dates was invalid. Please enter valid dates or diasble dated export");
					emptyDateAlert.setHeaderText("Invalid Date");
					emptyDateAlert.showAndWait();
				} else if (startDate.getValue().isBefore(endDate.getValue())) {
					if (writeToReadableFile(startDate.getValue(), endDate.getValue())) {
						result = successfulExportAlert.showAndWait();
					}
				} else {
					Alert invalidDateAlert = new Alert(AlertType.ERROR, "Your start date must come before your end date.");
					invalidDateAlert.setHeaderText("Invalid Date");
					invalidDateAlert.showAndWait();
					startDate.getEditor().clear();
					endDate.getEditor().clear();
				}
			} else {
				if (writeToReadableFile()) {
					result = successfulExportAlert.showAndWait();
				}
			}
			File outFile = new File(newDirPath + "/UserLogs.csv");
			if (result.get() == showFile){
				try {
					Runtime.getRuntime().exec("explorer.exe /select," + outFile.getAbsolutePath());
				} catch (IOException e) {
					// File explorer could not be opened, but write was successful.
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
		HBox deleteEditButtons = new HBox(15);
		deleteEditButtons.getChildren().addAll(deleteButton, editButton);
		deleteEditButtons.setAlignment(Pos.CENTER);
		dates.getChildren().addAll(startDate, endDate);
		dates.setAlignment(Pos.CENTER);
		layout.getChildren().addAll(title, logs, deleteEditButtons, enableDateRange, dates, exportButton);
		layout.setAlignment(Pos.CENTER);
		previousLogWindow.setScene(new Scene(layout, 300, 400));
		previousLogWindow.show();
	}

	/**
	 * Show the graphs window containing previous sessions.
	 */
	private void graphsWindow() {
		Stage graphsWindow = new Stage();
		graphsWindow.initModality(Modality.APPLICATION_MODAL);
		graphsWindow.setTitle("Previous Sessions");

		// Add a text title inside the window
		Text title = new Text("Previous Sessions");
		title.setFont(new Font(18));
		
		final NumberAxis xAxis = new NumberAxis();
        final CategoryAxis yAxis = new CategoryAxis();
        final BarChart<Number, String> bc = new BarChart<Number, String>(xAxis,yAxis);
        bc.setTitle("Session History");
        bc.setLegendVisible(false);
        xAxis.setLabel("Duration(Seconds)");  
        xAxis.setTickLabelRotation(90);
        yAxis.setLabel("Session");
        
		ObservableList<Series<Number, String>> data = getSessionsGraphData(timeController.getSessions());
		
		bc.setData(data);
		
		for (Series<Number, String> series: bc.getData()){
            for (XYChart.Data<Number, String> item: series.getData()){
                item.getNode().setOnMousePressed((MouseEvent event) -> {
                    System.out.println(String.valueOf(item.getYValue()));
                    Session matchingSession = timeController.getSessionByName(String.valueOf(item.getYValue()));
                    if(matchingSession != null) {
                    	TextInputDialog dialog = new TextInputDialog();
                    	dialog.initStyle(StageStyle.UTILITY);
                    	dialog.setTitle("Edit Session Name");
                    	dialog.setHeaderText("Edit Session Name");
                    	dialog.setContentText("New Session Name:");
                    	Optional<String> result = dialog.showAndWait();
                    	result.ifPresent(name -> matchingSession.setSessionName(name));
                    	//Name will need to be re-writen again
                    	//https://stackoverflow.com/questions/16880115/javafx-2-2-how-to-force-a-redraw-update-of-a-listview/25962110
                    	this.forceListRefreshOn(data,timeController.getSessions(),bc);
                    }
                    else {
                    	System.err.println("Error: Session matching name: " + item.getYValue().toString() + ": was not found.");
                    }
                });
            }
        }
		
		//-----------------------------------
		
		Text instructions = new Text("Click on bar to edit name of session");
		
		//-----------------------------------
		
		
		// Make a layout, and add everything to it, centered
		VBox layout = new VBox(10);
		layout.getChildren().addAll(title, bc,instructions);
		layout.setAlignment(Pos.CENTER);
		graphsWindow.setScene(new Scene(layout, 600, 600));
		graphsWindow.show();
	}
	
	/**
	 * Creates but does not show a mini timer window with only the timer.
	 * 
	 * @param window
	 *            The stage for the minitimer
	 */
	private void miniTimerWindow(Stage window) {
		Stage miniTimerWindow = window;
		miniTimerWindow.initStyle(StageStyle.UNDECORATED);
		int height = 60, width = 200;
		Scene miniScene = new Scene(miniLayout, width, height);

		miniLayout.setAlignment(Pos.CENTER);
		miniLayout.getChildren().addAll(miniTimerText);

		miniTimerWindow.setResizable(false);
		miniTimerWindow.setAlwaysOnTop(true);
		miniTimerWindow.setTitle("Timer");
		
		Rectangle2D rect = Screen.getPrimary().getVisualBounds();
		
		miniTimerWindow.setX(rect.getMinX() + rect.getWidth() - width);
		miniTimerWindow.setY(rect.getMinY() + rect.getHeight() - height);
		
		
		miniLayout.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });
		miniLayout.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
            	window.setX(event.getScreenX() - xOffset);
                window.setY(event.getScreenY() - yOffset);
            }
        });
		
		miniTimerWindow.setScene(miniScene);
	}
	
	private void setUIColor(Color c, String color) {
		if (miniTimerVisible) {
			miniLayout.setStyle("-fx-padding: 0;" + 
	                "-fx-border-style: solid inside;" + 
	                "-fx-border-width: 5;" +
	                "-fx-border-insets: 0;" + 
	                "-fx-border-radius: 0;" + 
	                "-fx-border-color: " + color + ";");
			miniTimerText.setFill(c);
		}
		timerText.setFill(c);
	}

	/**
	 * Loads the saved userdata csv. If the file is not present, it aborts, and
	 * if the file is formatted incorrectly, it aborts
	 * 
	 * @return
	 */
	private boolean loadSavedSessions() {
		File saveFile = new File("output/userdata.csv");

		Scanner key;
		try {
			key = new Scanner(saveFile);
		} catch (FileNotFoundException e) {
			return false;
		}
		List<String> sessions = new ArrayList<String>();
		while (key.hasNextLine()) {
			sessions.add(key.nextLine());
		}
		key.close();

		// saves times as timepairs, and add to a session.
		String[] times;
		for (String line : sessions) {
			Session session = new Session();
			times = line.split(",");
			if (!times[0].isEmpty()) {
				for (int i = 0; i < times.length - 1; i += 2) {
					try {
						TimePair tp = new TimePair(Long.parseLong(times[i]), Long.parseLong(times[i + 1]));
						session.addTimePair(tp);
					} catch (Exception e) {
						sessions.clear();
						return false;
					}

				}
				if (times.length % 2 != 0) {
					session.setSessionName(times[times.length - 1]);
				} else {
					session.setSessionName("");
				}
				timeController.addSession(session);
			}
		}
		return true;
	}
	
	/**
	 * Writes the sessions to a human readable format.
	 * 
	 * @return true if the write happens, false otherwise
	 */
	private boolean writeToReadableFile() {
		File outDir = new File(newDirPath);
		File outFile = new File(newDirPath + "/UserLogs.csv");

		Alert fileWriteAlert = new Alert(AlertType.ERROR, "File was unable to be written.");
		fileWriteAlert.setHeaderText("File Save Error");
		
		StringBuilder sb = new StringBuilder();
		sb.append("Session Name:");
		sb.append(",");
		sb.append("Start Time:");
		sb.append(",");
		sb.append("End Time");
		sb.append(",");
		sb.append("Duration");
		sb.append("\n");

		if (timeController.getSessions().size() < 1) {
			Alert noSessionsAlert = new Alert(AlertType.ERROR, "No sessions in the specified date range");
			noSessionsAlert.setHeaderText("Export Error");
			noSessionsAlert.showAndWait();
			return false;
		}
		
		for (Session session : timeController.getSessions()) {
			int sessionSize = session.getTimePairList().size();
			if(sessionSize > 0) {
				TimePair startPair = session.getTimePairList().get(0);
				TimePair endPair = sessionSize > 1 ? session.getTimePairList().get(sessionSize - 1) : startPair;
				Date timeBegin = new Date(startPair.getStartTime());
				Date timeEnd = new Date(endPair.getEndTime());
				DateFormat dateFormat = new SimpleDateFormat("EEEE MMMM dd yyyy hh:mm:ss a");
				String startTime = dateFormat.format(timeBegin);
				String endTime = dateFormat.format(timeEnd);
				List<Integer> duration = session.getDuration();
				String durationTime = String.format("%d:%02d:%02d", duration.get(0), duration.get(1),
						duration.get(2));
				sb.append(session.getSessionName());
				sb.append(",");
				sb.append(startTime);
				sb.append(",");
				sb.append(endTime);
				sb.append(",");
				sb.append(durationTime);
				sb.append("\n");
			}
		}
		
		// Make the directory if it doesn't exist.
		try {
			outDir.mkdir();
		} catch (Exception e) {
			fileWriteAlert.showAndWait();
			return false;
		}

		PrintWriter pw;
		try {
			pw = new PrintWriter(outFile);
		} catch (FileNotFoundException e) {
			fileWriteAlert.showAndWait();
			return false;
		}
		
		pw.write(sb.toString());
		pw.close();
		
		return true;
	}
	
	/**
	 * Writes the sessions from a specified date range to a human readable
	 * format
	 * 
	 * @param start
	 *            Start Date
	 * @param end
	 *            End Date
	 * @return true if the file is written, false otherwise
	 */
	private boolean writeToReadableFile(LocalDate start, LocalDate end) {
		// Set up start Millis
		Instant startDate = Instant.from(start.atStartOfDay(ZoneId.of("UTC")));
		long startMillis = startDate.getEpochSecond() * 1000;

		// Set up end Millis
		end = end.plusDays(1);
		Instant endDate = Instant.from(end.atStartOfDay(ZoneId.of("UTC")));
		long endMillis = endDate.getEpochSecond() * 1000;
		
		File outDir = new File(newDirPath);
		File outFile = new File(newDirPath + "/range" + getNumberOfExportedRangeFiles() + ".csv");

		Alert fileWriteAlert = new Alert(AlertType.ERROR, "File was unable to be written.");
		fileWriteAlert.setHeaderText("File Save Error");

		StringBuilder sb = new StringBuilder();
		sb.append("Session Name:");
		sb.append(",");
		sb.append("Start Time:");
		sb.append(",");
		sb.append("End Time");
		sb.append(",");
		sb.append("Duration");
		sb.append("\n");
		
		int validSessions = 0;
		
		for (Session session : timeController.getSessions()) {
			int sessionSize = session.getTimePairList().size();
			if(sessionSize > 0) {
				TimePair startPair = session.getTimePairList().get(0);
				TimePair endPair = sessionSize > 1 ? session.getTimePairList().get(sessionSize - 1) : startPair;
				if ((startPair.getStartTime() >= startMillis && startPair.getStartTime() <= endMillis)
						|| (endPair.getEndTime() >= startMillis && endPair.getEndTime() <= endMillis)) {
					validSessions++;
					Date timeBegin = new Date(startPair.getStartTime());
					Date timeEnd = new Date(endPair.getEndTime());
					DateFormat dateFormat = new SimpleDateFormat("EEEE MMMM dd yyyy hh:mm:ss a");
					String startTime = dateFormat.format(timeBegin);
					String endTime = dateFormat.format(timeEnd);
					List<Integer> duration = session.getDuration();
					String durationTime = String.format("%d:%02d:%02d", duration.get(0), duration.get(1),
							duration.get(2));
					sb.append(session.getSessionName());
					sb.append(",");
					sb.append(startTime);
					sb.append(",");
					sb.append(endTime);
					sb.append(",");
					sb.append(durationTime);
					sb.append("\n");
				}
			}
		}
		
		if (validSessions < 1) {
			Alert noSessionsAlert = new Alert(AlertType.ERROR, "No sessions to export in the specified range.");
			noSessionsAlert.setHeaderText("Export Error");
			noSessionsAlert.showAndWait();
			return false;
		}
		
		// Make the directory if it doesn't exist.
		try {
			outDir.mkdir();
		} catch (Exception e) {
			fileWriteAlert.showAndWait();
			return false;
		}

		PrintWriter pw;
		try {
			pw = new PrintWriter(outFile);
		} catch (FileNotFoundException e) {
			fileWriteAlert.showAndWait();
			return false;
		}
		
		pw.write(sb.toString());
		pw.close();
		
		return true;
	}
	
	/**
	 * Get the number of range files that already exist
	 * 
	 * @return number of range files
	 */
	private int getNumberOfExportedRangeFiles() {
		File out = new File("output");
		File[] filesInOutput = out.listFiles();
		int count = 0;
		for (File f : filesInOutput) {
			if (f.getName().contains("range")) {
				count++;
			}
		}
		return ++count;
	}
	
	/**
	 * Writes the sessions to a file in CSV format.
	 * 
	 * @return true if file is written, false otherwise.
	 */
	private boolean saveSessions() {
		File outDirNonReadable = new File("output");
		File outFileNonReadable = new File("output/userdata.csv");

		// Make the directory if it doesn't exist.
		try {
			outDirNonReadable.mkdir();
		} catch (Exception e) {
			return false;
		}

		PrintWriter out;
		try {
			out = new PrintWriter(outFileNonReadable);
		} catch (IOException e) {
			// If an exception with opening the file happens, return false.
			return false;
		}

		for (Session session : timeController.getSessions()) {

			// Go through each pair of times
			for (TimePair tp : session.getTimePairList()) {
				// Print both times, each one followed by a comma.
				out.printf("%d,%d,", tp.getStartTime(), tp.getEndTime());
			}
			// Print the session name, if it exists, at the end of the list
			if (session.getSessionName() != null) {
				out.printf("%s,", session.getSessionName());
			}
			// Add a new line at the end of the session.
			out.print("\n");

		}
		out.close();
		return true;
	}
	
	private ObservableList<Series<Number, String>> getSessionsGraphData(List<Session> sessions) {
	    ObservableList<XYChart.Series<Number, String>> answer = FXCollections.observableArrayList();
        
        XYChart.Series<Number, String> series1 = new XYChart.Series<Number, String>();
        series1.setName("Sesson Duration");  
        
        for(Session session : sessions) {
        	series1.getData().add(new XYChart.Data<Number, String>(session.getTotalTime() / 1000, session.getSessionName()));
        }
        
        answer.add(series1);
        
        return answer;
	}
	
	private <T> void forceListRefreshOn(ObservableList<Series<Number, String>> data, List<Session> list, BarChart<Number, String> bc) {
	    data.clear();
	    
	    data = getSessionsGraphData(timeController.getSessions());
		
		bc.setData(data);
		
		for (Series<Number, String> series: bc.getData()){
            for (XYChart.Data<Number, String> item: series.getData()){
                item.getNode().setOnMousePressed(new EventHandler<MouseEvent>() {
                	//https://stackoverflow.com/questions/5107158/how-to-pass-parameters-to-anonymous-class
                	private ObservableList<Series<Number, String>> anonVar;
                    public void handle(MouseEvent me) {
                    	System.out.println(String.valueOf(item.getYValue()));
                        Session matchingSession = timeController.getSessionByName(String.valueOf(item.getYValue()));
                        if(matchingSession != null) {
                        	matchingSession.setSessionName("Test");
                        	forceListRefreshOn(anonVar,timeController.getSessions(),bc);
                        }
                        else {
                        	System.err.println("Error: Session matching name: " + item.getYValue().toString() + ": was not found.");
                        }
                    }
                    private EventHandler<MouseEvent> init(ObservableList<Series<Number, String>> data){
                        anonVar = data;
                        return this;
                    }
                }.init(data));
            }
        }
	}

	/**
	 * Displays the total elapsed time to the console
	 * @param timeModel, the model you wish to display the time of
	 */
	public void displayElapsedTimeInSeconds(TimeModel timeModel) {
		double time = timeModel.getCurrentSessionTime() / 1000.0;
		//System.out.println(time);
		System.out.printf("Total Elapsed Time: %4.2f Seconds\n", time);
	}
}
