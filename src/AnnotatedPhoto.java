import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class AnnotatedPhoto implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public File imageURL;
	public ArrayList<String> tags;
	public String annotation = "";
	public ArrayList<StrokeMark> strokes;
	
	transient public BufferedImage image;
	
	public AnnotatedPhoto(File url) {
		imageURL = url;
		tags = new ArrayList<>();
		strokes = new ArrayList<>();
	}
	
	public boolean loadPhoto() {
		try {
			image = ImageIO.read(imageURL);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	
	public static class StrokeMark implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		public int x1, y1, x2, y2;
		public Color color;
		public float width;
	}
}
