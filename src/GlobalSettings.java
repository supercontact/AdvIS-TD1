import java.io.File;
import java.nio.file.FileSystems;

public class GlobalSettings {
	public static File savedSettingsLocation = FileSystems.getDefault().getPath("settings.srz").toFile();
	public static File backgroundImageLocation = FileSystems.getDefault().getPath("data", "resources", "bg.jpg").toFile();
	public static File frameImageLocation = FileSystems.getDefault().getPath("data", "resources", "frame.png").toFile();
}
