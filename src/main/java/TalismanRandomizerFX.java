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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class TalismanRandomizerFX extends Application {

    private TextField campoNumeroGiocatori;
    private List<CheckBox> caselleDaSpuntare;
    private ComboBox<String> comboLingua;
    private Label labelNumeroGiocatori;
    private Label labelEspansioni;
    private Button estraiButton;
    private CheckBox abilitaModalitaScura;
    private VBox chat;
    private ScrollPane scrollChatBox;


    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));


        // menu lingua in alto a destra
        comboLingua = new ComboBox<>();
        comboLingua.getItems().addAll(ServizioLingua.getLingueDisponibili());
        comboLingua.setValue("en");
        comboLingua.setOnAction(e -> {
            String linguaSelezionata = comboLingua.getValue();
            ServizioLingua.caricaLingua(linguaSelezionata);
            aggiornaTestiUI();
        });

        HBox topBar = new HBox(comboLingua);
        topBar.setAlignment(Pos.TOP_RIGHT);
        root.setTop(topBar);

        VBox mainContent = new VBox(10);

        // label e campo numero giocatori
        labelNumeroGiocatori = new Label();
        campoNumeroGiocatori = new TextField();
        campoNumeroGiocatori.setPromptText("Es. 3");

        // limita input numerico da 1 a 6
        campoNumeroGiocatori.setTextFormatter(new TextFormatter<String>(change -> {
            if (!change.getControlNewText().matches("\\d{0,1}")) {
                return null;
            }
            try {
                int val = Integer.parseInt(change.getControlNewText());
                if (val > 6) return null;
            } catch (NumberFormatException e) {
                // ignorare
            }
            return change;
        }));

        // label espansioni
        labelEspansioni = new Label();

        // checkbox espansioni dentro uno scroll
        VBox containerCaselle = new VBox(5);
        containerCaselle.getStyleClass().add("vbox-caselle");

        // carica lingua di default
        ServizioLingua.caricaLingua("en");

        // legenda espansioni (chiave -> nome tradotto)
        Map<String, String> legenda = ServizioLingua.getEspansioni();
        caselleDaSpuntare = new ArrayList<>();

        for (Map.Entry<String, String> entry : legenda.entrySet()) {
            CheckBox cb = new CheckBox(entry.getValue());
            cb.setUserData(entry.getKey());
            caselleDaSpuntare.add(cb);
            containerCaselle.getChildren().add(cb);
        }

        ScrollPane scroll = new ScrollPane(containerCaselle);
        scroll.setPrefHeight(200);
        scroll.setFitToWidth(true);

        // bottone estrai
        estraiButton = new Button();

        // box immagini personaggi
        HBox boxImmaginiPersonaggi = new HBox(10);
        boxImmaginiPersonaggi.setAlignment(Pos.CENTER);
        boxImmaginiPersonaggi.setStyle("-fx-background-color: black; -fx-padding: 10px;");
        boxImmaginiPersonaggi.setVisible(false);

        // checkbox modalità scura
        abilitaModalitaScura = new CheckBox();
        abilitaModalitaScura.setOnAction(e -> {
            if (abilitaModalitaScura.isSelected()) {
                root.getStyleClass().add("dark-mode");
            } else {
                root.getStyleClass().remove("dark-mode");
            }
        });

        // Test per chatBox(registro)
        chat = new VBox(5);
        chat.setAlignment(Pos.BOTTOM_LEFT);
        chat.setPadding(new Insets(10));
        chat.setStyle("-fx-background-color: #222222;");
        scrollChatBox = new ScrollPane(chat);
        scrollChatBox.setPrefSize(300, 400);
        scrollChatBox.setFitToWidth(true);
        scrollChatBox.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollChatBox.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollChatBox.setStyle("-fx-background-color: black;");

        // aggiungo tutto al mainContent
        mainContent.getChildren().addAll(abilitaModalitaScura, labelNumeroGiocatori, campoNumeroGiocatori, labelEspansioni, scroll, estraiButton, boxImmaginiPersonaggi, chat);

        root.setCenter(mainContent);

        // listener bottone estrai
        estraiButton.setOnAction(e -> {
            String testoNumero = campoNumeroGiocatori.getText();
            int numeroGiocatori;

            try {
                numeroGiocatori = Integer.parseInt(testoNumero);
                if (numeroGiocatori < 1 || numeroGiocatori > 6) {
                    mostraAvviso("numeroGiocatoriNonValido");
                    return;
                }
            } catch (NumberFormatException ex) {
                mostraAvviso("numeroNonValido");
                return;
            }

            List<String> espansioniSelezionate = caselleDaSpuntare.stream().filter(CheckBox::isSelected).map(cb -> (String) cb.getUserData()).collect(Collectors.toList());

            if (espansioniSelezionate.isEmpty()) {
                mostraAvviso("nessunaEspansione");
                return;
            }

            try {
                AudioClip suonoInizio = new AudioClip(getClass().getResource("/suoni/inizio.wav").toExternalForm());
                suonoInizio.setVolume(0.1);
                suonoInizio.play();

                List<String> poolPersonaggi = new ArrayList<>();
                for (String espansioneSelezionata : espansioniSelezionate) {
                    poolPersonaggi.addAll(ServizioLingua.getPersonaggiDaEspansione(espansioneSelezionata));
                }

                if (numeroGiocatori > poolPersonaggi.size()) {
                    mostraAvviso("personaggiInsufficienti");
                    return;
                }

                Collections.shuffle(poolPersonaggi);

                List<String> personaggiUsciti = new ArrayList<>(poolPersonaggi.subList(0, numeroGiocatori));

                estraiButton.setDisable(true);
                boxImmaginiPersonaggi.getChildren().clear();
                boxImmaginiPersonaggi.setVisible(true);

                Timeline timeline = new Timeline();



                for (int i = 0; i < personaggiUsciti.size(); i++) {
                    final int index = i;
                    KeyFrame keyFrame = new KeyFrame(Duration.seconds(1 + i * 1.5), event -> {

                        String nome = personaggiUsciti.get(index).toLowerCase();
                        String nomeTradottoPerURL = ServizioLingua.getNomeOriginalePersonaggio(nome);
                        System.out.println("Nome TRADOTTO URL: " + nomeTradottoPerURL);

                        String fileName = nomeTradottoPerURL + ".png";
                        URL imageUrl = getClass().getResource("/immagini/" + fileName);
                        System.out.println("Cerco immagine per: " + fileName + ", imageUrl: " + imageUrl);

                        // test per chatBox
                        LocalDateTime oraAttuale = LocalDateTime.now();
                        int ore = oraAttuale.getHour();
                        int minuti = oraAttuale.getMinute();
                        int secondi = oraAttuale.getSecond();
                        String oraFormattata = String.format("%02d:%02d:%02d", ore, minuti, secondi);

                        Text nomePg = new Text( oraFormattata + "| Hai rollato: " + ServizioLingua.getNomeTradotto(nomeTradottoPerURL));
                        nomePg.setFill(javafx.scene.paint.Color.web("#ffd966"));
                        nomePg.setFont(Font.font("System", FontWeight.BOLD, 20));
                        chat.getChildren().addAll(nomePg);



                        if (imageUrl != null) {
                            ImageView immaginePersonaggio = new ImageView(new Image(imageUrl.toExternalForm()));
                            immaginePersonaggio.setFitWidth(200);
                            immaginePersonaggio.setFitHeight(250);
                            immaginePersonaggio.setPreserveRatio(true);

                            VBox pgBox = new VBox(5);
                            pgBox.setAlignment(Pos.CENTER);

                            String nomeTradotto = ServizioLingua.getNomeTradotto(nomeTradottoPerURL);
                            Text nomeTesto = new Text(nomeTradotto.toUpperCase());

                            nomeTesto.setFill(javafx.scene.paint.Color.web("#ffd966"));
                            nomeTesto.setFont(Font.font("System", FontWeight.BOLD, 20));

                            pgBox.getChildren().addAll(immaginePersonaggio, nomeTesto);
                            boxImmaginiPersonaggi.getChildren().add(pgBox);
                        } else {
                            System.out.println(">> Immagine non trovata per: " + nome);
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

        // aggiorno testi ui all'avvio
        aggiornaTestiUI();

        Scene scene = new Scene(root, 1300, 800);
        scene.getStylesheets().add(getClass().getResource("/stili/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Sistema Pseudointelligente di Estrazione Aleatoria Nominativa ad Alta Entropia_v2 (S.P.E.A.N.A.E.)  Made by Vincent");
        stage.show();
    }

    private void mostraAvviso(String messaggio) {
        Alert allerta = new Alert(Alert.AlertType.WARNING);
        allerta.setTitle(ServizioLingua.get("warnings", "titolo"));
        allerta.setHeaderText(null);
        allerta.setContentText(ServizioLingua.get("warnings", messaggio));
        allerta.showAndWait();
    }

    private void aggiornaTestiUI() {
        labelNumeroGiocatori.setText(ServizioLingua.get("labels", "numeroGiocatori"));
        labelEspansioni.setText(ServizioLingua.get("labels", "selezionaEspansioni"));
        estraiButton.setText(ServizioLingua.get("buttons", "estrai"));
        abilitaModalitaScura.setText(ServizioLingua.get("labels", "modalitaScura"));


        // Aggiorna testo checkbox espansioni
        Map<String, String> legenda = ServizioLingua.getEspansioni();
        for (CheckBox cb : caselleDaSpuntare) {
            String key = (String) cb.getUserData();
            if (legenda.containsKey(key)) {
                cb.setText(legenda.get(key));
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}