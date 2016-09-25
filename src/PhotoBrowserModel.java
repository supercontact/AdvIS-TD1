import java.io.File;

public class PhotoBrowserModel {

	public enum ViewMode {
		PhotoViewer,
		Browser,
		Split
	}
	
	AnnotatedAlbum album;
	int currentViewingIndex = -1;
	boolean flipped = false;
	ViewMode mode = ViewMode.PhotoViewer;
	
	
	public PhotoBrowserModel() {
		album = new AnnotatedAlbum();
	}
	
	public void addPhotos(File[] url) {
		currentViewingIndex = album.photoList.size();
		flipped = false;
		for (int i = 0; i < url.length; i++) {
			AnnotatedPhoto newPhoto = new AnnotatedPhoto(url[i]);
			album.photoList.add(newPhoto);
			newPhoto.loadPhoto();
		}
	}
	
	public boolean deletePhoto() {
		return deletePhoto(currentViewingIndex);
	}
	public boolean deletePhoto(int index) {
		if (index < 0) return false;
		album.photoList.remove(index);
		if (album.photoList.size() <= currentViewingIndex) {
			currentViewingIndex--;
		}
		return true;
	}
	
	public boolean clearPhoto() {
		return clearPhoto(currentViewingIndex);
	}
	public boolean clearPhoto(int index) {
		if (index < 0) return false;
		album.photoList.get(index).strokes.clear();
		album.photoList.get(index).annotations.clear();
		return true;
	}
	
	public int getPhotoCount() {
		return album.photoList.size();
	}
	
	public AnnotatedPhoto getAnnotatedPhoto() {
		return album.photoList.get(currentViewingIndex);
	}
	public AnnotatedPhoto getAnnotatedPhoto(int index) {
		return album.photoList.get(index);
	}
	
	public void nextPhoto() {
		if (currentViewingIndex != -1) {
			currentViewingIndex = (currentViewingIndex + 1) % album.photoList.size();
		}
	}
	
	public void prevPhoto() {
		if (currentViewingIndex != -1) {
			currentViewingIndex = (currentViewingIndex - 1 + album.photoList.size()) % album.photoList.size();
		}
	}
	
	public boolean saveAlbum() {
		return SerializationControl.save(album, GlobalSettings.savedAlbumLocation);
	}

	public boolean loadAlbum() {
		if (GlobalSettings.savedAlbumLocation.exists()) {
			AnnotatedAlbum loadedAlbum = (AnnotatedAlbum)SerializationControl.load(GlobalSettings.savedAlbumLocation);
			if (loadedAlbum != null) {
				album = loadedAlbum;
				if (album.photoList.size() > 0) {
					currentViewingIndex = 0;
				}
				for (AnnotatedPhoto photo : album.photoList) {
					photo.loadPhoto();
				}
				return true;
			}
		}
		return false;
	}
}
