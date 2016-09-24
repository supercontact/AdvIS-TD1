import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

public class PhotoComponent extends JComponent {

	private static final long serialVersionUID = 1L;
	
	private static int frameWidth = 20;
	
	private PhotoBrowserModel model;
	private Image background;
	private Image frame;

	public PhotoComponent() {
		model = new PhotoBrowserModel();
		try {
			background = ImageIO.read(FileSystems.getDefault().getPath("data", "resources", "bg.jpg").toFile());
			frame = ImageIO.read(FileSystems.getDefault().getPath("data", "resources", "frame.png").toFile());
		} catch (IOException e) {
			System.out.println("Background loading error!");
		}
	}
	
	public void addPhotos(File[] url) {
		model.addPhotos(url);
		Image img = model.getAnnotatedPhoto().image;
		int imgW = img.getWidth(null);
		int imgH = img.getHeight(null);
		setMinimumSize(new Dimension(imgW + 2 * frameWidth, imgH + 2 * frameWidth));
		setPreferredSize(new Dimension(imgW + 2 * frameWidth, imgH + 2 * frameWidth));
		
		revalidate();
		repaint();
	}

	@Override
	public void paintComponent(Graphics graphics) {	
		super.paintComponent(graphics);
		
		paintBackground(graphics);
		paintPhoto(graphics);	
	}
	
	private void paintBackground(Graphics graphics) {
		// The background stays fixed when scrolling
		int w = getParent().getWidth();
		int h = getParent().getHeight();
		int dx = -getX();
		int dy = -getY();
		int imgW = background.getWidth(null);
		int imgH = background.getHeight(null);
		if (w * imgH > h * imgW) {
			// Width is too large
			int imgH2 = h * imgW / w;
			graphics.drawImage(background, dx, dy, dx + w, dy + h, 0, (imgH - imgH2) / 2, imgW, (imgH + imgH2) / 2, null);
		} else {
			// Height is too large
			int imgW2 = w * imgH / h;
			graphics.drawImage(background, dx, dy, dx + w, dy + h, (imgW - imgW2) / 2, 0, (imgW + imgW2) / 2, imgH, null);
		}
	}
	
	private void paintPhoto(Graphics graphics) {
		if (model.mode == PhotoBrowserModel.ViewMode.PhotoViewer && model.getPhotoCount() > 0) {
			// Draw photo
			int midx = getWidth() / 2;
			int midy = getHeight() / 2;
			Image img = model.getAnnotatedPhoto().image;
			int imgW = img.getWidth(null);
			int imgH = img.getHeight(null);
			int minx = midx - imgW / 2;
			int maxx = midx + imgW / 2;
			int miny = midy - imgH / 2;
			int maxy = midy + imgH / 2;
			graphics.drawImage(img, minx, miny, imgW, imgH, null);
			
			// Draw frame (9-sliced)
			int[] fx = new int[] {0, frameWidth, 2 * frameWidth, 3 * frameWidth};
			int[] fy = fx;
			int[] ix = new int[] {minx - frameWidth, minx, maxx, maxx + frameWidth};
			int[] iy = new int[] {miny - frameWidth, miny, maxy, maxy + frameWidth};
			graphics.drawImage(frame, ix[0], iy[0], ix[1], iy[1], fx[0], fy[0], fx[1], fy[1], null);
			graphics.drawImage(frame, ix[1], iy[0], ix[2], iy[1], fx[1], fy[0], fx[2], fy[1], null);
			graphics.drawImage(frame, ix[2], iy[0], ix[3], iy[1], fx[2], fy[0], fx[3], fy[1], null);
			graphics.drawImage(frame, ix[2], iy[1], ix[3], iy[2], fx[2], fy[1], fx[3], fy[2], null);
			graphics.drawImage(frame, ix[2], iy[2], ix[3], iy[3], fx[2], fy[2], fx[3], fy[3], null);
			graphics.drawImage(frame, ix[1], iy[2], ix[2], iy[3], fx[1], fy[2], fx[2], fy[3], null);
			graphics.drawImage(frame, ix[0], iy[2], ix[1], iy[3], fx[0], fy[2], fx[1], fy[3], null);
			graphics.drawImage(frame, ix[0], iy[1], ix[1], iy[2], fx[0], fy[1], fx[1], fy[2], null);
		}
	}
}
