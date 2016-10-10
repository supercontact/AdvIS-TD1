package scene;

import java.awt.Image;
import java.awt.Rectangle;

// This node displays an image.
public class ImageNode extends Node {
	
	private static final long serialVersionUID = 1L;
	
	// Need modification to make it serializable
	public transient Image image;
	
	public ImageNode() {}
	public ImageNode(Image image) {
		this.image = image;
	}
	
	@Override
	public Rectangle getContentBounds() {
		return new Rectangle(0, 0, image.getWidth(null), image.getHeight(null));
	}
	
	@Override
	public void paintNode(Context context) {
		context.graphics.drawImage(image, 0, 0, image.getWidth(null), image.getHeight(null), null);
	}
	
}
