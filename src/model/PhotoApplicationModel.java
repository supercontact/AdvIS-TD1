package model;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PhotoApplicationModel {

	public enum ViewMode {
		PhotoViewer,
		Browser,
		Split,
		Hide
	}
	
	public AnnotatedAlbum album;
	public Set<AnnotatedPhoto> selectedPhotos;
	
	private ViewMode mode = ViewMode.Hide;
	private int currentViewingIndex = -1;
	private boolean flipped = false;
	
	private File albumLocation;
	
	private ArrayList<PhotoListener> listeners;
	
	public PhotoApplicationModel() {
		selectedPhotos = new HashSet<>();
		listeners = new ArrayList<>();
	}
	
	public boolean isShowingPhoto() {
		return currentViewingIndex >= 0;
	}
	
	public ViewMode getViewMode() {
		return mode;
	}
	public void setViewMode(ViewMode mode) {
		ViewMode oldMode = this.mode;
		if (oldMode != mode) {
			this.mode = mode;
			firePhotoEvent(PhotoEvent.CreateViewModeChangedEvent(this, oldMode, mode));
		}
	}
	
	public int getCurrentViewingIndex() {
		return currentViewingIndex;
	}
	public void setCurrentViewingIndex(int currentViewingIndex) {
		int oldIndex = this.currentViewingIndex;
		this.currentViewingIndex = currentViewingIndex;
		firePhotoEvent(PhotoEvent.CreateViewIndexChangedEvent(this, oldIndex, currentViewingIndex));
	}
	
	public void nextPhoto() {
		if (currentViewingIndex != -1) {
			int oldIndex = currentViewingIndex;
			currentViewingIndex = (currentViewingIndex + 1) % album.photoList.size();
			firePhotoEvent(PhotoEvent.CreateViewIndexChangedEvent(this, oldIndex, currentViewingIndex));
		}
	}
	public void prevPhoto() {
		if (currentViewingIndex != -1) {
			int oldIndex = currentViewingIndex;
			currentViewingIndex = (currentViewingIndex - 1 + album.photoList.size()) % album.photoList.size();
			firePhotoEvent(PhotoEvent.CreateViewIndexChangedEvent(this, oldIndex, currentViewingIndex));
		}
	}
	
	public boolean isFlipped() {
		return flipped;
	}
	public void setFlipped(boolean flipped) {
		if (this.flipped != flipped) {
			this.flipped = flipped;
			firePhotoEvent(PhotoEvent.CreatePhotoFlippedEvent(this, flipped));
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
	
	public void addPhotos(File[] url) {
		int oldIndex = currentViewingIndex;
		currentViewingIndex = album.photoList.size();
		flipped = false;
		for (int i = 0; i < url.length; i++) {
			album.importNewPhoto(url[i]).loadPhoto();
		}
		saveAlbum();
		for (int i = currentViewingIndex; i < album.photoList.size(); i++) {
			firePhotoEvent(PhotoEvent.CreatePhotoAddedEvent(this, album.photoList.get(i)));
		}
		firePhotoEvent(PhotoEvent.CreateViewIndexChangedEvent(this, oldIndex, currentViewingIndex));
	}
	
	public void deletePhoto() {
		deletePhoto(currentViewingIndex);
	}
	public void deletePhoto(int index) {
		if (index < 0) return;
		AnnotatedPhoto oldPhoto = album.photoList.get(index);
		album.removePhoto(index);
		saveAlbum();
		firePhotoEvent(PhotoEvent.CreatePhotoRemovedEvent(this, oldPhoto));
		if (album.photoList.size() <= currentViewingIndex) {
			currentViewingIndex--;
			firePhotoEvent(PhotoEvent.CreateViewIndexChangedEvent(this, currentViewingIndex + 1, currentViewingIndex));
		}
	}
	
	public void clearPhoto() {
		clearPhoto(currentViewingIndex);
	}
	public void clearPhoto(int index) {
		if (index < 0) return;
		AnnotatedPhoto photo = album.photoList.get(index);
		photo.strokes.clear();
		photo.annotations.clear();
		photo.primitives.clear();
		saveAlbum();
		firePhotoEvent(PhotoEvent.CreatePhotoAnnotationChangedEvent(this, photo));
	}
	
	public void setAlbumLocation(File url) {
		albumLocation = url;
	}
	
	public boolean saveAlbum() {
		if (album.saveAlbum(albumLocation)) {
			firePhotoEvent(PhotoEvent.CreateAlbumSavedEvent(this));
			return true;
		}
		return false;
	}
	
	public boolean loadAlbum() {
		album = AnnotatedAlbum.loadAlbum(albumLocation);
		if (album == null)  {
			album = new AnnotatedAlbum();
		}
		if (album.photoList.size() > 0) {
			currentViewingIndex = 0;
		}
		album.loadAllPhotos();
		return true;
	}
	
	// Events
	public void addPhotoListener(PhotoListener listener) {
		listeners.add(listener);
	}
	public void removePhotoListener(PhotoListener listener) {
		listeners.remove(listener);
	}
	public void firePhotoEvent(PhotoEvent e) {
		// Make a copy to avoid concurrent modification.
		PhotoListener[] listenersCopy = listeners.toArray(new PhotoListener[0]);
		for (PhotoListener listener : listenersCopy) {
			listener.photoEventReceived(e);
		}
	}
}
