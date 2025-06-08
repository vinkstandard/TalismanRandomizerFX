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

    private Label labelNumGiocatori;
    private Label labelEspansioni;
    private Label labelLingua;
    private Button estraiButton;
    private CheckBox abilitaModalitaScura;

    @Override
    public void start(Stage stage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        abilitaModalitaScura = new CheckBox(Traduttore.tr("dark_mode"));
        abilitaModalitaScura.setOnAction(e -> {
            if (abilitaModalitaScura.isSelected()) {
                root.getStyleClass().add("dark-mode");
            } else {
                root.getStyleClass().remove("dark-mode");
            }
        });

        ComboBox<String> linguaBox = new ComboBox<>();
        linguaBox.getItems().addAll("English", "Italiano");
        linguaBox.setValue("English");

        linguaBox.setOnAction(e -> {
            String scelta = linguaBox.getValue();
            Traduttore.setLingua(scelta.equals("Italiano") ? Traduttore.Lingua.IT : Traduttore.Lingua.EN);
            aggiornaTesti();
            stage.setTitle(Traduttore.tr("app_title"));
        });

        labelLingua = new Label(Traduttore.tr("select_language"));
        HBox barraSuperiore = new HBox(10);
        barraSuperiore.setAlignment(Pos.TOP_RIGHT);
        barraSuperiore.getChildren().addAll(labelLingua, linguaBox);

        labelNumGiocatori = new Label(Traduttore.tr("num_players"));
        campoNumeroGiocatori = new TextField();
        campoNumeroGiocatori.setPromptText("Es. 3");
        campoNumeroGiocatori.setTextFormatter(new TextFormatter<String>(change -> {
            if (!change.getControlNewText().matches("\\d{0,1}")) {
                return null;
            }
            try {
                int val = Integer.parseInt(change.getControlNewText());
                if (val > 6) return null;
            } catch (NumberFormatException e) {}
            return change;
        }));

        labelEspansioni = new Label(Traduttore.tr("select_expansions"));
        VBox containerCaselle = new VBox(5);
        containerCaselle.getStyleClass().add("vbox-caselle");

        Map<String, String> legenda = TalismanLogica.getLegenda();
        caselleDaSpuntare = new ArrayList<>();

        for (Map.Entry<String, String> entry : legenda.entrySet()) {
            CheckBox cb = new CheckBox(entry.getValue());
            cb.setUserData(entry.getKey());
            caselleDaSpuntare.add(cb);
            containerCaselle.getChildren().add(cb);
        }

        estraiButton = new Button(Traduttore.tr("extract_characters"));
        HBox boxImmaginiPersonaggi = new HBox(10);
        boxImmaginiPersonaggi.setAlignment(Pos.CENTER);
        boxImmaginiPersonaggi.setStyle("-fx-background-color: black; -fx-padding: 10px;");
        boxImmaginiPersonaggi.setVisible(false);
        estraiButton.setDisable(false);

        estraiButton.setOnAction(e -> {
            String testoNumero = campoNumeroGiocatori.getText();
            int numeroGiocatori;

            try {
                numeroGiocatori = Integer.parseInt(testoNumero);
                if (numeroGiocatori < 1 || numeroGiocatori > 6) {
                    mostraAvviso(Traduttore.tr("warning_insert_players"));
                    return;
                }
            } catch (NumberFormatException ex) {
                mostraAvviso(Traduttore.tr("warning_insert_players"));
                return;
            }

            List<String> espansioniSelezionate = caselleDaSpuntare.stream()
                    .filter(CheckBox::isSelected)
                    .map(cb -> (String) cb.getUserData())
                    .collect(Collectors.toList());

            if (espansioniSelezionate.isEmpty()) {
                mostraAvviso(Traduttore.tr("warning_select_expansion"));
                return;
            }

            try {
                AudioClip suonoInizio = new AudioClip(getClass().getResource("/suoni/inizio.wav").toExternalForm());
                suonoInizio.setVolume(0.1);
                suonoInizio.play();

                List<String> risultati = TalismanLogica.estraiPersonaggi(numeroGiocatori, espansioniSelezionate);

                estraiButton.setDisable(true);
                boxImmaginiPersonaggi.getChildren().clear();
                boxImmaginiPersonaggi.setVisible(true);

                Timeline timeline = new Timeline();

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

                            VBox pgBox = new VBox(5);
                            pgBox.setAlignment(Pos.CENTER);
                            Text nomeTesto = new Text(nome.toUpperCase());
                            nomeTesto.setFill(javafx.scene.paint.Color.web("#ffd966"));
                            nomeTesto.setFont(Font.font("System", FontWeight.BOLD, 20));
                            pgBox.getChildren().addAll(immaginePersonaggio, nomeTesto);
                            boxImmaginiPersonaggi.getChildren().add(pgBox);
                        }

                        try {
                            AudioClip suonoApparizione = new AudioClip(getClass().getResource("/suoni/character_selected.wav").toExternalForm());
                            suonoApparizione.setVolume(0.25);
                            suonoApparizione.play();
                        } catch (Exception ex) {
                            System.out.println(">> Errore suono: " + ex.getMessage());
                        }
                    });
                    timeline.getKeyFrames().add(keyFrame);
                }

                timeline.setOnFinished(finishEvent -> {
                    AudioClip suonoFinale = new AudioClip(getClass().getResource("/suoni/fine.wav").toExternalForm());
                    suonoFinale.setVolume(0.05);
                    suonoFinale.play();
                });

                timeline.play();

            } catch (Exception ex) {
                System.out.println(">> Errore durante l'estrazione: " + ex.getMessage());
            }
        });

        ScrollPane scroll = new ScrollPane(containerCaselle);
        scroll.setPrefHeight(200);
        scroll.setFitToWidth(true);

        root.getChildren().addAll(barraSuperiore, abilitaModalitaScura, labelNumGiocatori, campoNumeroGiocatori,
                labelEspansioni, scroll, estraiButton, boxImmaginiPersonaggi);

        Scene scene = new Scene(root, 1200, 600);
        scene.getStylesheets().add(getClass().getResource("/stili/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle(Traduttore.tr("app_title"));
        stage.show();
    }

    private void mostraAvviso(String messaggio) {
        Alert allerta = new Alert(Alert.AlertType.WARNING);
        allerta.setTitle(Traduttore.tr("warning_title"));
        allerta.setHeaderText(null);
        allerta.setContentText(messaggio);
        allerta.showAndWait();
    }

    private void aggiornaTesti() {
        abilitaModalitaScura.setText(Traduttore.tr("dark_mode"));
        labelNumGiocatori.setText(Traduttore.tr("num_players"));
        labelEspansioni.setText(Traduttore.tr("select_expansions"));
        estraiButton.setText(Traduttore.tr("extract_characters"));
        labelLingua.setText(Traduttore.tr("select_language"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}

