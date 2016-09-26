import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Path2D;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.Timer;

public class PhotoComponent extends JComponent implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, ActionListener {

	private static final long serialVersionUID = 1L;
	
	public int frameWidth = 20;
	public double imageScaleX = 1;
	public double imageScaleY = 1;
	public int imageScaleMinimumLevel = -20;
	public int imageScaleMaximumLevel = 20;
	public double scaleFactorPerLevel = 1.1;
	public double lineSpacing = 1.2;
	public int fps = 60;
	public long flippingAnimationTime = 400;
	public long savePeriod = 5000;
	public File backgroundImageLocation = GlobalSettings.backgroundImageLocation;
	public File frameImageLocation = GlobalSettings.frameImageLocation;
	public File errorImageLocation = GlobalSettings.errorImageLocation;
	
	private PhotoBrowserModel model;
	private Image background;
	private Image frame;
	private Image errorImage;
	private Timer timer;
	
	private Point prevMousePos;
	private int scaleLevel = 0;
	private boolean isLocked = false;
	private boolean isFlippingToBack = false;
	private boolean isFlippingToFront = false;
	private boolean isEditingText = false;
	private long flippingAnimationProgress = 0;
	private double imageScaleXBeforeFlipping = 1;
	private AnnotatedPhoto.StrokeMark currentStroke;
	private AnnotatedPhoto.Annotation currentAnnotation;
	private int annotationEditingPos = 0;
	private Point annotationEditingPoint;
	private boolean canStartStroke = false;
	private boolean changed = false;
	private long lastSaveTime = 0;
	

	public PhotoComponent() {
		model = new PhotoBrowserModel();
		model.loadAlbum();
		try {
			background = ImageIO.read(backgroundImageLocation);
			frame = ImageIO.read(frameImageLocation);
			errorImage = ImageIO.read(errorImageLocation);
		} catch (IOException e) {
			System.out.println("Resources loading error!");
		}
		
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);
		setFocusable(true);
		timer = new Timer(1000 / fps, this);
		timer.setInitialDelay(10);
		timer.start(); 
		
