import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;

import javax.imageio.ImageIO;

public class ResourceManager {

	public static BufferedImage backgroundImage;
	public static BufferedImage frameImage;
	public static BufferedImage errorImage;
	public static BufferedImage prevIcon;
	public static BufferedImage nextIcon;
	public static BufferedImage lineIcon;
	public static BufferedImage straightLineIcon;
	public static BufferedImage rectangleIcon;
	public static BufferedImage ellipseIcon;
	public static BufferedImage lineWidthIcon;
	public static BufferedImage textSizeIcon;
	public static BufferedImage colorIcon;
	
	private final static File backgroundImageLocation = findResource("bg.jpg");
	private final static File frameImageLocation = findResource("frame.png");
	private final static File errorImageLocation = findResource("noImage.png");
	private final static File prevIconLocation = findResource("prev.png");
	private final static File nextIconLocation = findResource("next.png");
	private final static File lineIconLocation = findResource("line.png");
	private final static File straightLineIconLocation = findResource("straightLine.png");
	private final static File rectangleIconLocation = findResource("rectangle.png");
	private final static File ellipseIconLocation = findResource("ellipse.png");
	private final static File lineWidthIconLocation = findResource("lineWidth.png");
	private final static File textSizeIconLocation = findResource("textSize.png");
	private final static File colorIconLocation = findResource("color.png");
	
	public static void loadResources() {
		try {
			backgroundImage = ImageIO.read(backgroundImageLocation);
			frameImage = ImageIO.read(frameImageLocation);
			errorImage = ImageIO.read(errorImageLocation);
			prevIcon = ImageIO.read(prevIconLocation);
			nextIcon = ImageIO.read(nextIconLocation);
			lineIcon = ImageIO.read(lineIconLocation);
			straightLineIcon = ImageIO.read(straightLineIconLocation);
			rectangleIcon = ImageIO.read(rectangleIconLocation);
			ellipseIcon = ImageIO.read(ellipseIconLocation);
			lineWidthIcon = ImageIO.read(lineWidthIconLocation);
			textSizeIcon = ImageIO.read(textSizeIconLocation);
			colorIcon = ImageIO.read(colorIconLocation);
		} catch (IOException e) {
			e.printStackTrace();
			PhotoApplication.showStatusText("Resources loading error!");
		}
	}
	
	public static BufferedImage cloneImage(BufferedImage source) {
		BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
	    Graphics g = copy.getGraphics();
	    g.drawImage(source, 0, 0, null);
	    g.dispose();
	    return copy;
	}
	
	private static File findResource(String name) {
		return FileSystems.getDefault().getPath("data", "resources", name).toFile();
	}

}
