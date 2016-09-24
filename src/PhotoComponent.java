import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Path2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.Timer;

public class PhotoComponent extends JComponent implements MouseListener, MouseMotionListener, MouseWheelListener, ActionListener {

	private static final long serialVersionUID = 1L;
	
	public int frameWidth = 20;
	public double imageScaleX = 1;
	public double imageScaleY = 1;
	public int imageScaleMinimumLevel = -10;
	public int imageScaleMaximumLevel = 10;
	public double scaleFactorPerLevel = 1.2;
	public int fps = 60;
	public long flippingAnimationTime = 400;
	
	private PhotoBrowserModel model;
	private Image background;
	private Image frame;
	private Timer timer;
	
	private Point prevMousePos;
	private int scaleLevel = 0;
	private boolean isLocked = false;
	private boolean isFlippingToBack = false;
	private boolean isFlippingToFront = false;
	private long flippingAnimationProgress = 0;
	private double imageScaleXBeforeFlipping = 1;
	private AnnotatedPhoto.StrokeMark currentStroke;
	private boolean canStartStroke = false;
	

	public PhotoComponent() {
		model = new PhotoBrowserModel();
		try {
			background = ImageIO.read(FileSystems.getDefault().getPath("data", "resources", "bg.jpg").toFile());
			frame = ImageIO.read(FileSystems.getDefault().getPath("data", "resources", "frame.png").toFile());
		} catch (IOException e) {
			System.out.println("Resources loading error!");
		}
		
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		timer = new Timer(1000 / fps, this);
		timer.setInitialDelay(10);
		timer.start(); 
	}
	
	public void addPhotos(File[] url) {
		model.addPhotos(url);
		
		imageScaleX = 1;
		imageScaleY = 1;
		
		updateComponentSize();
		revalidate();
		repaint();
	}
	
	public void updateComponentSize() {
		Image img = model.getAnnotatedPhoto().image;
		int imgW = img.getWidth(null);
		int imgH = img.getHeight(null);
		
		int w = (int)((imgW + 2 * frameWidth) * imageScaleX);
		int h = (int)((imgH + 2 * frameWidth) * imageScaleY);
		setMinimumSize(new Dimension(w, h));
		setPreferredSize(new Dimension(w, h));
	}

	@Override
	public void paintComponent(Graphics graphics) {	
		super.paintComponent(graphics);
		
		paintBackground(graphics);
		if (model.mode == PhotoBrowserModel.ViewMode.PhotoViewer && model.currentViewingIndex >= 0) {
			if (!model.flipped) {
				paintPhoto(graphics);
			} else {
				paintPhotoBack(graphics);
				paintStrokes(graphics);
			}
			paintPhotoFrame(graphics);
		}
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
		Graphics2D g = (Graphics2D)graphics;
		
		Rectangle rect = calculateImageRect();
		Image img = model.getAnnotatedPhoto().image;
		
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(img, rect.x, rect.y, rect.width, rect.height, null);
	}
	
	private void paintPhotoBack(Graphics graphics) {
		// White for now
		Rectangle rect = calculateImageRect();
		
		graphics.setColor(Color.WHITE);
		graphics.fillRect(rect.x, rect.y, rect.width, rect.height);
	}
	
	private void paintPhotoFrame(Graphics graphics) {
		Rectangle rect = calculateImageRect();
		int frameWidthX = (int)(frameWidth * imageScaleX);
		int frameWidthY = (int)(frameWidth * imageScaleY);
		
		// 9-sliced Image
		int[] fx = new int[] {0, frameWidth, 2 * frameWidth, 3 * frameWidth};
		int[] fy = new int[] {0, frameWidth, 2 * frameWidth, 3 * frameWidth};
		int[] ix = new int[] {rect.x - frameWidthX, rect.x, rect.x + rect.width, rect.x + rect.width + frameWidthX};
		int[] iy = new int[] {rect.y - frameWidthY, rect.y, rect.y + rect.height, rect.y + rect.height + frameWidthY};
		
		graphics.drawImage(frame, ix[0], iy[0], ix[1], iy[1], fx[0], fy[0], fx[1], fy[1], null);
		graphics.drawImage(frame, ix[1], iy[0], ix[2], iy[1], fx[1], fy[0], fx[2], fy[1], null);
		graphics.drawImage(frame, ix[2], iy[0], ix[3], iy[1], fx[2], fy[0], fx[3], fy[1], null);
		graphics.drawImage(frame, ix[2], iy[1], ix[3], iy[2], fx[2], fy[1], fx[3], fy[2], null);
		graphics.drawImage(frame, ix[2], iy[2], ix[3], iy[3], fx[2], fy[2], fx[3], fy[3], null);
		graphics.drawImage(frame, ix[1], iy[2], ix[2], iy[3], fx[1], fy[2], fx[2], fy[3], null);
		graphics.drawImage(frame, ix[0], iy[2], ix[1], iy[3], fx[0], fy[2], fx[1], fy[3], null);
		graphics.drawImage(frame, ix[0], iy[1], ix[1], iy[2], fx[0], fy[1], fx[1], fy[2], null);
	}
	
