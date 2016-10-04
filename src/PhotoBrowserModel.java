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
		album = PhotoApplication.app.album;
		if (album.photoList.size() > 0) {
			currentViewingIndex = 0;
		}
	}
	
	public boolean isShowingPhoto() {
		return currentViewingIndex >= 0;
	}
	
	public void addPhotos(File[] url) {
		currentViewingIndex = album.photoList.size();
		flipped = false;
		for (int i = 0; i < url.length; i++) {
			album.importNewPhoto(url[i]).loadPhoto();
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
		album.photoList.get(index).primitives.clear();
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
	
	public void jumpTo(int index) {
		if (index < getPhotoCount()) {
			currentViewingIndex = index;
		}
	}
	
	public boolean saveAlbum() {
		return album.saveAlbum();
	}
	
	public boolean loadAlbum() {
		AnnotatedAlbum loadedAlbum = AnnotatedAlbum.loadAlbum();
		if (loadedAlbum == null) return false;
		album = loadedAlbum;
		if (album.photoList.size() > 0) {
			currentViewingIndex = 0;
		}
		return true;
	}
}
