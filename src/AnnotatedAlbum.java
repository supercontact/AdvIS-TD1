import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class AnnotatedAlbum implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public ArrayList<AnnotatedPhoto> photoList;

	public AnnotatedAlbum() {
		photoList = new ArrayList<>();
	}

	public int getSize() {
		return photoList.size();
	}
	
	public AnnotatedPhoto importNewPhoto(File file) {
		AnnotatedPhoto newPhoto = new AnnotatedPhoto(file);
		newPhoto.setIndex(photoList.size());
		photoList.add(newPhoto);
		return newPhoto;
	}
	
	public void loadAllPhotos() {
		for (AnnotatedPhoto photo : photoList) {
			photo.loadPhoto();
		}
	}
	
	public boolean saveAlbum() {
		return SerializationControl.save(this, GlobalSettings.savedAlbumLocation);
	}

	public static AnnotatedAlbum loadAlbum() {
		if (GlobalSettings.savedAlbumLocation.exists()) {
			AnnotatedAlbum album = (AnnotatedAlbum)SerializationControl.load(GlobalSettings.savedAlbumLocation);
			for (int i = 0; i < album.photoList.size(); i++) {
				album.photoList.get(i).setIndex(i);
			}
			return album;
		}
		return null;
	}
	
}
