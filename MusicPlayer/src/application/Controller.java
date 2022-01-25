package application;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
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

public class Controller implements Initializable {

	@FXML
	private Pane pane;

	@FXML
	private Label songLabel, addedMessage;

	@FXML
	private Button nextButton, previousButton, viewPlaylistButton, addButton, clearPlaylistButton, returnFromPlaylistButton;

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

	private Timer timer, addedTimer;
	private TimerTask task, addedTask;
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

		if (files != null) {
			for (File file : files) {
				songs.add(file);
				String stripped = file.getName();
				nameWithoutExtension = stripped.substring(0, stripped.lastIndexOf("."));
				tracksListView.getItems().addAll(nameWithoutExtension);
			}
		}

		media = new Media(songs.get(songNumber).toURI().toString());
		mediaPlayer = new MediaPlayer(media);
		tracksListView.getSelectionModel().select(songNumber);
		mediaPlayer.stop();
		
		
		//REMOVE .MP3 SUFFIX FOR MORE PROFESSIONAL FORMATTING
		songLabel.setText(songs.get(songNumber).getName().replace(".mp3", ""));
		
		Platform.runLater(() -> {
			returnFromPlaylistButton.setVisible(false);
		});

		
		//LISTENER FOR USER SELECTION OF TRACK FROM LISTVIEW
		tracksListView.getSelectionModel().selectedItemProperty()
				.addListener((ChangeListener<? super String>) new ChangeListener<String>() {

					@Override
					public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
						mediaPlayer.stop();

						clickedSong = tracksListView.getSelectionModel().getSelectedItem();
						

						if (artistRadioButton.isSelected() && searchBox.getText() != null && clickedSong != null) {
							String[] parts = clickedSong.split(" - ");
							String part1 = parts[0];
							String part2 = parts[1];
							clickedSong = part2 + " - " + part1;
						} 

						songLabel.setText(clickedSong);

						selectedTrackPath = ("music\\" + clickedSong + ".mp3");
						File file = new File(selectedTrackPath);
						selectedTrackPath = file.toURI().toString();

						if (clickedSong != null) {
							mediaPlayer.stop();
							selectedTrack = new Media(selectedTrackPath);
							mediaPlayer = new MediaPlayer(selectedTrack);
							playMedia();
						}
					}

				});

	}

	public void playMedia() {
			mediaPlayer.play();
			beginTimer();
		
	}

	public void pauseMedia() {
		cancelTimer();
		mediaPlayer.pause();
	}

	public void nextMedia() {
		clickedSong = null;
		
		if (songNumber < songs.size() - 1) {
			songNumber++;
			mediaPlayer.stop();

			if (running) {
				cancelTimer();
			}

			media = new Media(songs.get(songNumber).toURI().toString());
			mediaPlayer = new MediaPlayer(media);
			
			songNumber = tracksListView.getSelectionModel().getSelectedIndex()+1;
			tracksListView.getSelectionModel().select(songNumber);
			
			System.out.println(songNumber);

			songLabel.setText(tracksListView.getSelectionModel().getSelectedItem().replace(".mp3", ""));

			//playMedia();
			
		} else {
			songNumber = 0;
			mediaPlayer.stop();

			if (running) {
				cancelTimer();
			}

			media = new Media(songs.get(songNumber).toURI().toString());
			mediaPlayer = new MediaPlayer(media);
			tracksListView.getSelectionModel().select(songNumber);

			songLabel.setText(tracksListView.getSelectionModel().getSelectedItem().replace(".mp3", ""));
			playMedia();
		}
	}

	public void previousMedia() {

		clickedSong = null;

		if (songNumber > 0) {
			songNumber--;
			mediaPlayer.stop();

			if (running) {
				cancelTimer();
			}

			media = new Media(songs.get(songNumber).toURI().toString());
			mediaPlayer = new MediaPlayer(media);
			tracksListView.getSelectionModel().select(songNumber);
			
			songLabel.setText(tracksListView.getSelectionModel().getSelectedItem().replace(".mp3", ""));

			playMedia();
			
		} else {
			songNumber = songs.size() - 1;
			mediaPlayer.stop();

			if (running) {
				cancelTimer();
			}

			media = new Media(songs.get(songNumber).toURI().toString());
			mediaPlayer = new MediaPlayer(media);
			tracksListView.getSelectionModel().select(songNumber);

			songLabel.setText(tracksListView.getSelectionModel().getSelectedItem().replace(".mp3", ""));
			playMedia();
		}

	}

	//SONG TIMER SETUP
	public void beginTimer() {

		timer = new Timer();

		task = new TimerTask() {

			public void run() {
				running = true;

				current = mediaPlayer.getCurrentTime().toSeconds();

				end = media.getDuration().toSeconds();

				//System.out.println(current / end);
				
				Platform.runLater(() -> {
					
				//PROGRESS BAR COMMENTED OUT DUE TO ISSUES WITH DISPLAYING CORRECTLY
				//songProgressBar.setProgress(current / end);

				if (current / end == 1) {
					cancelTimer();
					nextMedia();
				}
				
				});
			}
		};
		
		timer.scheduleAtFixedRate(task, 500, 500);

	}

	public void cancelTimer() {
		running = false;
		timer.cancel();
	}

	
	// SEARCH BOX PROCESSING
	public void search(ActionEvent event) {
		viewPlaylistButton.setVisible(true);
		artistRadioButton.setSelected(false);
		titleRadioButton.setSelected(true);
		
		tracksListView.getItems().clear();
		initialize(null, null);

		ListView<String> filteredTracksListView = new ListView<>();

		String filter = searchBox.getText();

		filter = filter.toLowerCase();
		
		if (filter != null) {
		for (String t : tracksListView.getItems()) {
			if (t.toLowerCase().contains(filter) && filter.length() > 0) {
				filteredTracksListView.getItems().add(t);
				// System.out.println(t);
				addedMessage.setText(null);
			} 
		}
		}
		

		if (filter.equals("") || searchBox.getText() == null) {
			tracksListView.getItems().clear();
			initialize(null, null);
		} else {
			tracksListView.setItems(FXCollections.observableArrayList(filteredTracksListView.getItems()));
			titleRadioButton.isSelected();
			if (filteredTracksListView.getItems().isEmpty()) {
				addedMessage.setText("No results found");
			}
		}

	}

	public void resetSearch() {
		viewPlaylistButton.setVisible(true);
		tracksListView.getItems().clear();
		initialize(null, null);
		addedMessage.setText(null);
		searchBox.setText(null);
	}

	
	// SORT LISTVIEW INTO TITLE OR ARTIST SORTED
	public void sortListView(ActionEvent event) {
		if (titleRadioButton.isSelected()) {

			ArrayList<String> titleSortedList = new ArrayList<>();
			for (String t : tracksListView.getItems()) {

				String[] parts = t.split(" - ");
				String part1 = parts[0];
				String part2 = parts[1];

				titleSortedList.add(part2 + " - " + part1);
			}

			Collections.sort(titleSortedList);

			tracksListView.getItems().clear();
			for (String p : titleSortedList) {
				tracksListView.getItems().add(p);
			}

		} else if (artistRadioButton.isSelected()) {

			ArrayList<String> artistSortedList = new ArrayList<>();
			for (String i : tracksListView.getItems()) {

				String[] parts = i.split(" - ");
				String part1 = parts[0];
				String part2 = parts[1];

				artistSortedList.add(part2 + " - " + part1);
			}

			Collections.sort(artistSortedList);

			tracksListView.getItems().clear();
			for (String n : artistSortedList) {
				tracksListView.getItems().add(n);
			}
		}

	}
	
	ArrayList<String> playlist = new ArrayList<>();
	
	public void addtoPlaylist(ActionEvent event) {
		
		String addedSong = songLabel.getText();
		
		playlist.add(addedSong);
		
		
		System.out.println(playlist);
		
		//PROVIDE USER WITH TEMPORARY MESSAGE THAT SONG HAS BEEN ADDED TO PLAYLIST
		addedMessage.setText("Added to playlist");
		
		addedTimer = new Timer();

		addedTask = new TimerTask() {
			@Override
			public void run() {
				Platform.runLater(() -> {
					addedMessage.setText(null);
				});
			}
		};
		addedTimer.schedule(addedTask, 1200);
		
	}
	
	public void viewPlaylist() {
		addButton.setVisible(false);
		viewPlaylistButton.setVisible(false);
		artistRadioButton.setSelected(false);
		titleRadioButton.setSelected(true);
		
		tracksListView.getItems().clear();
		for (String z : playlist) {
			tracksListView.getItems().add(z);
		}
		
		returnFromPlaylistButton.setVisible(true);
		clearPlaylistButton.setVisible(true);
		if (tracksListView.getItems().isEmpty()) {
			addedMessage.setText("Playlist Empty - ");
			clearPlaylistButton.setVisible(false);
		}
		
	}
	
	public void returnFromPlaylist() {
		viewPlaylistButton.setVisible(true);
		addButton.setVisible(true);
		tracksListView.getItems().clear();
		initialize(null, null);
		addedMessage.setText(null);
		clearPlaylistButton.setVisible(false);
		//radioButtons.clearSelection();
		artistRadioButton.setSelected(false);
		titleRadioButton.setSelected(true);
	}
	
	public void clearPlaylist() {
		mediaPlayer.stop();
		playlist.clear();
		tracksListView.getItems().clear();
		clearPlaylistButton.setVisible(false);
		addedMessage.setText("Playlist Empty - ");
		
	}

}