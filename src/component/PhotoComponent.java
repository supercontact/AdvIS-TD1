package component;
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
import java.awt.Shape;
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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import custom.ResourceManager;
import model.AnnotatedPhoto;
import model.PhotoApplicationModel;
import model.PhotoEvent;
import model.PhotoListener;

public class PhotoComponent extends JComponent implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, ActionListener, PhotoListener {

	private static final long serialVersionUID = 1L;
	
	public enum ScaleMode {
		OriginalSize,
		FitWindow,
		FitWidth,
		FitHeight
	}
	
	// Constants
	public final int frameWidth = 20;
	public final double imageScaleMinimum = 0.05;
	public final double imageScaleMaximum = 20;
	public final double scaleFactor = 1.1;
	public final double lineSpacing = 1.2;
	public final int fps = 60;
	public final long flippingAnimationTime = 400;
	public final float controlPanelAlphaWhenDrawing = 0.2f;
	public final long savePeriod = 5000;
	
	// States and status
	public ScaleMode defaultScaleMode = ScaleMode.OriginalSize;
	public boolean scaleSmallPhoto = false;
	public double imageScaleX = 1;
	public double imageScaleY = 1;
	public boolean isCreatingPrimitive = false;
	public int currentStrokeWidth = 5;
	public int currentTextSize = 15;
	public AnnotatedPhoto.PrimitiveMark.Type currentPrimitiveType = AnnotatedPhoto.PrimitiveMark.Type.Ellipse;
	public Color currentColor = new Color(0x19, 0x2C, 0x3C);
	public String currentFontName = "Arial";
	
	// Link
	public PhotoContainer container;
	
	// Loaded resources
	private Image frame;
	private Image errorImage;
	
	// Model
	private PhotoApplicationModel model;
	
	// Internal variables
	private boolean initiated = false;
	private Timer timer;
	private Point prevMouseDragPos;
	private boolean isLocked = false;
	private boolean isFlippingToBack = false;
	private boolean isFlippingToFront = false;
	private boolean isEditingText = false;
	private long flippingAnimationProgress = 0;
	private double imageScaleXMultiplier = 1;
	private AnnotatedPhoto.StrokeMark currentStroke;
	private AnnotatedPhoto.PrimitiveMark currentPrimitive;
	private AnnotatedPhoto.Annotation currentAnnotation;
	private int annotationEditingPos = 0;
	private Point annotationEditingPoint;
	private boolean canStartDrawing = false;
	private boolean changed = false;
	private boolean customScaled = false;
	private long lastSaveTime = 0;
	

	public PhotoComponent(PhotoApplicationModel model) {
		this.model = model;
		
		frame = ResourceManager.frameImage;
		errorImage = ResourceManager.errorImage;
		setFocusable(true);
	}
	
	public void init() {
		if (initiated) return;
		
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);
		model.addPhotoListener(this);
		timer = new Timer(1000 / fps, this);
		timer.setInitialDelay(10);
		timer.start(); 
		
