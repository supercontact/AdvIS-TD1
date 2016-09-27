import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class AnnotatedPhoto implements Serializable {
	
	private static final long serialVersionUID = 3L;

	public File imageURL;
	public ArrayList<String> tags;
	public ArrayList<Annotation> annotations;
	public ArrayList<StrokeMark> strokes;
	
	private int addonCount = 0;
	
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
	
	public Annotation createAnnotation() {
		Annotation annotation = new Annotation();
		annotation.text = "";
		annotation.color = Color.black;
		annotation.size = 15;
		annotation.position = new Point();
		// Default value
		
		annotation.index = addonCount++;
		annotations.add(annotation);
		return annotation;
	}
	
	public StrokeMark createStroke() {
		StrokeMark stroke = new StrokeMark();
		stroke.color = Color.black;
		stroke.width = 5;
		// Default value
		
		stroke.index = addonCount++;
		strokes.add(stroke);
		return stroke;
	}
	
	public void undo() {
		if (annotations.size() > 0 && 
				(strokes.size() == 0 || annotations.get(annotations.size() - 1).index > strokes.get(strokes.size() - 1).index)) {
			annotations.remove(annotations.size() - 1);
			addonCount--;
		} else if (strokes.size() > 0 && 
				(annotations.size() == 0 || strokes.get(strokes.size() - 1).index > annotations.get(annotations.size() - 1).index)) {
			strokes.remove(strokes.size() - 1);
			addonCount--;
		}
	}
	
	
	public static class StrokeMark implements Serializable {
		
		private static final long serialVersionUID = 2L;
		
		public ArrayList<Point> path;
		public Color color;
		public double width;
		
		protected int index;
		
		public StrokeMark() {
			path = new ArrayList<Point>();
		}
	}
	
	public static class Annotation implements Serializable {

		private static final long serialVersionUID = 3L;
		
		public String text;
		public Color color;
		public int size;
		public String font;
		public Point position;
		
		protected int index;
	}
}
