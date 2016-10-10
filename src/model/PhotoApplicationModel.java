package model;
import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

// This class is the core model of the whole application.
// It provides core functionalities like adding/removing photos, saving/loading album, etc.
// It will fire corresponding PhotoEvents when things happen.

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
	private Image errorImage;
	private Image errorImageThumbnail;
	private int thumbnailSize = 256;
	
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
			AnnotatedPhoto newPhoto = album.importNewPhoto(url[i]);
			newPhoto.loadPhoto();
			newPhoto.generateThumbnail(thumbnailSize);
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
		if (album.photoList.size() <= currentViewingIndex) {
			currentViewingIndex--;
			firePhotoEvent(PhotoEvent.CreateViewIndexChangedEvent(this, currentViewingIndex + 1, currentViewingIndex));
		}
		firePhotoEvent(PhotoEvent.CreatePhotoRemovedEvent(this, oldPhoto));
	}
	public void deleteSelectedPhotos() {
		for (AnnotatedPhoto photo : selectedPhotos) {
			deletePhoto(photo.getIndex());
		}
		selectedPhotos.clear();
	}
	
	public void clearPhoto() {
		clearPhoto(currentViewingIndex);
	}
	public void clearPhoto(int index) {
		if (index < 0) return;
		AnnotatedPhoto photo = album.photoList.get(index);
		photo.clear();
		saveAlbum();
		firePhotoEvent(PhotoEvent.CreatePhotoAnnotationChangedEvent(this, photo));
	}
	
	public void selectPhoto(int index) {
		AnnotatedPhoto photo = album.photoList.get(index);
		if (!selectedPhotos.contains(photo)) {
			selectedPhotos.add(photo);
			firePhotoEvent(PhotoEvent.CreatePhotoSelectedEvent(this, photo));
		}
		
	}
	
	public void deselectPhoto(int index) {
		AnnotatedPhoto photo = album.photoList.get(index);
		if (selectedPhotos.contains(photo)) {
			selectedPhotos.remove(photo);
			firePhotoEvent(PhotoEvent.CreatePhotoDeselectedEvent(this, photo));
		}
		
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
		album.generateAllThumbnails(thumbnailSize);
		for (AnnotatedPhoto photo : album.photoList) {
			if (!photo.imageLoaded) {
				photo.image = errorImage;
				photo.thumbnail = errorImageThumbnail;
			}
		}
		firePhotoEvent(PhotoEvent.CreateAlbumLoadedEvent(this));
		return true;
	}
	
	// Specifying certain parameters
	public void setAlbumLocation(File url) {
		albumLocation = url;
	}
	public void setErrorImage(Image image) {
		errorImage = image;
	}
	public void setErrorImageThumbnail(Image image) {
		errorImageThumbnail = image;
	}
	public void setThumbnailSize(int size) {
		thumbnailSize = size;
	}
	
	
	// Event management
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
