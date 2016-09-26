import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class AnnotatedPhoto implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public File imageURL;
	public ArrayList<String> tags;
	public ArrayList<Annotation> annotations;
	public ArrayList<StrokeMark> strokes;
	
	transient public Image image;
	transient public boolean imageLoaded = false;
	
	public AnnotatedPhoto(File url) {
		imageURL = url;
		tags = new ArrayList<>();
		annotations = new ArrayList<>();
		strokes = new ArrayList<>();
	}
	
	public boolean loadPhoto() {
		try {
			image = ImageIO.read(imageURL);
			imageLoaded = true;
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	
	public static class StrokeMark implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		public ArrayList<Point> path;
		public Color color;
		public double width;
		
		public StrokeMark() {
			path = new ArrayList<Point>();
		}
	}
	
	public static class Annotation implements Serializable {

		private static final long serialVersionUID = 1L;
		
		public String text;
		public Color color;
		public int size;
		public Point position;
	}
}
