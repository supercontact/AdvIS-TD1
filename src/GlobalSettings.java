import java.io.File;
import java.nio.file.FileSystems;

public class GlobalSettings {
	public static File savedSettingsLocation = FileSystems.getDefault().getPath("settings.srz").toFile();
	public static File savedAlbumLocation = FileSystems.getDefault().getPath("data", "albums", "defaultAlbum.srz").toFile();
	
	public static File backgroundImageLocation = FileSystems.getDefault().getPath("data", "resources", "bg.jpg").toFile();
	public static File frameImageLocation = FileSystems.getDefault().getPath("data", "resources", "frame.png").toFile();
	public static File errorImageLocation = FileSystems.getDefault().getPath("data", "resources", "noImage.png").toFile();
	
	public static String[] fontStrings = {"Arial", "Georgia", "Impact", "Comic Sans MS", "Courier New"};
}
