package custom;
import java.io.File;
import java.io.Serializable;
import java.nio.file.FileSystems;

import util.SerializationControl;

// This class holds user specific settings that are kept between launches of the application.
public class SavedSettings implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static SavedSettings settings;
	
	public static void loadSettings() {
		settings = (SavedSettings)SerializationControl.load(GlobalSettings.savedSettingsLocation);
		if (settings == null) {
			createNewSettings();
		}
	}
	
	public static void saveSettings() {
		SerializationControl.save(settings, GlobalSettings.savedSettingsLocation);
	}
	
	public static void createNewSettings() {
		settings = new SavedSettings();
		settings.defaultFileLocation = FileSystems.getDefault().getPath("").toFile();
	}
	
	
	public File defaultFileLocation;

}
