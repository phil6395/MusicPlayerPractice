package application;
	
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;


public class Main extends Application {
	
	@Override
	public void start(Stage stage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("Scene.fxml"));
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			stage.setTitle("Music Player");
			stage.getIcons().add(
					   new Image(
					      getClass().getResourceAsStream( "icon.png" ))); 
			stage.setResizable(false);
			
			stage.setScene(scene);
			stage.show();
			
			scene.getRoot().requestFocus();
			

			
		} catch(Exception e) {
			e.printStackTrace();
		}

		stage.setOnCloseRequest((EventHandler<WindowEvent>) new EventHandler<WindowEvent>() {
			
			@Override
			public void handle(WindowEvent arg0) {
				Platform.exit();
				System.exit(0);
			}
			
		});
			
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
}
