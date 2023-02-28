package application.controller;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

import Utils.Utils;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class FaceDetectionController {
	@FXML
	private CheckBox haarClassifier;

	@FXML
	private CheckBox lbpClassifier;

	@FXML
	private ImageView originalFrame;

	@FXML
	private Button cameraButton;

	private ScheduledExecutorService timer;
	private VideoCapture capture;
	private boolean cameraActive;
	private CascadeClassifier faceCascade;
	private int absoluteFaceSize;

	public void init()
	{
		this.capture = new VideoCapture();
		this.faceCascade = new CascadeClassifier();
		this.absoluteFaceSize = 0;
		
		originalFrame.setFitWidth(600);
		originalFrame.setPreserveRatio(true);
	}
	

	@FXML
	protected void startCamera() {
		if (!this.cameraActive) {
			this.haarClassifier.setDisable(true);
			this.lbpClassifier.setDisable(true);

			this.capture.open(0);

			if (this.capture.isOpened()) {
				this.cameraActive = true;

				Runnable frameGrabber = new Runnable() {

					@Override
					public void run() {

						Mat frame = grabFrame();
						Image imageToShow = Utils.mat2Image(frame);
						updateImageView(originalFrame, imageToShow);

					}
				};

				this.timer = Executors.newSingleThreadScheduledExecutor();
				this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

				this.cameraButton.setText("Parar Camera");
			} else {
				System.err.println("Error para abrir conexão com a camera...");
			}
		} else {
			this.cameraActive = false;
			this.cameraButton.setText("Abrir Camera");
			this.haarClassifier.setDisable(false);
			this.lbpClassifier.setDisable(false);

			this.stopAcquisition();
		}
	}

	@FXML
	protected void haarSelected(Event event)
	{
		if (this.lbpClassifier.isSelected())
			this.lbpClassifier.setSelected(false);
			
		this.checkboxSelection("C:\\Users\\lhcteles\\eclipse-workspace\\FaceDetectionFx\\src\\application\\resources\\haarcascades\\haarcascade_frontalface_alt.xml");
	}
	
	@FXML
	protected void lbpSelected(Event event)
	{
		if (this.haarClassifier.isSelected())
			this.haarClassifier.setSelected(false);
			
		this.checkboxSelection("C:\\Users\\lhcteles\\eclipse-workspace\\FaceDetectionFx\\src\\application\\resources\\lbpcascades\\lbpcascade_frontalface.xml");
	}
	
	private void checkboxSelection(String classifierPath)
	{
		this.faceCascade.load(classifierPath);
		
		this.cameraButton.setDisable(false);
	}
	
	private void stopAcquisition() {
		if (this.timer != null && !this.timer.isShutdown()) {
			try {
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				System.err.println("Error em parar o frame de captura, tentando liberar a camera agora... " + e);
			}
		}

		if (this.capture.isOpened()) {
			this.capture.release();
		}
	}

	private Mat grabFrame() {
		Mat frame = new Mat();

		if (this.capture.isOpened()) {
			try {
				this.capture.read(frame);
				if (!frame.empty()) {
					this.detectAndDisplay(frame);
				}
			} catch (Exception e) {
				System.err.println("Error durante elaboração de imagem: " + e);
			}
		}

		return frame;
	}

	private void detectAndDisplay(Mat frame) {
		MatOfRect faces = new MatOfRect();
		Mat grayFrame = new Mat();

		Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
		Imgproc.equalizeHist(grayFrame, grayFrame);

		if (this.absoluteFaceSize == 0) {
			int height = grayFrame.rows();
			if (Math.round(height * 0.2f) > 0) {
				this.absoluteFaceSize = Math.round(height * 0.2f);
			}
		}

		this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
				new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());

		Rect[] faceArray = faces.toArray();

		for (Rect rect : faceArray) {
			Imgproc.rectangle(frame, rect.tl(), rect.br(), new Scalar(0, 255, 0), 3);
		}
	}

	private void updateImageView(ImageView view, Image image) {
		Utils.onFxThread(view.imageProperty(), image);
	}
	
	public void setClosed()
	{
		this.stopAcquisition();
	}

}
