import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.File;

import javax.swing.JComponent;

public class PhotoComponent extends JComponent {

	private static final long serialVersionUID = 1L;
	
	private PhotoBrowserModel model;

	public PhotoComponent() {
		model = new PhotoBrowserModel();
	}
	
	public void addPhotos(File[] url) {
		model.addPhotos(url);
		invalidate();
		repaint();
	}

	@Override
	public void paintComponent(Graphics graphics) {	
		super.paintComponent(graphics);
		
		Graphics2D g = (Graphics2D)graphics;
		
		if (model.mode == PhotoBrowserModel.ViewMode.PhotoViewer && model.getPhotoCount() > 0) {
			int midx = getWidth() / 2;
			int midy = getHeight() / 2;
			Image img = model.getAnnotatedPhoto().image;
			int imgW = img.getWidth(null);
			int imgH = img.getHeight(null);
			g.drawImage(img, midx - imgW / 2, midy - imgH / 2, imgW, imgH, null);
		}
	}
}
