package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import model.Video;

public class MainController {

	@FXML
	private MediaView mediaVideo;
	@FXML
	private HBox mediaTool;
	@FXML
	private Label lblTitle;
	@FXML
	private TextField txtSearchSubtitle;
	@FXML
	private CheckBox chkFavorite;
	@FXML
	private ListView<String> listSubtitle;
	@FXML
	private Button btnAddVideo;
	@FXML
	private Button btnDeleteVideo;
	@FXML
	private Button btnAddSubtitle;
	@FXML
	private ListView<String> listVideo;
	@FXML
	private Button btnPlay;
	@FXML
	private Slider sliderTimeVideo;
	@FXML
	private Label lblTimeVideo;
	@FXML
	private Slider sliderVloume;
	@FXML 
	private TabPane tab;
	
	// Unity
//	private Media media;
	private MediaPlayer mediaPlayer;
	private Duration duration;
	private List<Video> videos = new ArrayList<Video>();
	private int indexSelectedVideo;
	
	
	public void initialize() {
        System.out.println("initilize");
        
        tab.getSelectionModel().select(0);
		btnAddSubtitle.setDisable(true);
		btnDeleteVideo.setDisable(true);
		indexSelectedVideo = -1;
        
		setListVideo();
		
        setActionListVideo();
        setActionSliderTimeVideo();
        setActionListSubtitle();
        
    }

	public void setListVideo() {
		
		
//		ObservableList<String> items = FXCollections.observableArrayList ("video1", "video2", "video3", "video4");
//        listVideo.setItems(items);
		readListVideo();
		for (int i = 0; i < videos.size(); i++) {
//			System.out.println(videos.get(i).getName());
			listVideo.getItems().add(videos.get(i).getName());
		}
		
	}
	
