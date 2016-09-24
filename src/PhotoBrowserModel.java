import java.io.File;

public class PhotoBrowserModel {

	public enum ViewMode {
		PhotoViewer,
		Browser,
		Split
	}
	
	AnnotatedAlbum album;
	int currentViewingIndex = 0;
	boolean flipped = false;
	ViewMode mode = ViewMode.PhotoViewer;
	
	
	public PhotoBrowserModel() {
		album = new AnnotatedAlbum();
	}
	
	public void addPhotos(File[] url) {
		currentViewingIndex = album.photoList.size();
		for (int i = 0; i < url.length; i++) {
			AnnotatedPhoto newPhoto = new AnnotatedPhoto(url[i]);
			album.photoList.add(newPhoto);
			newPhoto.loadPhoto();
		}
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
	
	public void deleteAnnotatedPhoto() {
		// TODO
	}
	public void deleteAnnotatedPhoto(int index) {
		// TODO
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