	private void paintStrokes(Graphics graphics) {
		Graphics2D g = (Graphics2D)graphics;
	    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    
		List<AnnotatedPhoto.StrokeMark> strokes = model.getAnnotatedPhoto().strokes;
		if (model.flipped) {
			for (AnnotatedPhoto.StrokeMark stroke : strokes) {
				Stroke strokeType = new BasicStroke((float)(stroke.width * imageScaleY));
				g.setStroke(strokeType);
				g.setColor(stroke.color);
				g.setClip(calculateImageRect());
				
				Path2D line = new Path2D.Double();
				Point p0 = toComponentCoordinates(stroke.path.get(0));
				line.moveTo(p0.x, p0.y);
				for (int i = 1; i < stroke.path.size(); i++) {
					Point p = toComponentCoordinates(stroke.path.get(i));
					line.lineTo(p.x, p.y);
				}
				g.draw(line);
				g.setClip(null);
			}
		}
	}
	
	public void flipPhoto() {
		if (!isLocked) {
			imageScaleXBeforeFlipping = imageScaleY;
			if (!model.flipped) {
				isFlippingToBack = true;
			} else {
				isFlippingToFront = true;
			}
			isLocked = true;
		}
	}
	
	private Rectangle calculateImageRect() {
		if (model.currentViewingIndex < 0) {
			return new Rectangle(0, 0, 0, 0);
		}
		Image img = model.getAnnotatedPhoto().image;
		int midx = getWidth() / 2;
		int midy = getHeight() / 2;
		int imgW = (int)(img.getWidth(null) * imageScaleX);
		int imgH = (int)(img.getHeight(null) * imageScaleY);
		return new Rectangle(midx - imgW / 2, midy - imgH / 2, imgW, imgH);
	}
	
	private Point toImageCoordinates(Point p) {
		Rectangle rect = calculateImageRect();
		return new Point((int)((p.x - rect.x) / imageScaleX), (int)((p.y - rect.y) / imageScaleY));
	}
	private Point toComponentCoordinates(Point p) {
		Rectangle rect = calculateImageRect();
		return new Point((int)(p.x * imageScaleX + rect.x), (int)(p.y * imageScaleY + rect.y));
	}
	

	// MouseListener & MouseMotionListener & MouseWheelListener: Mouse events
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() % 2 == 0 && model.mode == PhotoBrowserModel.ViewMode.PhotoViewer && model.currentViewingIndex >= 0) {
			flipPhoto();
		}
	}
	@Override
	public void mousePressed(MouseEvent e) {
		prevMousePos = e.getPoint();
		if (calculateImageRect().contains(prevMousePos)) {
			canStartStroke = true;
		}
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		currentStroke = null;
		canStartStroke = false;
	}
	@Override
	public void mouseDragged(MouseEvent e) {
		if (model.mode == PhotoBrowserModel.ViewMode.PhotoViewer && model.flipped) {
			if (canStartStroke) {
				if (currentStroke == null) {
					currentStroke = new AnnotatedPhoto.StrokeMark();
					currentStroke.width = 5; // Will let the user choose in the future
					currentStroke.color = Color.black; // Will let the user choose in the future
					currentStroke.path.add(toImageCoordinates(prevMousePos));
					model.getAnnotatedPhoto().strokes.add(currentStroke);
				}
				currentStroke.path.add(toImageCoordinates(e.getPoint()));
				repaint();
				prevMousePos = e.getPoint();
			}
		}
	}
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		scaleLevel -= e.getWheelRotation();
		scaleLevel = Math.max(scaleLevel, imageScaleMinimumLevel);
		scaleLevel = Math.min(scaleLevel, imageScaleMaximumLevel);
		
		imageScaleX = Math.pow(scaleFactorPerLevel, scaleLevel);
		imageScaleY = Math.pow(scaleFactorPerLevel, scaleLevel);
		
		updateComponentSize();
		revalidate();
		repaint();
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		// Do nothing
	}
	@Override
	public void mouseExited(MouseEvent e) {
		// Do nothing
	}
	@Override
	public void mouseMoved(MouseEvent e) {
		// Do nothing
	}
	
	
	// ActionLister: Execute in loop
	@Override
	public void actionPerformed(ActionEvent e) {
		if (isFlippingToBack || isFlippingToFront) {
			if (flippingAnimationProgress < flippingAnimationTime) {
				flippingAnimationProgress += 1000 / fps;
				flippingAnimationProgress = Math.min(flippingAnimationProgress, flippingAnimationTime);
			}
			model.flipped = (2 * flippingAnimationProgress < flippingAnimationTime) ? isFlippingToFront : isFlippingToBack;
			imageScaleX = imageScaleXBeforeFlipping * Math.abs(Math.cos(Math.PI * flippingAnimationProgress / flippingAnimationTime));
			repaint();
			if (flippingAnimationProgress == flippingAnimationTime) {
				flippingAnimationProgress = 0;
				isFlippingToBack = false;
				isFlippingToFront = false;
				isLocked = false;
				imageScaleX = imageScaleXBeforeFlipping;
			}
		}
	}
}
