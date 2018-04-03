import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
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

/**
 *
 *
 */
public class Main extends Application {
	private static TimeModel timeModel;
	private static TimeView timeView;
	private static TimeController timeController;

	private Text timerText;
	private Text miniTimerText;

	private List<String> appList = new ArrayList<String>();

	private String trackingApp = "NONE";
	
	private boolean saveButtonReady = false;
	private static String osVersion;
	
	private double xOffset = 0;
    private double yOffset = 0;
    private VBox miniLayout = new VBox(5);
    private boolean miniTimerVisible = false;

	public static void main(String[] args) {
		timeModel = new TimeModel();
		timeView = new TimeView();
		timeModel.loadSavedSessions();
		osVersion = System.getProperty("os.name");
		launch(args);
	}

	/**
	 * Starts a new thread to update the time in the window
	 */
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
	 * This is a background process that continuously updates the visible timer
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

						timerText.setText(durationString);
						miniTimerText.setText(durationString);
						//miniTimerText.setFill(Color.GREEN);
					}
				});

				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Starts a new thread to get the list of currently running applications
	 */
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
	 * Uses Powershell command to find a list of applications running processes that
	 * also have a visible window. Updates the appList
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

			} catch (Exception err) {
				err.printStackTrace();
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
			} catch (Exception e) {
				e.printStackTrace();
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
		
		Text invalidName = new Text("(Empty name)");

		ComboBox<String> applicationList = new ComboBox<String>();
		applicationList.setValue("NONE");
		applicationList.setDisable(true);

		TextField sessionName = new TextField();
		sessionName.setPrefSize(150, 5);
		
		invalidName.setFill(Color.RED);

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
			if (sessionName.getText().trim().length() != 0 && sessionName.getText() != null) {
				invalidName.setText("");
			}else {
				invalidName.setText("(Empty name)");
			}
		});

		saveButton.setOnAction(a -> {
			this.setUIColor(Color.BLACK,"black");
			startButton.setText("Start");
			applicationList.setValue("NONE");
			timeController.stopTime();
			timeController.endCurrentSession();
			timeController.saveSessions();
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
			if (saveButtonReady && sessionName.getText().trim().length() != 0 && sessionName.getText() != null) {
				invalidName.setText("");
				
			}else  if (sessionName.getText().trim().length() != 0 && sessionName.getText() != null){
				invalidName.setText("");
			}else{
				invalidName.setText("(Empty name)");
			}
		});

		applicationList.setOnShowing(a -> {
			if (!appList.contains(trackingApp)) {
				applicationList.setValue("NONE");
			}
			ObservableList<String> currentAppList = FXCollections
					.observableArrayList(appList.subList(0, appList.size()));
			applicationList.setItems(currentAppList);
		});

		enableAppTracking.setOnAction(a -> {
			if (enableAppTracking.isSelected()) {
				Alert appTrackingInfo = new Alert(AlertType.INFORMATION, 
						"Application tracking will keep the timer running as long as the selected application is running. " + 
						"Re-opening the appplication, as long as it is still selected in the dropdown, will start the timer again.");
				appTrackingInfo.setHeaderText("Application Tracking");
				appTrackingInfo.setTitle("Application Tracking Info");
				appTrackingInfo.showAndWait();
				startButton.setDisable(true);
				applicationList.setDisable(false);
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
		
		miniTimerWindow(viewMiniTimerWindow, primaryStage);
		
		previousLogs.setOnAction(a -> {
			previousLogWindow();
		});
		
		graphs.setOnAction(a -> {
			Stage previousLogWindow = new Stage();
			previousLogWindow.initModality(Modality.APPLICATION_MODAL);
			previousLogWindow.setTitle("Previous Sessions");

			// Add a text title inside the window
			Text title = new Text("Previous Sessions");
			title.setFont(new Font(18));
			
			final NumberAxis xAxis = new NumberAxis();
	        final CategoryAxis yAxis = new CategoryAxis();
	        final BarChart<NumberAxis, CategoryAxis> bc = new BarChart(xAxis,yAxis);
	        bc.setTitle("Session History");
	        bc.setLegendVisible(false);
	        xAxis.setLabel("Duration(Seconds)");  
	        xAxis.setTickLabelRotation(90);
	        yAxis.setLabel("Session");
	        
			ObservableList<Series<NumberAxis, CategoryAxis>> data = getSessionsGraphData(timeController.getSessions());
			
			bc.setData(data);
			
			for (Series<NumberAxis, CategoryAxis> series: bc.getData()){
	            for (XYChart.Data<NumberAxis, CategoryAxis> item: series.getData()){
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
			previousLogWindow.setScene(new Scene(layout, 600, 600));
			previousLogWindow.show();
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
		logNames.getChildren().addAll(logNameTitle, sessionName, invalidName);
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
		
		
		primaryStage.setScene(scene);
		primaryStage.show();

		// Close all other windows when primary stage gets closed.
		primaryStage.setOnCloseRequest(a -> {
			Platform.exit();
		});
	}

	/**
	 * Shows the previous recorded sessions in the given Stage
	 * 
	 * @param window
	 *            Stage to show the sessions in
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
				// DEBUG
				// System.out.println(logs.getSelectionModel().getSelectedIndex());
			}
		});

		Button directoryExportButton = new Button("Change Export Directory");
		directoryExportButton.setText("Change Export Directory");

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
			hourSpinner.setPrefWidth(50);

			Spinner<Integer> minuteSpinner = new Spinner<Integer>(minuteValueFactory);
			minuteSpinner.setPrefWidth(50);

			Spinner<Integer> secondSpinner = new Spinner<Integer>(secondValueFactory);
			secondSpinner.setPrefWidth(50);

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
				timeController.saveSessions();
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

		directoryExportButton.setOnAction(b -> {
			DirectoryChooser chooser = new DirectoryChooser();
			chooser.setTitle("Select Directory");
			File defaultDirectory = new File("output");
			chooser.setInitialDirectory(defaultDirectory);
			File selectedDirectory = chooser.showDialog(previousLogWindow);
			if (selectedDirectory != null) {
				timeModel.setDirectory(selectedDirectory.getAbsolutePath());
			}
		});

		// delete button handler
		deleteButton.setOnAction(b -> {
			int logIndex = (logs.getSelectionModel().getSelectedIndex());
			Alert deleteAlert = new Alert(AlertType.CONFIRMATION, "Are you sure you want to delete this session?");
			deleteAlert.setHeaderText("Delete Confirmation");
			deleteAlert.showAndWait().ifPresent(response -> {
				if (response == ButtonType.OK) {
					timeController.deleteSession(logIndex);
					logs.setItems(FXCollections.observableArrayList(timeModel.getFormattedSessionList()));
					deleteButton.setDisable(true);
					editButton.setDisable(true);
					timeController.saveSessions();
				}
			});
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
		layout.getChildren().addAll(title, logs, deleteEditButtons, enableDateRange, dates, exportButton,
				directoryExportButton);
		layout.setAlignment(Pos.CENTER);
		previousLogWindow.setScene(new Scene(layout, 300, 400));
		previousLogWindow.show();
	}

	/**
	 * Creates but does not show a mini timer window with only the timer.
	 * 
	 * @param window
	 *            The stage for the minitimer
	 * @param primaryWindow
	 *            The main stage
	 */
	private void miniTimerWindow(Stage window, Stage primaryWindow) {
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

		//miniTimerWindow.show();

		miniTimerWindow.setOnCloseRequest(a -> {
			primaryWindow.requestFocus();
		});
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
	
	private ObservableList<Series<NumberAxis, CategoryAxis>> getSessionsGraphData(List<Session> sessions) {
	    ObservableList<XYChart.Series<NumberAxis,CategoryAxis>> answer = FXCollections.observableArrayList();
        
        XYChart.Series<NumberAxis,CategoryAxis> series1 = new XYChart.Series<NumberAxis,CategoryAxis>();
        series1.setName("Sesson Duration");  
        
        for(Session session : sessions) {
        	series1.getData().add(new XYChart.Data(session.getTotalTime() / 1000, session.getSessionName()));
        }
        
        answer.add(series1);
        
        return answer;
	}
	
	private <T> void forceListRefreshOn(ObservableList<Series<NumberAxis, CategoryAxis>> data, List<Session> list, BarChart<NumberAxis, CategoryAxis> bc) {
	    ObservableList<Series<NumberAxis, CategoryAxis>> items = data;
	    data.clear();
	    
	    data = getSessionsGraphData(timeController.getSessions());
		
		bc.setData(data);
		
		for (Series<NumberAxis, CategoryAxis> series: bc.getData()){
            for (XYChart.Data<NumberAxis, CategoryAxis> item: series.getData()){
                item.getNode().setOnMousePressed(new EventHandler<MouseEvent>() {
                	//https://stackoverflow.com/questions/5107158/how-to-pass-parameters-to-anonymous-class
                	private ObservableList<Series<NumberAxis, CategoryAxis>> anonVar;
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
                    private EventHandler<MouseEvent> init(ObservableList<Series<NumberAxis, CategoryAxis>> data){
                        anonVar = data;
                        return this;
                    }
                }.init(data));
            }
        }
	}
}