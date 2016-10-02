import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.swing.JComponent;

public class PhotoIcon extends JComponent {

	private static final long serialVersionUID = 1L;
	
	public final int thumbnailSize = GlobalSettings.thumbnailSize;
	public final int frameWidth = 20;
	
	public AnnotatedPhoto photo;
	
	private Image frame;
	
	public PhotoIcon(AnnotatedPhoto photo) {
		this.photo = photo;
		setPreferredSize(new Dimension(
				thumbnailSize + 2 * frameWidth, 
				thumbnailSize + 2 * frameWidth));
		
		frame = ResourceManager.frameImage;
	}
	
	
	@Override
	public void paintComponent(Graphics graphics) {
		Graphics2D g = (Graphics2D)graphics;
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		
		paintPhotoAndFrame(graphics);
	}
	
	private void paintPhotoAndFrame(Graphics graphics) {
		Dimension size = getSize();
		int midx = size.width / 2;
		int midy = size.height / 2;
		int[] gridx = {
				midx - thumbnailSize / 2 - frameWidth, 
				midx - thumbnailSize / 2, 
				midx + (thumbnailSize + 1) / 2, 
				midx + (thumbnailSize + 1) / 2 + frameWidth};
		int[] gridy = {
				midy - thumbnailSize / 2 - frameWidth, 
				midy - thumbnailSize / 2, 
				midy + (thumbnailSize + 1) / 2, 
				midy + (thumbnailSize + 1) / 2 + frameWidth};
		int[] fx = new int[] {0, frameWidth, 2 * frameWidth, 3 * frameWidth};
		int[] fy = new int[] {0, frameWidth, 2 * frameWidth, 3 * frameWidth};
		
		graphics.drawImage(photo.thumbnail, gridx[1], gridy[1], gridx[2] - gridx[1], gridy[2] - gridy[1], null);
		
		graphics.drawImage(frame, gridx[0], gridy[0], gridx[1], gridy[1], fx[0], fy[0], fx[1], fy[1], null);
		graphics.drawImage(frame, gridx[1], gridy[0], gridx[2], gridy[1], fx[1], fy[0], fx[2], fy[1], null);
		graphics.drawImage(frame, gridx[2], gridy[0], gridx[3], gridy[1], fx[2], fy[0], fx[3], fy[1], null);
		graphics.drawImage(frame, gridx[2], gridy[1], gridx[3], gridy[2], fx[2], fy[1], fx[3], fy[2], null);
		graphics.drawImage(frame, gridx[2], gridy[2], gridx[3], gridy[3], fx[2], fy[2], fx[3], fy[3], null);
		graphics.drawImage(frame, gridx[1], gridy[2], gridx[2], gridy[3], fx[1], fy[2], fx[2], fy[3], null);
		graphics.drawImage(frame, gridx[0], gridy[2], gridx[1], gridy[3], fx[0], fy[2], fx[1], fy[3], null);
		graphics.drawImage(frame, gridx[0], gridy[1], gridx[1], gridy[2], fx[0], fy[1], fx[1], fy[2], null);
	}

}
