package application.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.face.Face;
import org.opencv.face.FaceRecognizer;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

import Utils.Utils;
import application.GlobalVars;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

public class FaceDetectionController implements Initializable {

	@FXML
	private ImageView originalFrame;

	@FXML
	private Button cameraButton;

	@FXML
	private CheckBox isNovoUser;

	@FXML
	private TextField edtNome;

	@FXML
	private Text tv_validacao;

//	@FXML
//	private Button btnInit;

	private ScheduledExecutorService timer;
	private VideoCapture capture;
	private CascadeClassifier faceCascade;
	private FaceRecognizer faceTreinada;
	private HashMap<Integer, String> nomes = new HashMap<Integer, String>();
	private boolean cameraActive;
	private boolean podeVerificar = false;
	private boolean isPosVerificacao = false;
	private static boolean isAguardandoVerificacao = false;
	private int timerInt = 0;
	private int timerSemRosto = 0;
	private int countDesconhecido = 0;
	private int absoluteFaceSize;
	private int timerSet;
	private int nuEquip;
	private int confidenceInt;
	private String novoNome = null;
	private String codAssoc;
	private String path;
	private int maxLabel = 0;

	@FXML
	protected void startCamera() {
		iniciarCamera();
	}

	private void iniciarCamera() {
		System.out.println("iniciarCamera");
		podeVerificar = false;

		if (!this.cameraActive) {
			System.out.println("startCamera");

			this.capture.open(0);

			if (this.capture.isOpened()) {

				this.cameraActive = true;

				Runnable frameGrabber = new Runnable() {

					@Override
					public void run() {
						if (!isAguardandoVerificacao) {
							timerInt++;
						}

						if (timerInt > 0 && !isPosVerificacao) {
							System.out.println(timerInt + " podeVerificar: " + podeVerificar);
						}

						Mat frame = grabFrame();
						Image imageToShow = Utils.mat2Image(frame);
						updateImageView(originalFrame, imageToShow);

						if (timerInt >= (timerSet * 20)) {
							timerInt = 0;
							podeVerificar = true;
						}
					}
				};
				this.timer = Executors.newSingleThreadScheduledExecutor();
				this.timer.scheduleAtFixedRate(frameGrabber, 0, 10, TimeUnit.MILLISECONDS);
				this.cameraButton.setText("Pausar");
			} else {
				System.err.println("Error para abrir conexão com a camera...");
			}
		} else {
			this.cameraActive = false;
			this.cameraButton.setText("Pausar");
			this.stopAcquisition();
		}
	}

	private void selectLBP() {

		this.checkboxSelection(path + "\\lbpcascades\\lbpcascade_frontalface.xml");
	}

	private void checkboxSelection(String classifierPath) {
		this.faceCascade.load(classifierPath);

		this.cameraButton.setDisable(false);
	}

	private void stopAcquisition() {
		if (this.timer != null && !this.timer.isShutdown()) {
			try {
				this.cameraButton.setText("Iniciar");
				this.cameraActive = false;
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
				if (!frame.empty() && !isAguardandoVerificacao) {
					this.detectAndDisplay(frame);
				}
			} catch (Exception e) {
				System.err.println("Error durante elaboração de imagem: " + e);
			}
		}

		return frame;
	}