	public void setActionListVideo() {
		listVideo.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// TODO Auto-generated method stub
				if (newValue == null) {
					// reset state
			        btnPlay.setText(">");
					sliderTimeVideo.setValue(0);
					listSubtitle.getItems().clear();
					
					lblTitle.setText("Không có Video");
					
					btnDeleteVideo.setDisable(true);
					btnAddSubtitle.setDisable(true);
					return;
				}
				
				btnDeleteVideo.setDisable(false);
				indexSelectedVideo = listVideo.getSelectionModel().getSelectedIndex();
				
				lblTitle.setText(videos.get(indexSelectedVideo).getName());
				
				setMediaVideo(videos.get(indexSelectedVideo).getUrlVideo());
				
				if (videos.get(indexSelectedVideo).getUrlSubtitle() != null) {
					readFileSubtitle(videos.get(indexSelectedVideo).getUrlSubtitle());
				}
				
				btnAddSubtitle.setDisable(false);
				
			}
		});
	}
	
	public void setActionListSubtitle() {
		listSubtitle.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// TODO Auto-generated method stub
				if (newValue == null) return;
				
				SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
				System.out.println("a"+sdf);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                System.out.println("b"+sdf);
                Date date = null;
                try {
                    date = sdf.parse(newValue.substring(0,5));
                    System.out.println(sdf.parse(newValue.substring(0,5)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                
                mediaPlayer.seek(Duration.millis(date.getTime()));
			}
		});
	}
	
	public void handleButtonPlay(ActionEvent e) {
        MediaPlayer.Status status = mediaPlayer.getStatus();

        if (status == MediaPlayer.Status.UNKNOWN  || status == MediaPlayer.Status.HALTED)
        {
            // don't do anything in these states
            return;
        }

        if ( status == MediaPlayer.Status.PAUSED || status == MediaPlayer.Status.READY || status == MediaPlayer.Status.STOPPED) {
            mediaPlayer.play();
            btnPlay.setText("||");
        } 
        else {
        	mediaPlayer.pause();
        	btnPlay.setText(">");
        }
    }
	
	public void setMediaVideo(String uri) {
		if (uri == null)
            return;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }
        
		Media media = new Media(uri);
        mediaPlayer = new MediaPlayer(media);
        mediaVideo.setMediaPlayer(mediaPlayer);
        mediaVideo.setPreserveRatio(true);
        // reset state
        btnPlay.setText(">");
		sliderTimeVideo.setValue(0);
		listSubtitle.getItems().clear();
		// action
		play();
	}
	
	public void setActionSliderTimeVideo() {
		sliderTimeVideo.valueProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov) {
                if (sliderTimeVideo.isValueChanging()) {
                    // multiply duration by percentage calculated by slider position
                    mediaPlayer.seek(duration.multiply(sliderTimeVideo.getValue() / 100.0));
                }
            }
        });
		
	}
	
	public void updateTimeVideo() {
		Platform.runLater(new Runnable() {
            @SuppressWarnings("deprecation")
			public void run() {
                Duration currentTime = mediaPlayer.getCurrentTime();
                lblTimeVideo.setText(formatTime(currentTime, duration));
                sliderTimeVideo.setDisable(duration.isUnknown());
                if (!sliderTimeVideo.isDisabled() && duration.greaterThan(Duration.ZERO) && !sliderTimeVideo.isValueChanging()) {
                	sliderTimeVideo.setValue(currentTime.divide(duration).toMillis() * 100.0);
                }
            }
        });
    }
	
	public String formatTime(Duration elapsed, Duration duration) {
        int intElapsed = (int)Math.floor(elapsed.toSeconds());
        int elapsedHours = intElapsed / (60 * 60);
        if (elapsedHours > 0) {
            intElapsed -= elapsedHours * 60 * 60;
        }
        int elapsedMinutes = intElapsed / 60;
        int elapsedSeconds = intElapsed - elapsedHours * 60 * 60
                - elapsedMinutes * 60;

        if (duration.greaterThan(Duration.ZERO)) {
            int intDuration = (int)Math.floor(duration.toSeconds());
            int durationHours = intDuration / (60 * 60);
            if (durationHours > 0) {
                intDuration -= durationHours * 60 * 60;
            }
            int durationMinutes = intDuration / 60;
            int durationSeconds = intDuration - durationHours * 60 * 60 -
                    durationMinutes * 60;
            if (durationHours > 0) {
                return String.format("%d:%02d:%02d/%d:%02d:%02d",
                        elapsedHours, elapsedMinutes, elapsedSeconds,
                        durationHours, durationMinutes, durationSeconds);
            } else {
                return String.format("%02d:%02d/%02d:%02d",
                        elapsedMinutes, elapsedSeconds,durationMinutes,
                        durationSeconds);
            }
        } else {
            if (elapsedHours > 0) {
                return String.format("%d:%02d:%02d", elapsedHours,
                        elapsedMinutes, elapsedSeconds);
            } else {
                return String.format("%02d:%02d",elapsedMinutes,
                        elapsedSeconds);
            }
        }
    }
	
	public void play(){
		mediaPlayer.currentTimeProperty().addListener(new InvalidationListener()
        {
            public void invalidated(Observable ov) {
            	updateTimeVideo();
            }
        });
		mediaPlayer.setOnPlaying(new Runnable() {
            public void run() {
            	btnPlay.setText("||");
            }
        });

		mediaPlayer.setOnPaused(new Runnable() {
            public void run() {
                btnPlay.setText(">");
            }
        });
		mediaPlayer.setOnReady(new Runnable() {
            public void run() {
                duration = mediaPlayer.getMedia().getDuration();
                updateTimeVideo();
            }
        });		
		mediaPlayer.setOnEndOfMedia(new Runnable() {
            public void run() {
            	mediaPlayer.seek(mediaPlayer.getStartTime());
            	sliderTimeVideo.setValue(0);
            	mediaPlayer.pause();
            }
        });
    }
	
	public void readFileSubtitle(String url){
	        try (BufferedReader reader = new BufferedReader(new FileReader(new File(url)))) {
	            String line;
	            while ((line = reader.readLine()) != null){
//	            	System.out.println(line);
	                listSubtitle.getItems().add(line);
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	
	public File locateFile(Node node) {
		FileChooser chooser = new FileChooser();
	    chooser.setTitle("Chon Video");
	    return chooser.showOpenDialog(node.getScene().getWindow());
	
	}
	
	public void addVideo(ActionEvent event) {
		Node node = (Node) event.getSource();
		File file = locateFile(node); 
		if (file != null) {
//			System.out.println(file.getName());
			
//			setMediaVideo(file.toURI().toString());
			
			videos.add(new Video(file.getName(), file.toURI().toString()));
			
			listVideo.getItems().add(file.getName());
			
			writeListVideo();
		}
	
	}
	
	public void addSubtitle(ActionEvent event) {
		if (indexSelectedVideo > -1) {
			Node node = (Node) event.getSource();
			File file = locateFile(node);
			if (file != null) {
				listSubtitle.getItems().clear();
						
				readFileSubtitle(file.toString());
				
				videos.get(indexSelectedVideo).setUrlSubtitle(file.toString());
			}
		}

	}
	
	public void deleteVideo(ActionEvent event) {
		if (indexSelectedVideo > -1) {
			videos.remove(indexSelectedVideo);
			listVideo.getItems().remove(indexSelectedVideo);
		}
	}
	
	public void writeListVideo() {
		try {
			File file = new File(".\\videos.txt");
			if (file.exists()) {
				file.delete();
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			FileOutputStream fout = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fout);

			oos.writeObject(videos);

			oos.close();
			fout.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("Error initializing stream");
		}	
	}
	
	@SuppressWarnings("unchecked")
	public void readListVideo() {
		try {
			FileInputStream fin = new FileInputStream(new File(".\\videos.txt"));
			ObjectInputStream ois = new ObjectInputStream(fin);
			
			videos = (ArrayList<Video>)ois.readObject(); 
				
			ois.close();
			fin.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("Error initializing stream");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void stop() {
		writeListVideo();
		System.out.println("stop");
	}

}
