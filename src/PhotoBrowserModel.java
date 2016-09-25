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
	
	public boolean saveAlbum(File url) {
		// TODO
		return false;
	}

	public boolean loadAlbum(File url) {
		// TODO
		return false;
	}
}
