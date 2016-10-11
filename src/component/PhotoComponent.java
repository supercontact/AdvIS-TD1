package component;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import custom.ResourceManager;
import model.AnnotatedPhoto;
import model.PhotoApplicationModel;
import model.PhotoEvent;
import model.PhotoListener;
import scene.EllipseNode;
import scene.ImageBorderNode;
import scene.ImageNode;
import scene.Node;
import scene.PathNode;
import scene.PrimitiveNode;
import scene.RectangleNode;
import scene.StraightLineNode;
import scene.TextNode;

// This component displays an AnnotatedPhoto of the album.
// It allows creating annotations when the photo is flipped.
// The component should be added in a JScollpane to function properly.

public class PhotoComponent extends GraphicalComponent implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, ActionListener, PhotoListener {

	private static final long serialVersionUID = 1L;
	
	public enum ScaleMode {
		OriginalSize,
		FitWindow,
		FitWidth,
		FitHeight
	}
	
	public enum PrimitiveType {
		StraightLine,
		Rectangle,
		Ellipse
	}
	
	// Constants
	public final int frameWidth = 20;
	public final double imageScaleMinimum = 0.05;
	public final double imageScaleMaximum = 20;
	public final double scaleFactor = 1.1;
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
	public float currentStrokeWidth = 5;
	public int currentTextSize = 15;
	public PrimitiveType currentPrimitiveType = PrimitiveType.Ellipse;
	public Color currentColor = new Color(0x19, 0x2C, 0x3C);
	public String currentFontName = "Arial";
	
	// Link
	public PhotoContainer container;
	
	// Model
	private PhotoApplicationModel model;
	
	// Graphical nodes
	private ImageNode photoNode;
	private RectangleNode photoBackNode;
	private ImageBorderNode frameNode;
	private Node annotationNode;
	private TextNode errorPathNode;
	
	// Internal variables
	private boolean activated = false;
	private Timer timer;
	private Point prevMouseDragPos;
	private boolean isLocked = false;
	private boolean isFlippingToBack = false;
	private boolean isFlippingToFront = false;
	private boolean isEditingText = false;
	private long flippingAnimationProgress = 0;
	private double imageScaleXMultiplier = 1;
	private PathNode currentStroke;
	private PrimitiveNode currentPrimitive;
	private TextNode currentText;
	private AnnotatedPhoto currentTextPhoto;
	private int textEditingPos = 0;
	private boolean canStartDrawing = false;
	private boolean changed = false;
	private boolean customScaled = false;
	private long lastSaveTime = 0;
	

	public PhotoComponent(PhotoApplicationModel model) {
		this.model = model;
		
		setFocusable(true);
		initializeGraphicalNodes();
	}
	
	private void initializeGraphicalNodes() {
		graphicalNode = new Node();
		photoNode = new ImageNode();
		photoBackNode = new RectangleNode(new Point(0, 0), new Point(0, 0));
		photoBackNode.fillColor = Color.WHITE;
		photoBackNode.drawBorder = false;
		frameNode = new ImageBorderNode(ResourceManager.frameImage, 0, 0, frameWidth);
		frameNode.setPosition(new Point(-frameWidth, -frameWidth));
		errorPathNode = new TextNode();
		errorPathNode.textColor = Color.BLACK;
		annotationNode = new Node();
		
		graphicalNode.addChild(photoNode);
		graphicalNode.addChild(photoBackNode);
		graphicalNode.addChild(frameNode);
		graphicalNode.addChild(errorPathNode);
		graphicalNode.addChild(annotationNode);
	}
	
	public void activate() {
		if (activated) return;
		
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);
		model.addPhotoListener(this);
		timer = new Timer(1000 / fps, this);
		timer.setInitialDelay(10);
		timer.start(); 
		
