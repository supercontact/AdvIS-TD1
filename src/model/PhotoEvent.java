package model;

// This class represents an event fired by the PhotoApplicationModel.
// It can be of many different types.

public class PhotoEvent {
	public enum Type {
		ViewModeChanged,
		ViewIndexChanged,
		PhotoFlipped,
		PhotoAnnotationChanged,
		PhotoSelected,
		PhotoDeselected,
		PhotoAdded,
		PhotoRemoved,
		TagSelectionChanged,
		AlbumSaved,
		AlbumLoaded
	}
	
	public PhotoApplicationModel model;
	
	public Type type;
	public PhotoApplicationModel.ViewMode oldViewMode;
	public PhotoApplicationModel.ViewMode newViewMode;
	public int oldIndex;
	public int newIndex;
	public boolean flipped;
	public AnnotatedPhoto photo;
	
	public PhotoEvent(PhotoApplicationModel model, Type type) {
		this.model = model;
		this.type = type;
	}
	
	public static PhotoEvent CreateViewModeChangedEvent(
			PhotoApplicationModel model, 
			PhotoApplicationModel.ViewMode oldViewMode, 
			PhotoApplicationModel.ViewMode newViewMode) {
		PhotoEvent e = new PhotoEvent(model, Type.ViewModeChanged);
		e.oldViewMode = oldViewMode;
		e.newViewMode = newViewMode;
		return e;
	}
	public static PhotoEvent CreateViewIndexChangedEvent(
			PhotoApplicationModel model, 
			int oldIndex, 
			int newIndex) {
		PhotoEvent e = new PhotoEvent(model, Type.ViewIndexChanged);
		e.oldIndex = oldIndex;
		e.newIndex = newIndex;
		if (newIndex != -1) {
			e.photo = model.getAnnotatedPhoto(newIndex);
		}
		return e;
	}
	public static PhotoEvent CreatePhotoFlippedEvent(PhotoApplicationModel model, boolean flipped) {
		PhotoEvent e = new PhotoEvent(model, Type.PhotoFlipped);
		e.flipped = flipped;
		e.photo = model.getAnnotatedPhoto();
		return e;
	}
	public static PhotoEvent CreatePhotoAnnotationChangedEvent(PhotoApplicationModel model, AnnotatedPhoto photo) {
		PhotoEvent e = new PhotoEvent(model, Type.PhotoAnnotationChanged);
		e.photo = photo;
		return e;
	}
	public static PhotoEvent CreatePhotoSelectedEvent(PhotoApplicationModel model, AnnotatedPhoto photo) {
		PhotoEvent e = new PhotoEvent(model, Type.PhotoSelected);
		e.photo = photo;
		return e;
	}
	public static PhotoEvent CreatePhotoDeselectedEvent(PhotoApplicationModel model, AnnotatedPhoto photo) {
		PhotoEvent e = new PhotoEvent(model, Type.PhotoDeselected);
		e.photo = photo;
		return e;
	}
	public static PhotoEvent CreatePhotoAddedEvent(PhotoApplicationModel model, AnnotatedPhoto photo) {
		PhotoEvent e = new PhotoEvent(model, Type.PhotoAdded);
		e.photo = photo;
		return e;
	}
	public static PhotoEvent CreatePhotoRemovedEvent(PhotoApplicationModel model, AnnotatedPhoto photo) {
		PhotoEvent e = new PhotoEvent(model, Type.PhotoRemoved);
		e.photo = photo;
		return e;
	}
	public static PhotoEvent CreateTagSelectionChangedEvent(PhotoApplicationModel model) {
		PhotoEvent e = new PhotoEvent(model, Type.TagSelectionChanged);
		return e;
	}
	public static PhotoEvent CreateAlbumSavedEvent(PhotoApplicationModel model) {
		return new PhotoEvent(model, Type.AlbumSaved);
	}
	public static PhotoEvent CreateAlbumLoadedEvent(PhotoApplicationModel model) {
		return new PhotoEvent(model, Type.AlbumLoaded);
	}
}
