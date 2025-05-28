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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.*;

public class TalismanRandomizerFX extends Application {

    private static final List<String> characters = Arrays.asList("Assassino", "Druido", "Mago");

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #000000;"); // colore azzurro chiaro


        Text title = new Text("Estrazione Personaggi");

        Button tastoInizia = new Button("Inizia");
        VBox boxPersonaggi = new VBox(10);
        boxPersonaggi.setAlignment(Pos.CENTER);

        tastoInizia.setOnAction(e -> {
            tastoInizia.setDisable(true); // Disabilita il bottone

            boxPersonaggi.getChildren().clear();
            List<String> selezionati = getRandomCharacters(3);

            // Creazione della timeline per mostrare 1 personaggio alla volta
            Timeline timeline = new Timeline();
            for (int i = 0; i < selezionati.size(); i++) {
                final int index = i;
                KeyFrame frame = new KeyFrame(Duration.seconds((index + 1) * 1.5), ev -> {
                    String nome = selezionati.get(index);
                    Image immagine = new Image(getClass().getResource("/immagini/" + nome.toLowerCase() + ".png").toExternalForm());
                    ImageView view = new ImageView(immagine);
                    view.setPreserveRatio(true);
                    view.setFitHeight(300);
                    view.setFitWidth(200);

                    VBox pgBox = new VBox(5);
                    pgBox.setAlignment(Pos.CENTER);

                    Text nomeTesto = new Text(nome.toUpperCase()); // nome elevato a uppercase per maggior leggibilità
                    nomeTesto.setFill(javafx.scene.paint.Color.web("#ffd966")); // il colore del nome del personaggio
                    nomeTesto.setFont(Font.font("System", FontWeight.BOLD, 20));
                    pgBox.getChildren().addAll(view, nomeTesto);

                    boxPersonaggi.getChildren().add(pgBox);

                    playSound(); // riproduco il suono ogni volta che esce fuori un personaggio
                });
                timeline.getKeyFrames().add(frame);
            }

            timeline.play();
        });

        root.getChildren().addAll(title, tastoInizia, boxPersonaggi);

        Scene scena = new Scene(root, 600, 800);
        primaryStage.setScene(scena);
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

