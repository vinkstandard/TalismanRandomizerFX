import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import javafx.scene.media.AudioClip;
import javafx.stage.*;
import javafx.util.*;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class TalismanRandomizerFX extends Application {

    private TextField campoNumeroGiocatori;
    private List<CheckBox> caselleDaSpuntare;

    @Override
    public void start(Stage stage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        Label label = new Label("Numero di giocatori:");
        campoNumeroGiocatori = new TextField();
        campoNumeroGiocatori.setPromptText("Es. 3");

        Label labelEspansioni = new Label("Seleziona le espansioni:");
        VBox checkboxContainer = new VBox(5);

        // Genera i checkbox usando la legenda della logica
        Map<String, String> legenda = TalismanLogica.getLegenda(); // Questo deve esistere!
        caselleDaSpuntare = new ArrayList<>();

        for (Map.Entry<String, String> entry : legenda.entrySet()) {
            CheckBox cb = new CheckBox(entry.getValue());
            cb.setUserData(entry.getKey()); // salva il numero della espansione
            caselleDaSpuntare.add(cb);
            checkboxContainer.getChildren().add(cb);
        }

        Button estraiButton = new Button("Estrai Personaggi");
        HBox imageBox = new HBox(10);
        imageBox.setAlignment(Pos.CENTER);
        imageBox.setStyle("-fx-background-color: black; -fx-padding: 10px;");
        estraiButton.setDisable(false);

        estraiButton.setOnAction(e -> {
            try {
                int numeroGiocatori = Integer.parseInt(campoNumeroGiocatori.getText());
                List<String> espansioniSelezionate = caselleDaSpuntare.stream()
                        .filter(CheckBox::isSelected)
                        .map(cb -> (String) cb.getUserData())
                        .collect(Collectors.toList());

                List<String> risultati = TalismanLogica.estraiPersonaggi(numeroGiocatori, espansioniSelezionate);

                estraiButton.setDisable(true); // disabilita il pulsante

                imageBox.getChildren().clear();

                Timeline timeline = new Timeline();
                for (int i = 0; i < risultati.size(); i++) {
                    final int index = i;
                    KeyFrame keyFrame = new KeyFrame(Duration.seconds(1 + i * 1.5), event -> {
                        String nome = risultati.get(index);
                        String fileName = nome.toLowerCase().replaceAll(" ", "").replaceAll("'", "") + ".png";
                        URL imageUrl = getClass().getResource("/immagini/" + fileName);
                        if (imageUrl != null) {
                            ImageView imageView = new ImageView(new Image(imageUrl.toExternalForm()));
                            imageView.setFitWidth(200);
                            imageView.setFitHeight(250);
                            imageView.setPreserveRatio(true);
                            imageBox.getChildren().add(imageView);
                            VBox pgBox = new VBox(5);
                            pgBox.setAlignment(Pos.CENTER);

                            // per mettere i nomi
                            Text nomeTesto = new Text(nome.toUpperCase()); // elevato a uppercase per maggiore leggibilità
                            nomeTesto.setFill(javafx.scene.paint.Color.web("#ffd966")); // colore del nome
                            nomeTesto.setFont(Font.font("System", FontWeight.BOLD, 20)); // font del nome
                            pgBox.getChildren().addAll(imageView, nomeTesto);
                            imageBox.getChildren().add(pgBox);


                        } else {
                            System.out.println("Immagine non trovata per: " + nome);
                        }
                        // Suono
                        try {
                            AudioClip clip = new AudioClip(getClass().getResource("/suoni/character_selected.wav").toExternalForm());
                            clip.play();
                        } catch (Exception ex) {
                            System.out.println("Errore suono: " + ex.getMessage());
                        }
                    });
                    timeline.getKeyFrames().add(keyFrame);
                }

                timeline.play();

            } catch (NumberFormatException ex) {
                System.out.println("Numero giocatori non valido");
            }
        });
        AudioClip finale = new AudioClip(getClass().getResource("/suoni/character_selected.wav").toExternalForm());

        ScrollPane scroll = new ScrollPane(checkboxContainer);
        scroll.setPrefHeight(200);
        scroll.setFitToWidth(true);

        root.getChildren().addAll(label, campoNumeroGiocatori, labelEspansioni, scroll, estraiButton, imageBox);

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Talisman Randomizer FX");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

