import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Main extends Application implements Runnable{
	static TimeModel timeModel;
	static TimeView timeView;
	static TimeController timeController;
	
	Thread timer;
	private boolean startClicked;
    public static void main(String[] args) {
    	timeModel = new TimeModel();
		timeView = new TimeView();
    	launch(args);
    }

	@Override
	public void start(Stage primaryStage) throws Exception {
		//=====Setup Time Components=====
		timeController = new TimeController(timeModel,timeView);
		
		
		//====Setup====
		primaryStage.setTitle("Title");
		VBox verticalBox = new VBox();
        Scene scene = new Scene(verticalBox, 300, 400);
        scene.setFill(Color.OLDLACE);
        
        Text txt = new Text("Test");
        Button startButton = new Button("Start");
        Button resetButton = new Button("Reset");
        startButton.setOnAction(a -> {
        	startClicked = !startClicked;
        	if(startClicked) {
        		startButton.setText("Pause");
	        	timer = new Thread(new Main());
	        	timer = new Thread(new Main());
	        	timeController.startTime();
	            timer.start();
        	}
        	else {
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
        VBox vb = new VBox();
        vb.getChildren().addAll(txt,startButton,resetButton);
        
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

	@Override
	public void run() {
		timeController = new TimeController(timeModel,timeView);
		while(true) {
	        timeController.displayElapsedTimeInSeconds(timeModel);
		}
	}
	
} 