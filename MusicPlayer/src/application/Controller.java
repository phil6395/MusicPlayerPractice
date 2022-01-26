package application;

import java.io.File;
import java.io.IOException;
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
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import javafx.stage.DirectoryChooser;

public class Controller implements Initializable {

	@FXML
	private Pane pane;

	@FXML
	private Label songLabel, addedMessage;

	@FXML
	private Button nextButton, previousButton, viewPlaylistButton, addButton, clearPlaylistButton,
			returnFromPlaylistButton, setDirectoryButton;

	@FXML
	private ToggleButton playToggle, shuffleToggle;

	@FXML
	private ProgressBar songProgressBar;

	public Media media;
	public MediaPlayer mediaPlayer;

	// private File directory;
	private File[] files;

	private ArrayList<File> songs;

	private String selectedTrackPath;

	private int songNumber;

	private Timer timer, addedTimer, idleTimer;
	private TimerTask task, addedTask, idleTask;

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

	File directory = new File("music");

	@Override
	public void initialize(URL url, ResourceBundle rb) {

		songs = new ArrayList<File>();

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
		tracksListView.getSelectionModel().select(0);
		mediaPlayer.stop();

		// REMOVE .MP3 SUFFIX FOR MORE PROFESSIONAL FORMATTING
		songLabel.setText(songs.get(songNumber).getName().replace(".mp3", ""));

		Platform.runLater(() -> {
			returnFromPlaylistButton.setVisible(false);
		});

		// LISTENER FOR USER SELECTION OF TRACK FROM LISTVIEW
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

						selectedTrackPath = (directory + "\\" + clickedSong + ".mp3");
						File file = new File(selectedTrackPath);
						selectedTrackPath = file.toURI().toString();

						if (clickedSong != null) {
							mediaPlayer.stop();
							media = new Media(selectedTrackPath);
							mediaPlayer = new MediaPlayer(media);
							playMedia();

						}
					}

				});

	}

	public void playMedia() {
		mediaPlayer.play();
		beginTimer();
		beginIdleTimer();
	}

	public void pauseMedia() {
		cancelTimer();
		mediaPlayer.pause();
	}

	public void nextMedia() {
		clickedSong = null;
		cancelTimer();
		cancelIdleTimer();

		if (songNumber < songs.size() - 1) {

			mediaPlayer.stop();

			media = new Media(songs.get(songNumber).toURI().toString());
			mediaPlayer = new MediaPlayer(media);

			if (shuffleToggle.isSelected()) {

				int max = tracksListView.getItems().size() - 1;
				System.out.println("songs size: " + max);
				int min = 0;
				double randomNumber = Math.floor(Math.random() * (max - min + 1)) + min;
				System.out.println("randomNumber: " + randomNumber);
				System.out.println("songNumber: " + songNumber);
				if ((int) randomNumber == songNumber && (int) randomNumber <= max - 1) {
					randomNumber++;
					//System.out.println("Equal to songNumber, random number incremented to be: " + randomNumber);
				} else if ((int) randomNumber == songNumber && (int) randomNumber == max) {
					randomNumber--;
					//System.out.println("Equal to songNumber, random number decremented to be: " + randomNumber);
				}
				songNumber = (int) randomNumber;

			} else {
				songNumber++;
				songNumber = tracksListView.getSelectionModel().getSelectedIndex() + 1;
			}


			media = new Media(songs.get(songNumber).toURI().toString());
			mediaPlayer = new MediaPlayer(media);
			tracksListView.getSelectionModel().select(songNumber);

			System.out.println(songNumber);

			songLabel.setText(tracksListView.getSelectionModel().getSelectedItem().replace(".mp3", ""));

			beginTimer();
			beginIdleTimer();

		} else {
			songNumber = 0;
			mediaPlayer.stop();

			tracksListView.getSelectionModel().select(songNumber);

			songLabel.setText(tracksListView.getSelectionModel().getSelectedItem().replace(".mp3", ""));
			
			beginTimer();
			beginIdleTimer();
			
		}
	}

	public void previousMedia() {
		clickedSong = null;
		cancelTimer();
		cancelIdleTimer();

		if (songNumber > 0) {
			songNumber--;
			mediaPlayer.stop();

			media = new Media(songs.get(songNumber).toURI().toString());
			mediaPlayer = new MediaPlayer(media);
			tracksListView.getSelectionModel().select(songNumber);

			songLabel.setText(tracksListView.getSelectionModel().getSelectedItem().replace(".mp3", ""));


		} else {
			songNumber = songs.size() - 1;
			mediaPlayer.stop();

			media = new Media(songs.get(songNumber).toURI().toString());
			mediaPlayer = new MediaPlayer(media);
			tracksListView.getSelectionModel().select(songNumber);

			songLabel.setText(tracksListView.getSelectionModel().getSelectedItem().replace(".mp3", ""));
		}

	}

	// SONG TIMER SETUP
	public void beginTimer() {

		timer = new Timer();

		task = new TimerTask() {

			public void run() {

				current = mediaPlayer.getCurrentTime().toSeconds();
				//System.out.println("current: " + current);

				end = media.getDuration().toSeconds();
				//System.out.println("end: " + end);

				System.out.println(current / end);

				Platform.runLater(() -> {
					 songProgressBar.setProgress(current / end);

					if ((int) (current / end) == 1) {
						cancelTimer();
						nextMedia();
					}

				});
			}
		};

		timer.scheduleAtFixedRate(task, 500, 500);

	}

	public void cancelTimer() {
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
		addButton.setVisible(true);
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

		// PROVIDE USER WITH TEMPORARY MESSAGE THAT SONG HAS BEEN ADDED TO PLAYLIST
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

	public void setDirectory() {
		DirectoryChooser chooser = new DirectoryChooser();
		File selectedDirectory = chooser.showDialog(null);
		if (selectedDirectory == null) {
		} else {
			directory = selectedDirectory;
		}
		mediaPlayer.stop();
		playlist.clear();
		returnFromPlaylist();
	}

	public void beginIdleTimer() {

		idleTimer = new Timer();

		idleTask = new TimerTask() {
			@Override
			public void run() {
				Platform.runLater(() -> {
					try {
						switchToIdleScene();
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			}
		};
		idleTimer.schedule(idleTask, 3000);

	}

	public void cancelIdleTimer() {
		idleTimer.cancel();
	}

	public void switchToIdleScene() throws IOException {
//		Parent root = FXMLLoader.load(getClass().getResource("IdleScene.fxml"));
//		Scene scene = titleRadioButton.getScene();
//		System.out.println(scene);
//		scene.setRoot(root);
	}

}