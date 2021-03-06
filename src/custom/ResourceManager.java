package custom;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;

import javax.imageio.ImageIO;

import component.PhotoApplication;

// This class manages the loading of resources of the application.
// The resources include button icons, background, image labels, etc.
public class ResourceManager {

	public static BufferedImage backgroundImage;
	public static BufferedImage frameImage;
	public static BufferedImage errorImage;
	public static Image errorImageThumbnail;
	public static BufferedImage prevIcon;
	public static BufferedImage nextIcon;
	public static BufferedImage returnIcon;
	public static BufferedImage lineIcon;
	public static BufferedImage straightLineIcon;
	public static BufferedImage rectangleIcon;
	public static BufferedImage ellipseIcon;
	public static BufferedImage lineWidthIcon;
	public static BufferedImage textSizeIcon;
	public static BufferedImage colorIcon;
	public static BufferedImage trashIcon;
	public static BufferedImage checkBoxIconSelected;
	public static BufferedImage checkBoxIconUnselected;
	public static BufferedImage checkBoxIconHalfselected;
	
	private final static File backgroundImageLocation = findResource("bg.jpg");
	private final static File frameImageLocation = findResource("frame.png");
	private final static File errorImageLocation = findResource("noImage.png");
	private final static File prevIconLocation = findResource("prev.png");
	private final static File nextIconLocation = findResource("next.png");
	private final static File returnIconLocation = findResource("return.png");
	private final static File lineIconLocation = findResource("line.png");
	private final static File straightLineIconLocation = findResource("straightLine.png");
	private final static File rectangleIconLocation = findResource("rectangle.png");
	private final static File ellipseIconLocation = findResource("ellipse.png");
	private final static File lineWidthIconLocation = findResource("lineWidth.png");
	private final static File textSizeIconLocation = findResource("textSize.png");
	private final static File colorIconLocation = findResource("color.png");
	private final static File trashIconLocation = findResource("trash.png");
	private final static File checkBoxIconSelectedLocation = findResource("checkbox1.png");
	private final static File checkBoxIconUnselectedLocation = findResource("checkbox2.png");
	private final static File checkBoxIconHalfselectedLocation = findResource("checkbox3.png");
	
	public static void loadResources() {
		try {
			backgroundImage = ImageIO.read(backgroundImageLocation);
			frameImage = ImageIO.read(frameImageLocation);
			errorImage = ImageIO.read(errorImageLocation);
			errorImageThumbnail = errorImage.getScaledInstance(GlobalSettings.thumbnailSize, GlobalSettings.thumbnailSize, Image.SCALE_SMOOTH);
			prevIcon = ImageIO.read(prevIconLocation);
			nextIcon = ImageIO.read(nextIconLocation);
			returnIcon = ImageIO.read(returnIconLocation);
			lineIcon = ImageIO.read(lineIconLocation);
			straightLineIcon = ImageIO.read(straightLineIconLocation);
			rectangleIcon = ImageIO.read(rectangleIconLocation);
			ellipseIcon = ImageIO.read(ellipseIconLocation);
			lineWidthIcon = ImageIO.read(lineWidthIconLocation);
			textSizeIcon = ImageIO.read(textSizeIconLocation);
			colorIcon = ImageIO.read(colorIconLocation);
			trashIcon = ImageIO.read(trashIconLocation);
			checkBoxIconSelected = ImageIO.read(checkBoxIconSelectedLocation);
			checkBoxIconUnselected = ImageIO.read(checkBoxIconUnselectedLocation);
			checkBoxIconHalfselected = ImageIO.read(checkBoxIconHalfselectedLocation);
		} catch (IOException e) {
			e.printStackTrace();
			PhotoApplication.showStatusText("Resources loading error!");
		}
	}
	
	private static File findResource(String name) {
		return FileSystems.getDefault().getPath("data", "resources", name).toFile();
	}

}
