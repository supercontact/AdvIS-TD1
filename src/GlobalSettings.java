import java.io.File;
import java.nio.file.FileSystems;

public class GlobalSettings {
	public final static File savedSettingsLocation = FileSystems.getDefault().getPath("settings.srz").toFile();
	public final static File savedAlbumLocation = FileSystems.getDefault().getPath("data", "albums", "defaultAlbum.srz").toFile();
	
	public final static String[] fontStrings = {"Arial", "Georgia", "Impact", "Comic Sans MS", "Courier New"};
	public final static int thumbnailSize = 256;
}
