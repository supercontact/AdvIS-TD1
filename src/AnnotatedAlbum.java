import java.io.Serializable;
import java.util.ArrayList;

public class AnnotatedAlbum implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public ArrayList<AnnotatedPhoto> photoList;

	public AnnotatedAlbum() {
		photoList = new ArrayList<>();
	}

}
