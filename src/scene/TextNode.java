package scene;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;

public class TextNode extends Node {
	
	private static final long serialVersionUID = 2L;
	
	public String text;
	public double lineSpacing = 1;
	public int width = Integer.MAX_VALUE;
	
	public transient boolean showMarker = false;
	public transient int markerPosition = 0;
	
	private transient PolygonNode marker;
	
	private Rectangle calculatedBounds;
	
	public TextNode() {
		text = "";
		createMarker();
	}
	public TextNode(String text) {
		this.text = text;
		createMarker();
	}

	@Override
	public Rectangle getContentBounds() {
		if (calculatedBounds == null) {
			return new Rectangle();
		} else {
			return calculatedBounds;
		}
	}
	
	@Override
	public void paintNode(Context context) {
		FontMetrics metrics = context.graphics.getFontMetrics();
		ArrayList<String> lines = splitString(text, width, metrics);
		
		int posY = 0;
		//int lineHeight = (int)(context.getTextSize() * lineSpacing);
		int lineHeight = (int)(metrics.getHeight() * lineSpacing);
		int lineStartPos = 0;
		int maxWidth = 0;
		
		context.beginText();
		
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			context.graphics.drawString(line, 0, posY);
			maxWidth = Math.max(maxWidth, Math.min(metrics.stringWidth(line), width));
			
			// Calculate the editing mark's position on the image
			if (showMarker && markerPosition >= lineStartPos) {
				if (markerPosition == lineStartPos + line.length() && i == lines.size() - 1 && line.charAt(line.length() - 1) == '\n') {
					marker.setPosition(new Point(0, posY + lineHeight));
				} else if (markerPosition < lineStartPos + line.length() || i == lines.size() - 1) {
					String sub = line.substring(0, markerPosition - lineStartPos);
					marker.setPosition(new Point(metrics.stringWidth(sub), posY));
				}
			}
			
			lineStartPos += line.length();
			posY += lineHeight;
		}
		if (lines.size() == 0) {
			marker.setPosition(new Point(0, 0));
		}
		
		calculatedBounds = new Rectangle(0, -lineHeight, maxWidth, lineHeight * lines.size());
	}
	
	@Override
	public void paintChildren(Context context) {
		if (showMarker) {
			marker.setScale(textSize / 100.0, textSize / 100.0);
			marker.paint(context);
		}
		super.paintChildren(context);
	}
	
	// Find the best prefix of the string that fits in the given space. End with a \n or space if possible.
	private int trimString(String str, int spaceInPixel, FontMetrics metrics) {
		int cutIndex = str.indexOf('\n');
		if (cutIndex >= 0) {
			str = str.substring(0, cutIndex + 1);
		}
		while (metrics.stringWidth(str) > spaceInPixel) {
			cutIndex = str.lastIndexOf(' ');
			if (cutIndex == -1) {
				cutIndex = str.length() - 1;
			}
			str = str.substring(0, cutIndex);
		}
		return str.length();
	}
	
	// Split the string into multiple lines given the width constraint.
	private ArrayList<String> splitString(String str, int spaceInPixel, FontMetrics metrics) {
		ArrayList<String> lines = new ArrayList<>();
		
		while (str.length() > 0) {
			int length = trimString(str, width, metrics);
			if (str.charAt(0) != '\n' && length == 0) break; // The width is too small, cannot even hold one character!
			
			// Get rid of all the excessive space if it is not following \n
			if (str.charAt(length - 1) != '\n') {
				while (length < str.length() && str.charAt(length) == ' ') {
					length++;
				}
			}
			
			lines.add(str.substring(0, length));
			str = str.substring(length);
		}
		return lines;
	}
	
	private void createMarker() {
		ArrayList<Point> markerCorners = new ArrayList<>();
		markerCorners.add(new Point(0, 0));
		markerCorners.add(new Point(-25, 50));
		markerCorners.add(new Point(25, 50));
		marker = new PolygonNode(markerCorners);
		marker.fillColor = Color.BLACK;
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		createMarker();
	}
}
