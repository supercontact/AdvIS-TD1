import java.io.File;
import java.nio.file.FileSystems;

public class GlobalSettings {
	public static File savedSettingsLocation = FileSystems.getDefault().getPath("settings.srz").toFile();
}