		reset();
		initiated = true;
	}
	
	public void deinit() {
		if (!initiated) return;
		
		removeMouseListener(this);
		removeMouseMotionListener(this);
		removeMouseWheelListener(this);
		removeKeyListener(this);
		model.removePhotoListener(this);
		timer.stop();
		
		initiated = false;
	}
	
	public void reset() {
		model.setFlipped(false);
		isEditingText = false;
		currentAnnotation = null;
		if (model.isShowingPhoto() && model.getAnnotatedPhoto().image == null) {
			model.getAnnotatedPhoto().image = errorImage;
		}
		customScaled = false;
		fitPhoto();
		revalidate();
		repaint();
	}
	
	/*public boolean saveAlbum() {
		changed = false;
		return model.saveAlbum();
	}*/
	
	/*public void addPhotos(File[] url) {
		model.addPhotos(url);
		saveAlbum();
		requestFocusInWindow();
		reset();
	}*/
	
	/*public void deleteCurrentPhoto() {
		if (model.isShowingPhoto()) {
			model.deletePhoto();
			saveAlbum();
			reset();
		}
	}*/
	
	/*public void clearCurrentPhoto() {
		if (model.isShowingPhoto()) {
			model.clearPhoto();
			model.saveAlbum();
			repaint();
		}
	}*/
	
	/*public void setPhotoIndex(int index) {
		model.jumpTo(index);
		reset();
		PhotoApplication.showStatusText("Viewing photo " + (getPhotoIndex() + 1) + "/" + getPhotoCount());
	}*/
	/*public int getPhotoIndex() {
		return model.currentViewingIndex;
	}*/
	
	/*public int getPhotoCount() {
		return model.getPhotoCount();
	}*/
	
	/*public void nextPhoto() {
		model.nextPhoto();
		reset();
		PhotoApplication.showStatusText("Viewing photo " + (getPhotoIndex() + 1) + "/" + getPhotoCount());
	}*/
	
	/*public void prevPhoto() {
		model.prevPhoto();
		reset();
		PhotoApplication.showStatusText("Viewing photo " + (getPhotoIndex() + 1) + "/" + getPhotoCount());
	}*/
	
	public void flipPhoto() {
		if (!isLocked) {
			isEditingText = false;
			if (!model.isFlipped()) {
				isFlippingToBack = true;
			} else {
				isFlippingToFront = true;
			}
			isLocked = true;
		}
	}
	
	/*public boolean isFlipped() {
		return model.flipped;
	}*/
	
	public void scalePhoto(int delta, Point pivot) {
		Rectangle rect = calculateImageRectWithFrame();
		Point pos = new Point(pivot.x - rect.x, pivot.y - rect.y);
		
		double oldScale = imageScaleY;
		
		if ((delta > 0 && imageScaleY > imageScaleMinimum) || (delta < 0 && imageScaleY < imageScaleMaximum)) {
			imageScaleY *= Math.pow(scaleFactor, -delta);
			imageScaleX *= Math.pow(scaleFactor, -delta);
		}
		updateComponentSize();
		revalidate();
		
		// Scroll photo such that the pixel pointed by the cursor stays fixed after scaling.
		double factor = imageScaleX / oldScale - 1;
		scrollPhoto((int)(- factor * pos.x), (int)(- factor * pos.y));
		
		customScaled = true;
		repaint();
	}
	
	public void fitPhoto() {
		if (model.getViewMode() != PhotoApplicationModel.ViewMode.PhotoViewer || !model.isShowingPhoto()) return;
		Image img = model.getAnnotatedPhoto().image;
		JScrollPane scrollPane = (JScrollPane)getParent().getParent();
		int requiredWidth = img.getWidth(null) + 2 * frameWidth;
		int requiredHeight = img.getHeight(null) + 2 * frameWidth;
		int epsilon = 10;
		int containerWidth = scrollPane.getWidth() - epsilon;
		int containerHeight = scrollPane.getHeight() - epsilon;
		int scrollbarWidth = scrollPane.getVerticalScrollBar().getPreferredSize().width;
		
		imageScaleX = 1;
		
		if (defaultScaleMode == ScaleMode.FitWidth && (scaleSmallPhoto || requiredWidth > containerWidth)) {
			int realContainerWidth = containerWidth;
			if (requiredHeight * containerWidth > requiredWidth * containerHeight) {
				realContainerWidth -= scrollbarWidth;
			}
			imageScaleX = realContainerWidth / (double) requiredWidth;
		} else if (defaultScaleMode == ScaleMode.FitHeight && (scaleSmallPhoto || requiredHeight > containerHeight)) {
			int realContainerHeight = containerHeight;
			if (requiredWidth * containerHeight > requiredHeight * containerWidth) {
				realContainerHeight -= scrollbarWidth;
			}
			imageScaleX = realContainerHeight / (double) requiredHeight;
		} else if (defaultScaleMode == ScaleMode.FitWindow) {
			double scale = Math.min(containerWidth / (double) requiredWidth, containerHeight / (double) requiredHeight);
			if (scaleSmallPhoto || scale < 1) {
				imageScaleX = scale;
			}
		}
		imageScaleY = imageScaleX;
		updateComponentSize();
	}
	
	public void scrollPhoto(int dx, int dy) {
		Rectangle visible = getVisibleRect();
		visible.x -= dx;
		visible.y -= dy;
		scrollRectToVisible(visible);
	}
	
	public void setScaleMode(ScaleMode mode) {
		defaultScaleMode = mode;
		fitPhoto();
		customScaled = false;
	}
	
	public void drawStrokeTo(Point pos) {
		if (currentStroke == null) {
			currentStroke = model.getAnnotatedPhoto().createStroke();
			currentStroke.color = currentColor;
			currentStroke.width = currentStrokeWidth;
			currentStroke.path.add(componentToImageCoordinates(prevMouseDragPos));
			isEditingText = false;
		}
		currentStroke.path.add(componentToImageCoordinates(pos));
		changed = true;
		repaint();
		prevMouseDragPos = pos;
	}
	
	public void drawPrimitiveTo(Point pos) {
		if (currentPrimitive == null) {
			currentPrimitive = model.getAnnotatedPhoto().createPrimitive();
			currentPrimitive.type = currentPrimitiveType;
			currentPrimitive.color = currentColor;
			currentPrimitive.lineWidth = currentStrokeWidth;
			currentPrimitive.p1 = componentToImageCoordinates(prevMouseDragPos);
			isEditingText = false;
		}
		currentPrimitive.p2 = componentToImageCoordinates(pos);
		changed = true;
		repaint();
		prevMouseDragPos = pos;
	}
	
	public void insertCharacter(char c) {
		if (currentAnnotation == null) {
			AnnotatedPhoto.Annotation annotation = model.getAnnotatedPhoto().createAnnotation();
			annotation.color = currentColor;
			annotation.size = currentTextSize;
			annotation.font = currentFontName;
			annotation.position = annotationEditingPoint;
			currentAnnotation = annotation;
		}
		if (annotationEditingPos == currentAnnotation.text.length()) {
			// Add at the end
			currentAnnotation.text += c;
		} else {
			// Insert in the middle
			currentAnnotation.text = currentAnnotation.text.substring(0, annotationEditingPos) + c + 
					currentAnnotation.text.substring(annotationEditingPos, currentAnnotation.text.length());
		}
		annotationEditingPos++;
		changed = true;
		repaint();
	}
	
	public void deleteCharacter() {
		if (annotationEditingPos > 0) {
			currentAnnotation.text = currentAnnotation.text.substring(0, annotationEditingPos - 1)
					+  currentAnnotation.text.substring(annotationEditingPos, currentAnnotation.text.length());
			annotationEditingPos--;
		}
		if (currentAnnotation.text.length() == 0) {
			model.getAnnotatedPhoto().undo();
			annotationEditingPoint = currentAnnotation.position;
			currentAnnotation = null;
		}
		changed = true;
		repaint();
	}
	
	public void undo() {
		isEditingText = false;
		currentAnnotation = null;
		model.getAnnotatedPhoto().undo();
		changed = true;
		repaint();
		PhotoApplication.showStatusText("Undo last operation.");
	}
	
	
	// All the painting methods
	@Override
	public void paintComponent(Graphics graphics) {	
		if (!customScaled) {
			fitPhoto();
			revalidate();
		}
		imageScaleX = imageScaleY * imageScaleXMultiplier;
		
		Graphics2D g = (Graphics2D)graphics;
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		if (!initiated) return;
		if (model.isShowingPhoto()) {
			Shape oldClip = g.getClip();
			g.setClip(calculateImageRect().intersection((Rectangle)oldClip));
			
			if (!model.isFlipped()) {
				paintPhoto(graphics);
				if (!model.getAnnotatedPhoto().imageLoaded) {
					paintPhotoPath(graphics);
				}
			} else {
				paintPhotoBack(graphics);
				paintStrokes(graphics);
				paintPrimitives(graphics);
				paintAnnotations(graphics);
				if (isEditingText) {
					paintEditingMark(graphics);
				}
			}
			
			g.setClip(oldClip);
			paintPhotoFrame(graphics);
		}
	}
	
	private void paintPhoto(Graphics graphics) {
		Graphics2D g = (Graphics2D)graphics;
		
		Rectangle rect = calculateImageRect();
		Image img = model.getAnnotatedPhoto().image;
		
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
		Stroke oldStroke = g.getStroke();
	    
		List<AnnotatedPhoto.StrokeMark> strokes = model.getAnnotatedPhoto().strokes;
		if (model.isFlipped()) {
			for (AnnotatedPhoto.StrokeMark stroke : strokes) {
				float realWidth = (float)(stroke.width * imageScaleY);
				
				Stroke strokeType = new BasicStroke(realWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
				g.setStroke(strokeType);
				g.setColor(stroke.color);
						
				Path2D line = new Path2D.Double();
				Point p0 = imageToComponentCoordinates(stroke.path.get(0));
				line.moveTo(p0.x, p0.y);
				for (int i = 1; i < stroke.path.size(); i++) {
					Point p = imageToComponentCoordinates(stroke.path.get(i));
					line.lineTo(p.x, p.y);
				}
				g.draw(line);
			}
		}
		
		g.setStroke(oldStroke);
	}
	
	private void paintPrimitives(Graphics graphics) {
		Graphics2D g = (Graphics2D)graphics;
		Stroke oldStroke = g.getStroke();
	    
		List<AnnotatedPhoto.PrimitiveMark> primitives = model.getAnnotatedPhoto().primitives;
		if (model.isFlipped()) {
			for (AnnotatedPhoto.PrimitiveMark primitive : primitives) {
				float realWidth = (float)(primitive.lineWidth * imageScaleY);
				
				Stroke strokeType = new BasicStroke(realWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
				g.setStroke(strokeType);
				g.setColor(primitive.color);
				
				Point p1c = imageToComponentCoordinates(primitive.p1);
				Point p2c = imageToComponentCoordinates(primitive.p2);
				int x = Math.min(p1c.x, p2c.x);
				int y = Math.min(p1c.y, p2c.y);
				int w = Math.abs(p1c.x - p2c.x);
				int h = Math.abs(p1c.y - p2c.y);
				
				Shape shape = null;
				if (primitive.type == AnnotatedPhoto.PrimitiveMark.Type.StraightLine) {
					shape = new Line2D.Float(p1c.x, p1c.y, p2c.x, p2c.y);
				} else if (primitive.type == AnnotatedPhoto.PrimitiveMark.Type.Rectangle) {
					shape = new Rectangle2D.Float(x, y, w, h);
				} else if (primitive.type == AnnotatedPhoto.PrimitiveMark.Type.Ellipse) {
					shape = new Ellipse2D.Float(x, y, w, h);
				}
				g.draw(shape);
			}
		}
		
		g.setStroke(oldStroke);
	}
	
	private void paintAnnotations(Graphics graphics) {
		Graphics2D g = (Graphics2D)graphics;
		Rectangle rect = calculateImageRect();
		
		List<AnnotatedPhoto.Annotation> annotations = model.getAnnotatedPhoto().annotations;
		if (model.isFlipped()) {
			for (AnnotatedPhoto.Annotation annotation : annotations) {
				Font font = new Font(annotation.font, Font.PLAIN, (int)(annotation.size * imageScaleY));
				g.setFont(font);
				g.setColor(annotation.color);
				FontMetrics metrics = g.getFontMetrics();
				
				Point pos = imageToComponentCoordinates(annotation.position);
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
							annotationEditingPoint = componentToImageCoordinates(new Point(pos.x, pos.y + (int)(annotation.size * imageScaleY * lineSpacing)));
						} else if (annotationEditingPos < startPosInAnnotation + length || length == str.length()) {
							String sub = str.substring(0, annotationEditingPos - startPosInAnnotation);
							annotationEditingPoint = componentToImageCoordinates(new Point(pos.x + metrics.stringWidth(sub), pos.y));
						}
					}
					
					startPosInAnnotation += length;
					str = str.substring(length);
					pos.y += (int)(annotation.size * imageScaleY * lineSpacing);
				}
			}
		}
	}
	
	private void paintEditingMark(Graphics graphics) {
		if (annotationEditingPoint != null) {
			Graphics2D g = (Graphics2D)graphics;
			g.setColor(Color.BLACK);
	
			int size = currentAnnotation == null ? currentTextSize : currentAnnotation.size;
			Point[] triangle = new Point[] {
					new Point(annotationEditingPoint.x, annotationEditingPoint.y),
					new Point(annotationEditingPoint.x - size / 4, annotationEditingPoint.y + size / 2),
					new Point(annotationEditingPoint.x + size / 4, annotationEditingPoint.y + size / 2)
			};
			int[] triangleXs = new int[3], triangleYs = new int[3];
			for (int i = 0; i < 3; i++) {
				Point p = imageToComponentCoordinates(triangle[i]);
				triangleXs[i] = p.x;
				triangleYs[i] = p.y;
			}
			g.fillPolygon(triangleXs, triangleYs, 3);
		}
	}
	
	private void paintPhotoPath(Graphics graphics) {
		Graphics2D g = (Graphics2D)graphics;
		Rectangle rect = calculateImageRect();
		
		Font font = new Font("SansSerif", Font.PLAIN, 15);
		g.setFont(font);
		g.setColor(Color.black);
		g.drawString(model.getAnnotatedPhoto().imageURL.toString(), rect.x + 10, rect.y + rect.height - 10);
	}
	
	
	
	// Updates and helper methods
	private void updateComponentSize() {
		int w, h;
		if (model.isShowingPhoto()) {
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
	
	// Find the best prefix of the string that fits in the given space. End with a \n or space if possible.
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
	
	private Rectangle calculateImageRect() {
		if (!model.isShowingPhoto()) {
			return new Rectangle(0, 0, 0, 0);
		}
		Image img = model.getAnnotatedPhoto().image;
		int midx = getWidth() / 2;
		int midy = getHeight() / 2;
		int imgW = (int)(img.getWidth(null) * imageScaleX);
		int imgH = (int)(img.getHeight(null) * imageScaleY);
		return new Rectangle(midx - imgW / 2, midy - imgH / 2, imgW, imgH);
	}
	
	private Rectangle calculateImageRectWithFrame() {
		Rectangle rect = calculateImageRect();
		rect.x -= frameWidth * imageScaleX;
		rect.y -= frameWidth * imageScaleY;
		rect.width += 2 * frameWidth * imageScaleX;
		rect.height += 2 * frameWidth * imageScaleY;
		return rect;
	}
	
	private Point componentToImageCoordinates(Point p) {
		Rectangle rect = calculateImageRect();
		return new Point((int)((p.x - rect.x) / imageScaleX), (int)((p.y - rect.y) / imageScaleY));
	}
	private Point imageToComponentCoordinates(Point p) {
		Rectangle rect = calculateImageRect();
		return new Point((int)(p.x * imageScaleX + rect.x), (int)(p.y * imageScaleY + rect.y));
	}

	
	
	// MouseListener & MouseMotionListener & MouseWheelListener: Mouse events
	@Override
	public void mouseClicked(MouseEvent e) {
		requestFocusInWindow();
		if (model.isShowingPhoto() && !isLocked) {
			if (e.getClickCount() == 1 && model.isFlipped()) {
				Point imagePoint = componentToImageCoordinates(e.getPoint());
				isEditingText = true;
				currentAnnotation = null;
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
		prevMouseDragPos = e.getPoint();
		if (calculateImageRect().contains(prevMouseDragPos) && model.isFlipped() && !isLocked) {
			canStartDrawing = true;
		}
		container.requestControlPanelHiding(controlPanelAlphaWhenDrawing);
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		currentStroke = null;
		currentPrimitive = null;
		canStartDrawing = false;
		container.requestControlPanelHiding(1);
	}
	@Override
	public void mouseDragged(MouseEvent e) {
		if (!isLocked) {
			if (canStartDrawing && model.isFlipped() && SwingUtilities.isLeftMouseButton(e)) {
				if (isCreatingPrimitive) {
					drawPrimitiveTo(e.getPoint());
				} else {
					drawStrokeTo(e.getPoint());
				}
			} else {
				scrollPhoto(e.getPoint().x - prevMouseDragPos.x, e.getPoint().y - prevMouseDragPos.y);
			}
		}
	}
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.isControlDown()) {
			// Scaling
			scalePhoto(e.getWheelRotation(), e.getPoint());
		} else {
			// Navigating
			if (e.getWheelRotation() > 0) {
				model.nextPhoto();
			} else {
				model.prevPhoto();
			}
		}
	}
	@Override
	public void mouseMoved(MouseEvent e) {
		// Do nothing
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		// Do nothing
	}
	@Override
	public void mouseExited(MouseEvent e) {
		// Do nothing
	}
	
	
	
	// KeyListener: Keyboard events
	@Override
	public void keyTyped(KeyEvent e) {
		if (isEditingText && !e.isControlDown() && !isLocked) {
			char c = e.getKeyChar();
			if ((int)c == KeyEvent.VK_BACK_SPACE) {
				deleteCharacter();
			} else if (c != KeyEvent.CHAR_UNDEFINED) {
				insertCharacter(c);
			}
		}
	}
	@Override
	public void keyPressed(KeyEvent e) {
		if (!isEditingText) {
			// Jump to another photo in the album
			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				model.nextPhoto();
			} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				model.prevPhoto();
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
		if (model.isFlipped() && e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z) {
			undo();
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
				model.saveAlbum();
			}
		}
		
		// Flipping animation
		if (isFlippingToBack || isFlippingToFront) {
			if (flippingAnimationProgress < flippingAnimationTime) {
				flippingAnimationProgress += 1000 / fps;
				flippingAnimationProgress = Math.min(flippingAnimationProgress, flippingAnimationTime);
			}     
			model.setFlipped((2 * flippingAnimationProgress < flippingAnimationTime) ? isFlippingToFront : isFlippingToBack);
			double scale = Math.abs(Math.cos(Math.PI * flippingAnimationProgress / flippingAnimationTime));
			imageScaleXMultiplier = scale;
			container.requestControlPanelHiding((float)scale);
			repaint();
			if (flippingAnimationProgress == flippingAnimationTime) {
				flippingAnimationProgress = 0;
				isFlippingToBack = false;
				isFlippingToFront = false;
				isLocked = false;
				imageScaleXMultiplier = 1;
			}
		}
	}

	// PhotoListener: React when the model is changed.
	@Override
	public void photoEventReceived(PhotoEvent e) {
		if (e.type == PhotoEvent.Type.ViewIndexChanged) {
			reset();
		} else if (e.type == PhotoEvent.Type.PhotoAnnotationChanged) {
			repaint();
		} else if (e.type == PhotoEvent.Type.AlbumSaved) {
			changed = false;
		}
	}
}
