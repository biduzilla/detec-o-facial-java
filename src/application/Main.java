package application;

import org.opencv.core.Core;

import application.controller.FaceDetectionController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("FaceDetection.fxml"));
			BorderPane root = (BorderPane) loader.load();

//			FaceDetectionController faceController = new FaceDetectionController();
//			loader.setController(faceController);

			root.setStyle("-fx-background-color: whitesmoke;");
			Scene scene = new Scene(root, 400, 400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setTitle("Detecção Facial");
			primaryStage.setScene(scene);

			primaryStage.setOnHiding(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					System.out.println("App Fechado");
//					faceController.finalizar();
					System.out.println("COMANDO -> FINALIZAR");
//					faceController.finalizar();
//					Platform.exit();
//					Thread.currentThread().interrupt();
//					System.exit(0);

				}
			});

			primaryStage.setOnCloseRequest((new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we) {
					System.out.println("App Fechado 2");
//					faceController.finalizar();
					System.out.println("COMANDO -> FINALIZAR");
//					faceController.finalizar();
//					Platform.exit();
//					System.exit(0);
				}
			}));

			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		System.out.println("***" + args[0]);
		inicializarParametros(args[0]);
//		inicializarParametros("2,01A,C:\\Siclus\\facial,48,52");
		launch(args);
	}

	private static void inicializarParametros(String paramentros) {
		System.out.println("*** inicializarPrograma");

		String[] parametrosList = paramentros.split(",");

		GlobalVars.setTimerSet(Integer.parseInt(parametrosList[0]));
		System.out.println("*** timerSet");

//		codAssoc = parametrosList[1];
		GlobalVars.setCodAssoc(parametrosList[1]);
		System.out.println("*** codAssoc");

//		path = parametrosList[2];
		GlobalVars.setPath(parametrosList[2]);
		System.out.println("*** path");

//		nuEquip = Integer.parseInt(parametrosList[3]);
		GlobalVars.setNuEquip(Integer.parseInt(parametrosList[3]));
		System.out.println("*** nuEquip");

//		confidenceInt = Integer.parseInt(parametrosList[4]);
		GlobalVars.setConfidenceInt(Integer.parseInt(parametrosList[4]));
		System.out.println("*** confidence");

		System.out.println("*** " + GlobalVars.getTimerSet() + " - " + GlobalVars.getCodAssoc() + " - "
				+ GlobalVars.getPath() + " - " + GlobalVars.getNuEquip() + " - " + GlobalVars.getConfidenceInt());

	}
}