		reinit();
	}
	
	public boolean saveAlbum() {
		changed = false;
		return model.saveAlbum();
	}
	
	public void addPhotos(File[] url) {
		model.addPhotos(url);
		saveAlbum();
		requestFocusInWindow();
		reinit();
	}
	
	public boolean deleteCurrentPhoto() {
		if (model.deletePhoto()) {
			saveAlbum();
			reinit();
			return true;
		}
		return false;
	}
	
	public boolean clearCurrentPhoto() {
		if (model.clearPhoto()) {
			model.saveAlbum();
			repaint();
			return true;
		}
		return false;
	}
	
	public void reinit() {
		imageScaleX = 1;
		imageScaleY = 1;
		scaleLevel = 0;
		model.flipped = false;
		isEditingText = false;
		currentAnnotation = null;
		if (model.getAnnotatedPhoto().image == null) {
			model.getAnnotatedPhoto().image = errorImage;
		}
		updateComponentSize();
		revalidate();
		repaint();
	}
	
	public void updateComponentSize() {
		int w, h;
		if (model.currentViewingIndex >= 0) {
			Image img = model.getAnnotatedPhoto().image;
			int imgW = img.getWidth(null);
			int imgH = img.getHeight(null);
			w = (int)((imgW + 2 * frameWidth) * imageScaleX);
			h = (int)((imgH + 2 * frameWidth) * imageScaleY);
		} else {
			w = 0;
			h = 0;
		}
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
				if (!model.getAnnotatedPhoto().imageLoaded) {
					paintPhotoPath(graphics);
				}
			} else {
				paintPhotoBack(graphics);
				paintStrokes(graphics);
				paintAnnotations(graphics);
				if (isEditingText) {
					paintEditingMark(graphics);
				}
			}
			paintPhotoFrame(graphics);
		}
	}
	
	private void paintBackground(Graphics graphics) {
		// The background stays fixed when scrolling
		int w = getParent().getParent().getWidth();
		int h = getParent().getParent().getHeight();
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
	
	private void paintAnnotations(Graphics graphics) {
		Graphics2D g = (Graphics2D)graphics;
		Rectangle rect = calculateImageRect();
		
		List<AnnotatedPhoto.Annotation> annotations = model.getAnnotatedPhoto().annotations;
		if (model.flipped) {
			for (AnnotatedPhoto.Annotation annotation : annotations) {
				Font font = new Font("SansSerif", Font.PLAIN, (int)(annotation.size * imageScaleY));
				g.setFont(font);
				g.setColor(annotation.color);
				g.setClip(calculateImageRect());
				FontMetrics metrics = g.getFontMetrics();
				
				Point pos = toComponentCoordinates(annotation.position);
				String str = annotation.text;
				int startPosInAnnotation = 0;
				
				while (str.length() > 0) {
					int length = trimString(str, rect.x + rect.width - pos.x, metrics);
					if (str.charAt(0) != '\n' && length == 0) break; // Too close to the right border!
					
					// Get rid of all the excessive space if it is not following \n
					if (str.charAt(length - 1) != '\n') {
						while (length < str.length() && str.charAt(length) == ' ') {
							length++;
						}
					}
					
					g.drawString(str.substring(0, length), pos.x, pos.y);
					
					// Calculate the editing mark's position on the image
					if (isEditingText && annotation == currentAnnotation && annotationEditingPos >= startPosInAnnotation) {
						if (annotationEditingPos == startPosInAnnotation + length && length == str.length() && str.charAt(str.length() - 1) == '\n') {
							annotationEditingPoint = toImageCoordinates(new Point(pos.x, pos.y + (int)(annotation.size * imageScaleY * lineSpacing)));
						} else if (annotationEditingPos < startPosInAnnotation + length || length == str.length()) {
							String sub = str.substring(0, annotationEditingPos - startPosInAnnotation);
							annotationEditingPoint = toImageCoordinates(new Point(pos.x + metrics.stringWidth(sub), pos.y));
						}
					}
					
					startPosInAnnotation += length;
					str = str.substring(length);
					pos.y += (int)(annotation.size * imageScaleY * lineSpacing);
				}
				
				g.setClip(null);
			}
		}
	}
	
	private void paintEditingMark(Graphics graphics) {
		if (annotationEditingPoint != null) {
			Graphics2D g = (Graphics2D)graphics;
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(Color.BLACK);
			g.setClip(calculateImageRect());
	
			Point[] triangle = new Point[] {
					new Point(annotationEditingPoint.x, annotationEditingPoint.y),
					new Point(annotationEditingPoint.x - 5, annotationEditingPoint.y + 7),
					new Point(annotationEditingPoint.x + 5, annotationEditingPoint.y + 7)
			};
			int[] triangleXs = new int[3], triangleYs = new int[3];
			for (int i = 0; i < 3; i++) {
				Point p = toComponentCoordinates(triangle[i]);
				triangleXs[i] = p.x;
				triangleYs[i] = p.y;
			}
			g.fillPolygon(triangleXs, triangleYs, 3);
			g.setClip(null);
		}
	}
	
	private void paintPhotoPath(Graphics graphics) {
		Graphics2D g = (Graphics2D)graphics;
		Rectangle rect = calculateImageRect();
		
		Font font = new Font("SansSerif", Font.PLAIN, 15);
		g.setFont(font);
		g.setColor(Color.black);
		g.setClip(calculateImageRect());
		g.drawString(model.getAnnotatedPhoto().imageURL.toString(), rect.x + 10, rect.y + rect.height - 10);
		g.setClip(null);
	}
	
	// Find the best prefix of the string that fits in the given space. End with a space if possible.
	private int trimString(String str, int spaceInPixel, FontMetrics metrics) {
		int cutIndex = str.indexOf('\n');
		if (cutIndex >= 0) {
			str = str.substring(0, cutIndex + 1);
		}
		while (metrics.stringWidth(str) > spaceInPixel) {
			cutIndex = str.lastIndexOf(' ');
			if (cutIndex == -1) {
				cutIndex = str.length() - 1;
			}
			str = str.substring(0, cutIndex);
		}
		return str.length();
	}
	
	public void flipPhoto() {
		if (!isLocked) {
			isEditingText = false;
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
		requestFocusInWindow();
		if (model.mode == PhotoBrowserModel.ViewMode.PhotoViewer && model.currentViewingIndex >= 0) {
			if (e.getClickCount() == 1 && model.flipped && !isLocked) {
				Point imagePoint = toImageCoordinates(e.getPoint());
				isEditingText = true;
				AnnotatedPhoto.Annotation annotation = new AnnotatedPhoto.Annotation();
				annotation.color = Color.black; // Will change
				annotation.position = imagePoint;
				annotation.size = 20; // Will change
				annotation.text = "";
				currentAnnotation = annotation;
				annotationEditingPos = 0;
				annotationEditingPoint = imagePoint;
				repaint();
			} else if (e.getClickCount() % 2 == 0) {
				flipPhoto();
			}
		}
	}
	@Override
	public void mousePressed(MouseEvent e) {
		prevMousePos = e.getPoint();
		if (calculateImageRect().contains(prevMousePos) && model.flipped) {
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
		if (model.mode == PhotoBrowserModel.ViewMode.PhotoViewer) {
			if (canStartStroke && model.flipped) {
				// Drawing stroke
				if (currentStroke == null) {
					currentStroke = new AnnotatedPhoto.StrokeMark();
					currentStroke.width = 5; // Will let the user choose in the future
					currentStroke.color = Color.black; // Will let the user choose in the future
					currentStroke.path.add(toImageCoordinates(prevMousePos));
					model.getAnnotatedPhoto().strokes.add(currentStroke);
					isEditingText = false;
				}
				currentStroke.path.add(toImageCoordinates(e.getPoint()));
				changed = true;
				repaint();
				prevMousePos = e.getPoint();
			} else {
				// Scrolling the photo
				Rectangle visible = getVisibleRect();
				visible.x -= e.getPoint().x - prevMousePos.x;
				visible.y -= e.getPoint().y - prevMousePos.y;
				scrollRectToVisible(visible);
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
	
	
	
	// KeyListener: Keyboard events
	@Override
	public void keyTyped(KeyEvent e) {
		if (isEditingText && !e.isControlDown()) {
			char c = e.getKeyChar();
			if ((int)c == KeyEvent.VK_BACK_SPACE) {
				if (annotationEditingPos > 0) {
					currentAnnotation.text = currentAnnotation.text.substring(0, annotationEditingPos - 1)
							+  currentAnnotation.text.substring(annotationEditingPos, currentAnnotation.text.length());
					annotationEditingPos--;
				}
				if (currentAnnotation.text.length() == 0) {
					model.getAnnotatedPhoto().annotations.remove(currentAnnotation);
					annotationEditingPoint = currentAnnotation.position;
				}
				changed = true;
				repaint();
			} else if (c != KeyEvent.CHAR_UNDEFINED) {
				if (currentAnnotation.text.length() == 0) {
					model.getAnnotatedPhoto().annotations.add(currentAnnotation);
				}
				if (annotationEditingPos == currentAnnotation.text.length()) {
					currentAnnotation.text += e.getKeyChar();
				} else {
					currentAnnotation.text = currentAnnotation.text.substring(0, annotationEditingPos) + e.getKeyChar() + 
							currentAnnotation.text.substring(annotationEditingPos, currentAnnotation.text.length());
				}
				annotationEditingPos++;
				changed = true;
				repaint();
			}
		}
	}
	@Override
	public void keyPressed(KeyEvent e) {
		if (!isEditingText) {
			// Jump to another photo in the album
			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				model.nextPhoto();
				reinit();
			} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				model.prevPhoto();
				reinit();
			}
		} else {
			// Navigate the pointer in the current editing annotation
			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				annotationEditingPos = Math.min(annotationEditingPos + 1, currentAnnotation.text.length());
				repaint();
			} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				annotationEditingPos = Math.max(annotationEditingPos - 1, 0);
				repaint();
			}
		}
		e.consume();
	}
	@Override
	public void keyReleased(KeyEvent e) {
		// Do nothing
	}
	
	
	
	// ActionLister: Animation control, execute in loop.
	@Override
	public void actionPerformed(ActionEvent e) {
		
		// Periodic save
		long t = System.currentTimeMillis();
		if (t - lastSaveTime > savePeriod) {
			lastSaveTime = t;
			if (changed) {
				saveAlbum();
			}
		}
		
		// Flipping animation
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