	private void detectAndDisplay(Mat frame) {
//		if(EnstiWeb.DEBUG) System.out.println("Estou em FaceDetectionController --> detectAndDisplay ...(fim)");
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

		if (faceArray.length != 0) {
//			
			if (isPosVerificacao) {
				tv_validacao.setText("Retire o rosto");
			} else {
				tv_validacao.setText("Aguardando Validação");

				for (Rect rect : faceArray) {
					Imgproc.rectangle(frame, rect.tl(), rect.br(), new Scalar(0, 255, 0), 3);
					Imgproc.rectangle(frame, new Point(rect.x, rect.y),
							new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255), 3);

					Imgproc.rectangle(frame, rect.tl(), rect.br(), new Scalar(0, 255, 0), 3);

					Rect rectCrop = new Rect(rect.tl(), rect.br());
					Mat croppedImage = new Mat(frame, rectCrop);
					Imgproc.cvtColor(croppedImage, croppedImage, Imgproc.COLOR_BGR2GRAY);
					Imgproc.equalizeHist(croppedImage, croppedImage);

					Mat resizeImage = new Mat();
					Size size = new Size(250, 250);

					if (podeVerificar) {
						timerInt = 0;
						podeVerificar = false;

						Imgproc.resize(croppedImage, resizeImage, size);

						System.out.println("Tirando Foto");

						if (isNovoUser.isSelected()) {

							salvarUser(resizeImage);

							isNovoUser.setSelected(false);
							edtNome.clear();

						} else {
							System.out.println("Reconhecendo Rosto");

							double[] returnedResults = faceRecognition(resizeImage);
							double prediction = returnedResults[0];
							double confidence = returnedResults[1];
							int label = (int) prediction;

							String name = "";

							if (nomes.containsKey(label)) {
								if (confidence <= confidenceInt) {
									name = nomes.get(label);
									System.out.println("COMANDO -> nCarterinha: " + name);

									countDesconhecido = 0;

									isAguardandoVerificacao = true;

									validacaoFx();

								} else {
									name = "0000000000";
									System.out.println("CcountDesconhecido: " + countDesconhecido);
									countDesconhecido++;
									if (countDesconhecido > 5) {
										System.out.println("COMANDO -> nCarterinha: " + name);
										countDesconhecido = 0;
										isAguardandoVerificacao = true;
										validacaoFx();
									}
								}
							}
							System.out
									.println("Nome = " + name + " -- Confidence = " + confidence + " Label = " + label);

						}
					}
				}
			}
		} else {
			timerInt = 0;
			if (isPosVerificacao) {
				timerSemRosto++;
				System.out.println("timerSemRosto: " + timerSemRosto + " podeVerificar: " + podeVerificar);

				if (timerSemRosto >= (2 * 20)) {
					validacaoFx();
					podeVerificar = false;
					timerSemRosto = 0;
					isPosVerificacao = false;
				}

			}
		}
	}

	private void validacaoFx() {

		if (isAguardandoVerificacao) {
			tv_validacao.setVisible(true);
			cameraButton.setVisible(false);
			isNovoUser.setVisible(false);
			edtNome.setVisible(false);
		} else {
			tv_validacao.setVisible(false);
			cameraButton.setVisible(true);
			isNovoUser.setVisible(true);
			edtNome.setVisible(true);

//			iniciarCamera();
		}
	}

	public void pausar() {
//		if (EnstiWeb.DEBUG)
//			System.out.println("Estou em FaceDetectionController --> pausar() ...(fim)");
		this.cameraActive = false;
		this.podeVerificar = false;
		this.stopAcquisition();
	}

	public void finalizar() {
		System.out.println("Finalizar controller");
		iniciarCamera();
//		Platform.exit();
//		System.out.println("Platform.exit()");

	}

	private void salvarUser(Mat resizeImage) {
		if (isNovoUser.isSelected()) {
			novoNome = edtNome.getText();
			if (!novoNome.isEmpty()) {
				if (verificaCodAssoc(novoNome)) {
					if (verificaUser(novoNome)) {
						Imgcodecs.imwrite(path + "\\imgs\\imgTratadas\\" + novoNome + "-" + maxLabel + ".png",
								resizeImage);
						System.out.println(novoNome + "-" + maxLabel + ".png");
					} else {
						System.out.println("Qtd mais que 2 fotos, update a mais antiga");
						Integer label = getLabel(novoNome);
						Imgcodecs.imwrite(path + "\\imgs\\imgTratadas\\" + novoNome + "-" + label + ".png",
								resizeImage);
						System.out.println("User Salvo");
						System.out.println("User Update: " + novoNome + "-" + label + ".png");
					}

					tv_validacao.setText("Salvando Usuário");
					tv_validacao.setVisible(true);
					cameraButton.setVisible(false);
					isNovoUser.setVisible(false);
					edtNome.setVisible(false);

					this.stopAcquisition();
					modeloDeTreino();
				} else {
					System.out.println("CodAssoc Errado: " + novoNome.substring(1, 4) + " Esperado: " + codAssoc);
					dialogWarning();

				}

			}
		}
	}

	private void dialogWarning() {
//		if (EnstiWeb.DEBUG)
//			System.out.println("Estou em FaceDetectionController --> dialogWarning() ...(fim)");
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Error N° da carteririnha");
				alert.setHeaderText("Codigo da associacao errado");
				alert.setContentText(
						"Codigo da associacao errado ou número da carterinha inválido, corrija e tente novamente");
				alert.show();

			}
		});
		this.stopAcquisition();
	}

	private void receberComando() {
		System.out.println("receberComando");

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
					String s;
					try {
						while ((s = reader.readLine()) != null) {
							if (s.startsWith("COMANDO ->")) {
								System.out.println("*** COMANDO -> " + s);

								isAguardandoVerificacao = false;
								System.out.println("*** isAguardandoVerificacao -> " + isAguardandoVerificacao);
								isPosVerificacao = true;

								if (s.contains("FECHAR")) {
									Thread.currentThread().interrupt();
								}

							} else {
								System.out.println("*** RECEBIDO -> " + s + " toCharArray: " + s.toCharArray().length);

							}
						}
					} catch (IOException e) {
						System.out.println("*** Error");
					}
				} catch (Exception e) {
					System.out.println("*** Error receberComando");
					System.out.println(e.getMessage());
				}
			}

		}).start();
	}

	private Boolean verificaCodAssoc(String matricula) {
		if (matricula.toCharArray().length == 10) {
			if (matricula.substring(1, 4).equals(codAssoc) || matricula.substring(1, 4).equals("000")) {
				return true;
			}
		}

		return false;
	}

	private Integer getLabel(String userName) {
		for (Map.Entry<Integer, String> map : nomes.entrySet()) {
			if (userName.equals(map.getValue())) {
				return map.getKey();
			}
		}
		return 0;
	}

	private boolean verificaUser(String userName) {
		int user = 0;
		for (Map.Entry<Integer, String> map : nomes.entrySet()) {
			if (userName.equals(map.getValue())) {
				System.out.println("User Localizado");
				user++;
			}
		}
		if (user >= 2) {
			return false;
		} else {
			return true;
		}
	}

	private double[] faceRecognition(Mat currentFace) {
		int[] predLabel = new int[1];
		double[] confidence = new double[1];
		int result = -1;

		if (faceTreinada == null) {
			System.out.println("faceTreinada == null");
			return null;
		} else {
			faceTreinada.predict(currentFace, predLabel, confidence);
			result = predLabel[0];

			return new double[] { result, confidence[0] };
		}
	}

	private void iniciaTreinamento() {

		faceTreinada = Face.createLBPHFaceRecognizer();

		if (faceTreinada == null) {
			System.out.println("iniciaTreinamento faceTreinada == null");

		} else {
			System.out.println("Variavel carregada");
		}

		faceTreinada.load(path + "\\modelo\\traineddata");

		System.out.println("Pronto para uso");

//		startCamera();
		validacaoFx();
		receberComando();
		iniciarCamera();
	}

	private void modeloDeTreino() {
		System.out.println(
				"***[**] " + timerSet + " - " + codAssoc + " - " + path + " - " + nuEquip + " - " + confidenceInt);

		new Thread(new Runnable() {

			@Override
			public void run() {
				String matUser = "";
				int count = 0;

				File root = new File(path + "\\imgs\\imgTratadas");
				FilenameFilter imgFilter = new FilenameFilter() {

					@Override
					public boolean accept(File dir, String name) {
						name = name.toLowerCase();
						return name.endsWith(".png");
					}
				};

				File[] imageFiles = separarFiles(imgFilter, root);

				List<Mat> images = new ArrayList<Mat>();

				System.out.println("Número de imagens lidas: " + imageFiles.length);

				Mat labels = new Mat(imageFiles.length, 1, CvType.CV_32SC1);

				int counter = 0;

				for (File image : imageFiles) {
					Mat img = Imgcodecs.imread(image.getAbsolutePath());
					Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
					Imgproc.equalizeHist(img, img);

					String nome = image.getName().split("\\-")[0];
					String labelId = image.getName().split("\\-")[1];
					int label = Integer.parseInt(labelId.split("\\.")[0]);

					if (label >= maxLabel) {
						maxLabel = label + 1;
					}

					images.add(img);
					labels.put(counter, 0, label);
					nomes.put(label, nome);

					counter++;
				}

				System.out.println("Leitura Banco Imagens Finalizada");

				FaceRecognizer faceRecognizer = Face.createLBPHFaceRecognizer();

				faceRecognizer.train(images, labels);

				System.out.println("Arquivo de treinanento gerado");

				faceRecognizer.save(path + "\\modelo\\traineddata");

				System.out.println("Arquivo de treinamento salvo");

				iniciaTreinamento();
			}
		}).start();
	}

	private File[] separarFiles(FilenameFilter imgs, File root) {
		int count = 0;
		String matricula = "";

		File[] allImages = root.listFiles(imgs);
		List<File> imgEscolhidas = new ArrayList<File>();
		System.out.println("Qtd de imgs totais: " + allImages.length);

		for (File bruto : allImages) {
			String nome = bruto.getName().split("\\-")[0];
			String labelId = bruto.getName().split("\\-")[1];
			int label = Integer.parseInt(labelId.split("\\.")[0]);

			if (matricula.equals("")) {
				matricula = nome;
			}

			if (!matricula.equals(nome)) {
				matricula = nome;
				count = 0;
			}

			if (count < 2) {
				imgEscolhidas.add(bruto);
			}
			count++;

		}

		return imgEscolhidas.stream().toArray(File[]::new);
	}

	private void updateImageView(ImageView view, Image image) {
		Utils.onFxThread(view.imageProperty(), image);
	}

	public void setClosed() {
		this.stopAcquisition();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.capture = new VideoCapture();
		this.faceCascade = new CascadeClassifier();
		this.absoluteFaceSize = 0;

		originalFrame.setFitWidth(300);
		originalFrame.setFitHeight(300);
		originalFrame.setPreserveRatio(true);
		this.timerSet = GlobalVars.getTimerSet();
		this.codAssoc = GlobalVars.getCodAssoc();
		this.path = GlobalVars.getPath();
		this.nuEquip = GlobalVars.getNuEquip();
		this.confidenceInt = GlobalVars.getConfidenceInt();
		selectLBP();
		modeloDeTreino();
	}

}
