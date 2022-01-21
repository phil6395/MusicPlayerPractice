package application;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class Controller implements Initializable{
	
	@FXML
	private Pane pane;
	
	@FXML
	private Label songLabel;
	
	@FXML
	private Button nextButton, previousButton, viewPlaylistButton, addButton;
	
	@FXML
	private ToggleButton playToggle, shuffleToggle;
	
	@FXML
	private ProgressBar songProgressBar;
	
	private Media media;
	private MediaPlayer mediaPlayer;
	
	private File directory;
	private File[] files;
	
	private ArrayList<File> songs;
	
	private String selectedTrackPath;

	private int songNumber;
	
	private Timer timer;
	private TimerTask task;
	private boolean running;
	
	private double current;
	private double end;
	
	public Media selectedTrack;
	public MediaPlayer selectionMediaPlayer;
	
	private String nameWithoutExtension;
	
	@FXML
	private TextField searchBox;
	
	@FXML
	private Button searchButton;
	
	@FXML
	private ListView<String> tracksListView;
	
	
	private String clickedSong;
	
	@FXML
	private RadioButton titleRadioButton, artistRadioButton;
	
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {	
		songs = new ArrayList<File>();
		
		directory = new File("music");
		
		files = directory.listFiles();
		
		if(files != null) {
			for(File file : files) {
				songs.add(file);
				String stripped = file.getName();
				nameWithoutExtension = stripped.substring(0,stripped.lastIndexOf("."));
				tracksListView.getItems().addAll(nameWithoutExtension);
			}
		}
		
		media = new Media(songs.get(songNumber).toURI().toString());
		mediaPlayer = new MediaPlayer(media);
		songLabel.setText(songs.get(songNumber).getName().replace(".mp3",""));
		
		tracksListView.getSelectionModel().selectedItemProperty().addListener((ChangeListener<? super String>) new ChangeListener<String>(){
			
			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				mediaPlayer.stop();
				clickedSong = tracksListView.getSelectionModel().getSelectedItem();
				songLabel.setText(clickedSong);
				
				selectedTrackPath = ("music\\" + clickedSong + ".mp3");
				File file = new File(selectedTrackPath);
				selectedTrackPath = file.toURI().toString();
				
				if (clickedSong !=null) {
					selectedTrack = new Media(selectedTrackPath);
					playMedia();
				}
			}
			
		});
		
	}

	
	public void playMedia() {
		if (clickedSong != null) {
			mediaPlayer = new MediaPlayer(selectedTrack);
			mediaPlayer.play();
			beginTimer();
		} else {
		mediaPlayer.play();
		beginTimer();
		}
	}
	
	public void pauseMedia() {
		cancelTimer();
		mediaPlayer.pause();
	}
	
	public void nextMedia() {
		clickedSong = null;
		
		if(songNumber < songs.size() - 1) {
			songNumber ++;
			mediaPlayer.stop();
			
			if(running) {
				cancelTimer();
			}
			
			media = new Media(songs.get(songNumber).toURI().toString());
			mediaPlayer = new MediaPlayer(media);
			
			songLabel.setText(songs.get(songNumber).getName().replace(".mp3",""));	
			
			playMedia();
		} else {
			songNumber = 0;
			mediaPlayer.stop();
			
			if(running) {
				cancelTimer();
			}
			
			media = new Media(songs.get(songNumber).toURI().toString());
			mediaPlayer = new MediaPlayer(media);
			
			songLabel.setText(songs.get(songNumber).getName().replace(".mp3",""));	
			playMedia();
		}
	}
	
	public void previousMedia() {
		
		clickedSong = null;
		
		if(songNumber > 0) {
			songNumber --;
			mediaPlayer.stop();
			
			if(running) {
				cancelTimer();
			}
			
			media = new Media(songs.get(songNumber).toURI().toString());
			mediaPlayer = new MediaPlayer(media);
			
			songLabel.setText(songs.get(songNumber).getName());	
			
			playMedia();
		} else {
			songNumber = songs.size() - 1;
			mediaPlayer.stop();
			
			if(running) {
				cancelTimer();
			}
			
			media = new Media(songs.get(songNumber).toURI().toString());
			mediaPlayer = new MediaPlayer(media);
			
			songLabel.setText(songs.get(songNumber).getName());	
			playMedia();
		}
		
	}
	
	public void beginTimer() {

			timer = new Timer();
			
			task = new TimerTask() {
				public void run() {
					running = true;
					
					current = mediaPlayer.getCurrentTime().toSeconds();
					
					end = media.getDuration().toSeconds();
						
					System.out.println(current/end);
		
					songProgressBar.setProgress(current/end);
					
					if(current/end == 1) {
						cancelTimer();
					}
				}
			};
			
			timer.scheduleAtFixedRate(task, 500, 500);
			
		}
		
	
	public void cancelTimer() {
		running = false;
		timer.cancel();	
	}
	
	
	public void search(ActionEvent event) {
		tracksListView.getItems().clear();
		initialize(null, null);
		
		ListView<String> filteredTracksListView = new ListView<>();
				
		String filter = searchBox.getText();
		
		filter = filter.toLowerCase();
		
		for(String t: tracksListView.getItems()) {
			if (t.toLowerCase().contains(filter)
					&& filter.length() > 0) {
				filteredTracksListView.getItems().add(t);
				//System.out.println(t);
			}
		}
		
		
		if(filter.equals("") || searchBox.getText() == null) {
			tracksListView.getItems().clear();
			initialize(null, null);
		}
		else {
			tracksListView.setItems(FXCollections.observableArrayList(filteredTracksListView.getItems()));
		}

		}
	
	public void resetSearch() {
		tracksListView.getItems().clear();
		initialize(null, null);
		searchBox.setText(null);
	}
	
	public void sortListView(ActionEvent event) {
		if (titleRadioButton.isSelected()) {
//			ObservableList<String> list = tracksListView.getSelectionModel().getSelectedItems();
//			System.out.println("list: " + list);
//			SortedList<String> titleSortedList = new SortedList<String>(list);
//			System.out.println(titleSortedList);
//			tracksListView.setItems(titleSortedList);
			
			ArrayList<String> titleSortedList = new ArrayList<>();
			for (String t: tracksListView.getItems()) {
				titleSortedList.add(t);
			}
			
			Collections.sort(titleSortedList);
			
			
			System.out.println(titleSortedList);

			
		}
		else if (artistRadioButton.isSelected()) {
			
			ArrayList<String> artistSortedList = new ArrayList<>();
			for (String i: tracksListView.getItems()) {
				
				String[] parts = i.split(" - ");
				String part1 = parts[0]; 
				String part2 = parts[1];
				
				artistSortedList.add(part2 + " - " + part1);
			}
			System.out.println(artistSortedList);
			
			
			Collections.sort(artistSortedList);
		}
		
	}
		
		

}