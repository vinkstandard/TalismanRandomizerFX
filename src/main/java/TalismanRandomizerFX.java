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

    // TODO: modifica background chatbox, rendilo già colorato invece di aspettare che si popoli per colorarlo
    //  cerca colori interessanti?
    //  aggiungi i bordi ai pulsanti e alla sezione numeroEspansioni come hai fatto nella box della selezione espansione
    //  rendi la finestra non scalabile
    private TextField campoNumeroGiocatori;
    private List<CheckBox> caselleDaSpuntare;
    private ComboBox<String> comboLingua;
    private Label labelNumeroGiocatori;
    private Label labelEspansioni;
    private Button estraiButton;
    private CheckBox abilitaModalitaScura;
    private VBox chat;
    private ScrollPane scrollChatBox;
    private Label labelHaiRollato;


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

        // checkbox modalità scura
        abilitaModalitaScura = new CheckBox();
        abilitaModalitaScura.setOnAction(e -> {
            if (abilitaModalitaScura.isSelected()) {
                root.getStyleClass().add("dark-mode");
            } else {
                root.getStyleClass().remove("dark-mode");
            }
        });

        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(10));
        comboLingua.setPrefHeight(25);
        abilitaModalitaScura.setPrefHeight(25);
        topBar.getChildren().addAll(abilitaModalitaScura, comboLingua);
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

        // label roll
        labelHaiRollato = new Label();
        labelHaiRollato.setVisible(false); // inizialmente non vogliamo vederlo

        GridPane containerCaselle = new GridPane();
        containerCaselle.setHgap(20);  // spazio orizzontale tra le caselle
        containerCaselle.setVgap(10);  // spazio verticale tra righe
        containerCaselle.setPadding(new Insets(10));
        containerCaselle.getStyleClass().add("grid-caselle");

        // carica lingua di default
        ServizioLingua.caricaLingua("en");

        // legenda espansioni (chiave -> nome tradotto)
        Map<String, String> legenda = ServizioLingua.getEspansioni();
        caselleDaSpuntare = new ArrayList<>();
        int colonne = 5;
        int row = 0, col = 0;
        for (Map.Entry<String, String> entry : legenda.entrySet()) {
            CheckBox cb = new CheckBox(entry.getValue());
            cb.setUserData(entry.getKey());
            caselleDaSpuntare.add(cb);
            containerCaselle.add(cb, col, row);

            col++;
            if (col == colonne) {
                col = 0;
                row++;
            }
        }



        // bottone estrai
        estraiButton = new Button();

        // box immagini personaggi
        HBox boxImmaginiPersonaggi = new HBox(10);
        boxImmaginiPersonaggi.setAlignment(Pos.CENTER);
        boxImmaginiPersonaggi.setStyle("-fx-background-color: black; -fx-padding: 10px;");
        boxImmaginiPersonaggi.setVisible(false);
        boxImmaginiPersonaggi.setManaged(false);



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
        scrollChatBox.setVisible(false);
        scrollChatBox.setManaged(false);

        // centralizzo le caselle delle espansioni e il pulsante
        HBox caselleWrappate = new HBox(containerCaselle);
        caselleWrappate.setAlignment(Pos.CENTER);
        VBox espansioniEBottoneCentro = new VBox(10, caselleWrappate, estraiButton);
        espansioniEBottoneCentro.setAlignment(Pos.CENTER);

        HBox boxCentratoEspansioni = new HBox(labelEspansioni);
        boxCentratoEspansioni.setAlignment(Pos.CENTER);

        // aggiungo tutto al mainContent
        mainContent.getChildren().addAll(labelNumeroGiocatori, campoNumeroGiocatori,
                boxCentratoEspansioni, espansioniEBottoneCentro, boxImmaginiPersonaggi, scrollChatBox, labelHaiRollato);

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
                boxImmaginiPersonaggi.setManaged(true);

                scrollChatBox.setVisible(true);
                scrollChatBox.setManaged(true);
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
                        labelHaiRollato.setVisible(true);
                        LocalDateTime oraAttuale = LocalDateTime.now();
                        int ore = oraAttuale.getHour();
                        int minuti = oraAttuale.getMinute();
                        int secondi = oraAttuale.getSecond();
                        String oraFormattata = String.format("%02d:%02d:%02d", ore, minuti, secondi);

                        Text nomePg = new Text(oraFormattata + "| " + ServizioLingua.get("labels", "haiRollato") + " " + ServizioLingua.getNomeTradotto(nomeTradottoPerURL));
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
                comboLingua.setVisible(false); // rimuoviamo l'opzione di cambiare lingua dopo i roll

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

        Scene scene = new Scene(root, 1300, 850);
        scene.getStylesheets().add(getClass().getResource("/stili/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Talisman Randomizer  Made by Vincent");
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
        labelHaiRollato.setText(""); // lo cleanniamo dopo averlo usato

        // aggiorno testo checkbox espansioni
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