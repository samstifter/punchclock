import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

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
			//if(startClicked) {
				try {
				Platform.runLater(new Runnable() 
				{
		            @Override 
		            public void run() 
		            {
		            	txt.setText(Double.toString(timeController.getCurrentSessionElapsedTime() / 1000.0));
		            }
		        });
		
				Thread.sleep(50);
				}
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
			//}
				/*
			else {
				if(timeController.getCurrentSessionElapsedTime() == 0) {
					txt.setText("0");
				}
				break;
			}
			*/
			//timeController.displayElapsedTimeInSeconds(timeModel);
			//txt.setText(Double.toString(timeController.getCurrentElapsedTime() / 1000.0));
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// =====Setup Time Components=====
		timeController = new TimeController(timeModel, timeView);

		// ====Setup====
		primaryStage.setTitle("Title");
		VBox verticalBox = new VBox();
        Scene scene = new Scene(verticalBox, 300, 400);
        scene.setFill(Color.OLDLACE);

        txt = new Text("Test");
        Button startButton = new Button("Start");
        Button resetButton = new Button("Reset");
        Button importButton = new Button("Import Logs");
        Button viewPrevious = new Button("View Previous Sessions");
        startButton.setOnAction(a -> {
        	startClicked = !startClicked;
        	if(startClicked) {
        		startButton.setText("Pause");
	        	timeController.startTime();
	            //timer.start();
        	}
        	else {
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

        	//This is where the imported list should go.
        	/*
        	List<String> li = new ArrayList<String>();
        	li.add("2018-01-01 00:00 1:50");
        	li.add("2018-01-01 00:00 2:50");
        	*/
        	ObservableList<String> logList = FXCollections.observableArrayList(timeModel.getFormattedSessionList());
        	ListView<String> logs = new ListView<String>(logList);

        	// Set the size for the list
        	logs.setPrefHeight(200);
        	logs.setPrefWidth(300);

        	// Add a button to export the logs
        	Button exportButton = new Button("Export Logs");

        	//Export Button Handler
        	exportButton.setOnAction(b -> {
        		// Implement Export Class
				try {
					timeController.writeToReadableFile();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}

			});

        	// Make a layout, and add everything to it, centered
        	VBox layout = new VBox(10);
            layout.getChildren().addAll(title, logs, exportButton);
            layout.setAlignment(Pos.CENTER);
            previousLogWindow.setScene(new Scene(layout, 300, 300));
            previousLogWindow.show();
        });

		importButton.setOnAction(a -> {
			timeController.loadSavedSessions();
		});

        VBox vb = new VBox();
        vb.getChildren().addAll(txt,startButton,resetButton,viewPrevious,importButton);

        //====Canvas====
        Canvas canvas = new Canvas(300,300);
        //====Menu====
        MenuBar menuBar = new MenuBar();
        Menu menuFile = new Menu("File");
        MenuItem newItem = new MenuItem("New Game");

        menuFile.getItems().addAll(newItem);
        menuBar.getMenus().addAll(menuFile);

        //====Create====
        verticalBox.getChildren().addAll(menuBar,vb,canvas);

        primaryStage.setScene(scene);
        primaryStage.show();


	}

	@Override
	public void stop(){
	    System.out.println("Stage is closing");
	}

}
