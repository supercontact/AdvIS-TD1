package scene;

import java.awt.Image;
import java.awt.Rectangle;

public class ImageNode extends Node {
	
	public Image image;
	
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