		reset();
		activated = true;
	}
	
	public void deactivate() {
		if (!activated) return;
		
		removeMouseListener(this);
		removeMouseMotionListener(this);
		removeMouseWheelListener(this);
		removeKeyListener(this);
		model.removePhotoListener(this);
		timer.stop();
		
		activated = false;
	}
	
	public void reset() {
		model.setFlipped(false);
		cancelEditing();
		customScaled = false;
		fitPhoto();
		revalidate();
		repaint();
	}
	
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
			currentStroke = new PathNode();
			model.getAnnotatedPhoto().annotation.addChild(currentStroke);
			model.getAnnotatedPhoto().registerNode(currentStroke);
			currentStroke.strokeColor = currentColor;
			currentStroke.strokeWidth = currentStrokeWidth;
			currentStroke.path.add(componentToImageCoordinates(prevMouseDragPos));
			isEditingText = false;
		}
		currentStroke.path.add(componentToImageCoordinates(pos));
		changed = true;
		repaint();
		prevMouseDragPos = pos;
	}
	
	public void drawPrimitiveTo(Point pos) {
		Point p1 = componentToImageCoordinates(prevMouseDragPos);
		Point p2 = componentToImageCoordinates(pos);
		
		if (currentPrimitive == null) {
			if (currentPrimitiveType == PrimitiveType.StraightLine) {
				currentPrimitive = new StraightLineNode(p1, p2);
			} else if (currentPrimitiveType == PrimitiveType.Rectangle) {
				currentPrimitive = new RectangleNode(p1, p2);
			} else if (currentPrimitiveType == PrimitiveType.Ellipse) {
				currentPrimitive = new EllipseNode(p1, p2);
			}
			model.getAnnotatedPhoto().annotation.addChild(currentPrimitive);
			model.getAnnotatedPhoto().registerNode(currentPrimitive);
			currentPrimitive.strokeColor = currentColor;
			currentPrimitive.fillInside = false;
			currentPrimitive.strokeWidth = currentStrokeWidth;
			isEditingText = false;
		} else {
			currentPrimitive.p2 = p2;
		}
		changed = true;
		repaint();
		prevMouseDragPos = pos;
	}
	
	public void createTextField(Point pos) {
		isEditingText = true;
		if (currentText != null) {
			currentText.showMarker = false;
			if (currentText.text.length() == 0) {
				model.getAnnotatedPhoto().undo();
			}
		}
		
		TextNode text = new TextNode();
		model.getAnnotatedPhoto().annotation.addChild(text);
		model.getAnnotatedPhoto().registerNode(text);
		text.textColor = currentColor;
		text.textSize = currentTextSize;
		text.font = currentFontName;
		text.setPosition(pos);
		text.width = model.getAnnotatedPhoto().image.getWidth(null) - pos.x;
		text.showMarker = true;
		text.markerPosition = 0;
		currentText = text;
		currentTextPhoto = model.getAnnotatedPhoto();
			
		textEditingPos = 0;
		repaint();
	}
	
	public void insertCharacter(char c) {
		if (textEditingPos == currentText.text.length()) {
			// Add at the end
			currentText.text += c;
		} else {
			// Insert in the middle
			currentText.text = currentText.text.substring(0, textEditingPos) + c + 
					currentText.text.substring(textEditingPos, currentText.text.length());
		}
		textEditingPos++;
		currentText.markerPosition = textEditingPos;
		changed = true;
		repaint();
	}
	
	public void deleteCharacter() {
		if (textEditingPos > 0) {
			currentText.text = currentText.text.substring(0, textEditingPos - 1)
					+ currentText.text.substring(textEditingPos, currentText.text.length());
			textEditingPos--;
			currentText.markerPosition = textEditingPos;
		}
		changed = true;
		repaint();
	}
	
	public void cancelEditing() {
		isEditingText = false;
		if (currentText != null) {
			currentText.showMarker = false;
			if (currentText.text.length() == 0) {
				currentText.setParent(null);
				currentTextPhoto.undo();
			}
			currentText = null;
			currentTextPhoto = null;
		}
	}
	
	public void undo() {
		cancelEditing();
		model.getAnnotatedPhoto().undo();
		changed = true;
		repaint();
		PhotoApplication.showStatusText("Undo last operation.");
	}
	
	
	// Painting method. Update and display the root graphical node.
	@Override
	public void paintComponent(Graphics graphics) {	
		if (!customScaled) {
			fitPhoto();
			revalidate();
		}
		imageScaleX = imageScaleY * imageScaleXMultiplier;
		
		if (!activated) return;
		updateGraphicalNodes();
		super.paintComponent(graphics); // Let the super class GraphicalComponent draw the graphical content.
	}
	
	
	// Updates
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
	
	private void updateGraphicalNodes() {
		if (!model.isShowingPhoto()) {
			graphicalNode.isVisible = false;
		} else {
			Rectangle rect = calculateImageRect();
			int w = model.getAnnotatedPhoto().image.getWidth(null);
			int h = model.getAnnotatedPhoto().image.getHeight(null);
			
			graphicalNode.isVisible = true;
			graphicalNode.setPosition(rect.x, rect.y);
			graphicalNode.setScale(imageScaleX, imageScaleY);
			photoNode.image = model.getAnnotatedPhoto().image;
			photoNode.isVisible = !model.isFlipped();
			photoBackNode.p2 = new Point(w, h);
			photoBackNode.isVisible = model.isFlipped();
			frameNode.width = w + 2 * frameWidth;
			frameNode.height = h + 2 * frameWidth;
			errorPathNode.setPosition(new Point(10, rect.height - 10));
			errorPathNode.isVisible = !model.getAnnotatedPhoto().imageLoaded && !model.isFlipped();
			errorPathNode.text = model.getAnnotatedPhoto().imageURL.toString();
			annotationNode.removeAllChild();
			annotationNode.addChild(model.getAnnotatedPhoto().annotation);
			annotationNode.clip = new Rectangle(0, 0, w, h);
			annotationNode.isVisible = model.isFlipped();
		}
	}
	
	private void updateCursor(Point mousePosition) {
		if (calculateImageRect().contains(mousePosition) && model.isFlipped()) {
			setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		} else {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}
	
	
	// Helper methods
	public Rectangle calculateImageRect() {
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
	
	public Rectangle calculateImageRectWithFrame() {
		Rectangle rect = calculateImageRect();
		rect.x -= frameWidth * imageScaleX;
		rect.y -= frameWidth * imageScaleY;
		rect.width += 2 * frameWidth * imageScaleX;
		rect.height += 2 * frameWidth * imageScaleY;
		return rect;
	}
	
	public Point componentToImageCoordinates(Point p) {
		Rectangle rect = calculateImageRect();
		return new Point((int)((p.x - rect.x) / imageScaleX), (int)((p.y - rect.y) / imageScaleY));
	}
	public Point imageToComponentCoordinates(Point p) {
		Rectangle rect = calculateImageRect();
		return new Point((int)(p.x * imageScaleX + rect.x), (int)(p.y * imageScaleY + rect.y));
	}

	
	
	// MouseListener & MouseMotionListener & MouseWheelListener: Mouse events
	@Override
	public void mouseClicked(MouseEvent e) {
		requestFocusInWindow();
		if (model.isShowingPhoto() && !isLocked) {
			if (e.getClickCount() == 1 && model.isFlipped()) {
				// Single click: Start editing text
				createTextField(componentToImageCoordinates(e.getPoint()));
			} else if (e.getClickCount() % 2 == 0) {
				// Double click: Flip photo
				cancelEditing();
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
				// Dragging at the back of the photo: Draw things
				cancelEditing();
				if (isCreatingPrimitive) {
					drawPrimitiveTo(e.getPoint());
				} else {
					drawStrokeTo(e.getPoint());
				}
			} else {
				// Dragging the photo (or the back of the photo with right mouse button): Move photo
				scrollPhoto(e.getPoint().x - prevMouseDragPos.x, e.getPoint().y - prevMouseDragPos.y);
			}
		}
	}
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.isControlDown()) {
			// Ctrl + mouse wheel: Scaling
			scalePhoto(e.getWheelRotation(), e.getPoint());
		} else {
			// Mouse wheel: Navigating
			if (e.getWheelRotation() > 0) {
				model.nextPhoto();
			} else {
				model.prevPhoto();
			}
		}
	}
	@Override
	public void mouseMoved(MouseEvent e) {
		updateCursor(e.getPoint());
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
				// Back space key: Delete character
				deleteCharacter();
			} else if (c != KeyEvent.CHAR_UNDEFINED) {
				// Other key: Insert character
				insertCharacter(c);
			}
		}
	}
	@Override
	public void keyPressed(KeyEvent e) {
		if (!isEditingText) {
			// Left & Right keys: Jump to another photo in the album
			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				model.nextPhoto();
			} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				model.prevPhoto();
			}
		} else {
			// Left & Right keys when editing: Navigate the pointer in the current editing annotation
			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				textEditingPos = Math.min(textEditingPos + 1, currentText.text.length());
				currentText.markerPosition = textEditingPos;
				repaint();
			} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				textEditingPos = Math.max(textEditingPos - 1, 0);
				currentText.markerPosition = textEditingPos;
				repaint();
			}
		}
		if (model.isFlipped() && e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z) {
			// Ctrl + z: Undo
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
