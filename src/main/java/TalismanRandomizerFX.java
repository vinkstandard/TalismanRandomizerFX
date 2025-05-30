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

        CheckBox abilitaModalitaScura = new CheckBox("Modalità scura");
        abilitaModalitaScura.setOnAction(e -> {
            if (abilitaModalitaScura.isSelected()) {
                root.getStyleClass().add("dark-mode");
            } else {
                root.getStyleClass().remove("dark-mode");
            }
        });

        Label label = new Label("Numero di giocatori:");
        campoNumeroGiocatori = new TextField();
        campoNumeroGiocatori.setPromptText("Es. 3");

        // impedisce l'inserimento di caratteri non numerici e oltre 6
        campoNumeroGiocatori.setTextFormatter(new TextFormatter<String>(change -> {
            if (!change.getControlNewText().matches("\\d{0,1}")) {
                return null;
            }
            try {
                int val = Integer.parseInt(change.getControlNewText());
                if (val > 6) return null;
            } catch (NumberFormatException e) {
                // ignora gli errori temporanei mentre si digita
            }
            return change;
        }));

        Label labelEspansioni = new Label("Seleziona le espansioni:");
        VBox containerCaselle = new VBox(5);
        containerCaselle.getStyleClass().add("vbox-caselle");


        Map<String, String> legenda = TalismanLogica.getLegenda();
        caselleDaSpuntare = new ArrayList<>();

        for (Map.Entry<String, String> entry : legenda.entrySet()) {
            CheckBox cb = new CheckBox(entry.getValue());
            cb.setUserData(entry.getKey()); // salva il numero della espansione
            caselleDaSpuntare.add(cb);
            containerCaselle.getChildren().add(cb);
        }

        Button estraiButton = new Button("Estrai Personaggi");
        HBox boxImmaginiPersonaggi = new HBox(10);
        boxImmaginiPersonaggi.setAlignment(Pos.CENTER);
        boxImmaginiPersonaggi.setStyle("-fx-background-color: black; -fx-padding: 10px;");
        boxImmaginiPersonaggi.setVisible(false); // lo nascondiamo, e lo riattiviamo solo quando il giocatore clicca estrai personaggi

        estraiButton.setDisable(false);

        estraiButton.setOnAction(e -> {
            try {
                AudioClip suonoInizio = new AudioClip(getClass().getResource("/suoni/inizio.wav").toExternalForm());
                suonoInizio.setVolume(0.1);
                suonoInizio.play();

                int numeroGiocatori = Integer.parseInt(campoNumeroGiocatori.getText());
                List<String> espansioniSelezionate = caselleDaSpuntare.stream()
                        .filter(CheckBox::isSelected)
                        .map(cb -> (String) cb.getUserData())
                        .collect(Collectors.toList());

                List<String> risultati = TalismanLogica.estraiPersonaggi(numeroGiocatori, espansioniSelezionate);

                estraiButton.setDisable(true); // disabilita il pulsante

                boxImmaginiPersonaggi.getChildren().clear();

                Timeline timeline = new Timeline();
                boxImmaginiPersonaggi.setVisible(true); // lo riattiviamo

                for (int i = 0; i < risultati.size(); i++) {
                    final int index = i;
                    KeyFrame keyFrame = new KeyFrame(Duration.seconds(1 + i * 1.5), event -> {
                        String nome = risultati.get(index);
                        String fileName = nome.toLowerCase() + ".png";
                        URL imageUrl = getClass().getResource("/immagini/" + fileName);
                        if (imageUrl != null) {
                            ImageView immaginePersonaggio = new ImageView(new Image(imageUrl.toExternalForm()));
                            immaginePersonaggio.setFitWidth(200);
                            immaginePersonaggio.setFitHeight(250);
                            immaginePersonaggio.setPreserveRatio(true);
                            boxImmaginiPersonaggi.getChildren().add(immaginePersonaggio);
                            VBox pgBox = new VBox(5);
                            pgBox.setAlignment(Pos.CENTER);

                            // per mettere il nome sotto all'immagine del personaggio
                            Text nomeTesto = new Text(nome.toUpperCase()); // elevato a uppercase per maggiore leggibilità
                            nomeTesto.setFill(javafx.scene.paint.Color.web("#ffd966")); // colore del nome
                            nomeTesto.setFont(Font.font("System", FontWeight.BOLD, 20)); // font del nome
                            pgBox.getChildren().addAll(immaginePersonaggio, nomeTesto);
                            boxImmaginiPersonaggi.getChildren().add(pgBox);


                        } else {
                            System.out.println(">> Immagine non trovata per: " + nome);
                        }
                        // parte il suono quando piazziamo un'immagine
                        try {
                            AudioClip suonoApparizionePersonaggio = new AudioClip(getClass().getResource("/suoni/character_selected.wav").toExternalForm());
                            suonoApparizionePersonaggio.setVolume(0.25);
                            suonoApparizionePersonaggio.play();
                        } catch (Exception ex) {
                            System.out.println(">> Errore con il suono: " + ex.getMessage());
                        }
                    });
                    timeline.getKeyFrames().add(keyFrame);
                }

                // a fine "animazione", parte la musichetta
                timeline.setOnFinished(finishEvent -> {
                    AudioClip suonoFinale = new AudioClip(getClass().getResource("/suoni/fine.wav").toExternalForm());
                    suonoFinale.setVolume(0.05);
                    suonoFinale.play();

                });
                timeline.play();

            } catch (NumberFormatException ex) {
                System.out.println(">> Numero giocatori non valido.");
            }
        });

        ScrollPane scroll = new ScrollPane(containerCaselle);
        scroll.setPrefHeight(200);
        scroll.setFitToWidth(true);


        // Aggiungi il toggle all'inizio dell'interfaccia
        root.getChildren().addAll(abilitaModalitaScura, label, campoNumeroGiocatori, labelEspansioni, scroll, estraiButton, boxImmaginiPersonaggi);

        Scene scene = new Scene(root, 1200, 600);
        scene.getStylesheets().add(getClass().getResource("/stili/style.css").toExternalForm()); // crea questo file dopo

        stage.setScene(scene);
        stage.setTitle("Sistema Pseudointelligente di Estrazione Aleatoria Nominativa ad Alta Entropia_v2 (S.P.E.A.N.A.E.)  Made by Vincent");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

