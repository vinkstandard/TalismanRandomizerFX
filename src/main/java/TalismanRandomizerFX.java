import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.*;

public class TalismanRandomizerFX extends Application {

    private static final List<String> characters = Arrays.asList("Assassino", "Druido", "Mago");

    @Override
    public void start(Stage primaryStage) {
        System.out.println(getClass().getResource("/immagini/assassino.png"));

        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);

        Text title = new Text("Estrazione Personaggi");

        Button generateButton = new Button("Estrai Personaggi");
        VBox characterBox = new VBox(10);
        characterBox.setAlignment(Pos.CENTER);

        generateButton.setOnAction(e -> {
            generateButton.setDisable(true); // Disabilita il bottone

            characterBox.getChildren().clear();
            List<String> selected = getRandomCharacters(3);

            // Creazione della timeline per mostrare 1 personaggio alla volta
            Timeline timeline = new Timeline();
            for (int i = 0; i < selected.size(); i++) {
                final int index = i;
                KeyFrame frame = new KeyFrame(Duration.seconds((index + 1) * 1.5), ev -> {
                    String name = selected.get(index);
                    Image img = new Image(getClass().getResource("/immagini/" + name.toLowerCase() + ".png").toExternalForm());
                    ImageView view = new ImageView(img);
                    view.setFitHeight(150);
                    view.setFitWidth(100);

                    VBox pgBox = new VBox(5);
                    pgBox.setAlignment(Pos.CENTER);
                    pgBox.getChildren().addAll(view, new Text(name));
                    characterBox.getChildren().add(pgBox);

                    playSound(); // Riproduce il suono a ogni personaggio
                });
                timeline.getKeyFrames().add(frame);
            }

            timeline.play();
        });

        root.getChildren().addAll(title, generateButton, characterBox);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Talisman Randomizer FX");
        primaryStage.setResizable(false); // per bloccare il ridimensionamento
        primaryStage.show();
    }

    private List<String> getRandomCharacters(int count) {
        List<String> copy = new ArrayList<>(characters);
        Collections.shuffle(copy);
        return copy.subList(0, count);
    }

    private void playSound() {
        AudioClip sound = new AudioClip(getClass().getResource("/suoni/character_selected.wav").toExternalForm());
        sound.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

