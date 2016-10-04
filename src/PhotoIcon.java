import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import javax.swing.JComponent;

public class PhotoIcon extends JComponent {

	private static final long serialVersionUID = 1L;
	
	public final int thumbnailSize = GlobalSettings.thumbnailSize;
	public final int frameWidth = 20;
	public final double margin = 0.05;
	public final double rolloverScale = 1.025;
	public final double rolloverRotate = 0.03;
	public final double pressedScale = 1.05;
	
	public AnnotatedPhoto photo;
	public PhotoContainer container;
	
	private boolean isRollover = false;
	private boolean isPressed = false;
	private boolean isSelected = false;
	private double imageScaleX = 1;
	private double imageScaleY = 1;
	
	
	private Image frame;
	
	
	public PhotoIcon(AnnotatedPhoto photo) {
		this.photo = photo;
		if (photo.thumbnail == null) {
			photo.thumbnail = ResourceManager.errorImageThumbnail;
		}
		setPreferredSize(new Dimension(
				(int)((thumbnailSize + 2 * frameWidth) * (1 + margin)), 
				(int)((thumbnailSize + 2 * frameWidth) * (1 + margin))));
		
		frame = ResourceManager.frameImage;
	}
	
	public boolean isRollover() {
		return isRollover;
	}
	public boolean isPressed() {
		return isPressed;
	}
	public boolean isSelected() {
		return isSelected;
	}
	public void setRollover(boolean b) {
		isRollover = b;
		repaint();
	}
	public void setPressed(boolean b) {
		isPressed = b;
		repaint();
	}
	public void setSelected(boolean b) {
		isSelected = b;
		repaint();
	}

	@Override
	public void paintComponent(Graphics graphics) {
		Graphics2D g = (Graphics2D)graphics;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		
		paintSelection(graphics);
        paintPhotoAndFrame(graphics);
	}
	
	private void paintSelection(Graphics graphics) {
		Color color = new Color(0, 0, 0, 0);
		if (isSelected) {
			color = new Color(120, 120, 200, 128);
		}
		/*if (isPressed) {
			color = new Color(120, 120, 200, 192);
		} else if (isSelected && isRollover) {
			color = new Color(120, 120, 200, 160);
		} else if (isSelected) {
			color = new Color(120, 120, 200, 128);
		} else if (isRollover) {
			color = new Color(120, 120, 200, 64);
		}*/
		
		graphics.setColor(color);
		graphics.fillRect(0, 0, getWidth(), getHeight());
	}
	
	private void paintPhotoAndFrame(Graphics graphics) {
		Graphics2D g = (Graphics2D) graphics;
		if (isPressed) {
			imageScaleX = pressedScale;
			imageScaleY = pressedScale;
		} else if (isRollover) {
			imageScaleX = rolloverScale;
			imageScaleY = rolloverScale;
		} else {
			imageScaleX = 1;
			imageScaleY = 1;
		}
		
		Dimension size = getSize();
		int midx = size.width / 2;
		int midy = size.height / 2;
		
		AffineTransform oldTrans = null;
		if (isRollover) {
			oldTrans = g.getTransform();
			AffineTransform newTrans = AffineTransform.getRotateInstance(rolloverRotate, midx, midy);
			newTrans.concatenate(oldTrans);
			g.setTransform(newTrans);
		}
		int[] gridx = {
				midx - (int)((thumbnailSize / 2 + frameWidth) * imageScaleX), 
				midx - (int)(thumbnailSize / 2 * imageScaleX), 
				midx + (int)((thumbnailSize + 1) / 2 * imageScaleX), 
				midx + (int)(((thumbnailSize + 1) / 2 + frameWidth) * imageScaleX)};
		int[] gridy = {
				midy - (int)((thumbnailSize / 2 + frameWidth) * imageScaleY), 
				midy - (int)(thumbnailSize / 2 * imageScaleY), 
				midy + (int)((thumbnailSize + 1) / 2 * imageScaleY), 
				midy + (int)(((thumbnailSize + 1) / 2 + frameWidth) * imageScaleY)};
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
		
		if (isRollover) {
			g.setTransform(oldTrans);
		}
	}
}
