package application;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.face.Face;
import org.opencv.face.FaceRecognizer;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ImgToInt {

	public static HashMap<Integer, String> nomes = new HashMap<Integer, String>();

	public static void main(String[] args) {
		File root = new File("C:\\Users\\lhcteles\\eclipse-workspace\\FaceDetection\\imagens\\imgTratada");

		FilenameFilter imgFilter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				name = name.toLowerCase();
				return name.endsWith(".jpg");
			}
		};

		File[] imageFiles = root.listFiles(imgFilter);

		List<Mat> images = new ArrayList<Mat>();

		System.out.println("Número de imagens lidas é: " + imageFiles.length);

//	List<Integer> trainingLabels = new ArrayList<>();

		Mat labels = new Mat(imageFiles.length, 1, CvType.CV_32SC1);

		int counter = 0;

		for (File image : imageFiles) {
			Mat img = Imgcodecs.imread(image.getAbsolutePath());
			Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
			Imgproc.equalizeHist(img, img);

			int label = Integer.parseInt(image.getName().split("\\-")[0]);
			String labnname = image.getName().split("\\_")[0];

			String name = labnname.split("\\-")[1];
			name = name + " - Imagem: " + image.getName().split("\\_")[1];

			nomes.put(label, name);
			images.add(img);
			
			System.out.println("label: " + label + "\nnome: " + name);


			labels.put(counter, 0, label);
			counter++;
		}
	}
}
