import java.io.File;
import java.nio.file.FileSystems;

public class GlobalSettings {
	public final static File savedSettingsLocation = FileSystems.getDefault().getPath("settings.srz").toFile();
	public final static File savedAlbumLocation = FileSystems.getDefault().getPath("data", "albums", "defaultAlbum.srz").toFile();
	
	public final static File backgroundImageLocation = FileSystems.getDefault().getPath("data", "resources", "bg.jpg").toFile();
	public final static File frameImageLocation = FileSystems.getDefault().getPath("data", "resources", "frame.png").toFile();
	public final static File errorImageLocation = FileSystems.getDefault().getPath("data", "resources", "noImage.png").toFile();
	public final static File prevIconLocation = FileSystems.getDefault().getPath("data", "resources", "prev.png").toFile();
	public final static File nextIconLocation = FileSystems.getDefault().getPath("data", "resources", "next.png").toFile();
	public final static File lineIconLocation = FileSystems.getDefault().getPath("data", "resources", "line.png").toFile();
	public final static File rectangleIconLocation = FileSystems.getDefault().getPath("data", "resources", "rectangle.png").toFile();
	public final static File ellipseIconLocation = FileSystems.getDefault().getPath("data", "resources", "ellipse.png").toFile();
	public final static File lineWidthIconLocation = FileSystems.getDefault().getPath("data", "resources", "lineWidth.png").toFile();
	public final static File textSizeIconLocation = FileSystems.getDefault().getPath("data", "resources", "textSize.png").toFile();
	public final static File colorIconLocation = FileSystems.getDefault().getPath("data", "resources", "color.png").toFile();
	
	public static String[] fontStrings = {"Arial", "Georgia", "Impact", "Comic Sans MS", "Courier New"};
}
