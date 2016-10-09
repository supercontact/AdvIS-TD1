package component;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;

import custom.GlobalSettings;
import custom.ResourceManager;
import model.AnnotatedPhoto;
import scene.ImageBorderNode;
import scene.ImageNode;
import scene.Node;
import scene.RectangleNode;

public class PhotoIcon extends GraphicalComponent {

	private static final long serialVersionUID = 1L;
	
	// Constants
	public final int thumbnailSize = GlobalSettings.thumbnailSize;
	public final int frameWidth = 20;
	public final double margin = 0.05;
	public final double rolloverScale = 1.025;
	public final double rolloverRotate = 0.03;
	public final double pressedScale = 1.05;
	
	// Links
	public AnnotatedPhoto photo;
	public PhotoContainer container;
	
	// Graphical nodes
	private ImageNode photoNode;
	private ImageBorderNode frameNode;
	private Node photoContainerNode;
	private RectangleNode selectionBoxNode;
	
	// Internal variables
	private boolean isRollover = false;
	private boolean isPressed = false;
	private boolean isSelected = false;
	
	
	public PhotoIcon(AnnotatedPhoto photo) {
		this.photo = photo;
		if (photo.thumbnail == null) {
			photo.thumbnail = ResourceManager.errorImageThumbnail;
		}
		setPreferredSize(new Dimension(
				(int)((thumbnailSize + 2 * frameWidth) * (1 + margin)), 
				(int)((thumbnailSize + 2 * frameWidth) * (1 + margin))));
		
		initializeGraphicalNodes();
	}
	
	private void initializeGraphicalNodes() {
		graphicalNode = new Node();
		photoContainerNode = new Node();
		photoNode = new ImageNode();
		photoNode.image = photo.thumbnail;
		photoNode.setPosition(new Point(- thumbnailSize / 2, - thumbnailSize / 2));
		frameNode = new ImageBorderNode(
				ResourceManager.frameImage, 
				thumbnailSize + 2 * frameWidth, 
				thumbnailSize + 2 * frameWidth, 
				frameWidth);
		frameNode.setPosition(new Point(- thumbnailSize / 2 - frameWidth, - thumbnailSize / 2 - frameWidth));
		selectionBoxNode = new RectangleNode(new Point(0, 0), new Point(0, 0));
		selectionBoxNode.fillColor = new Color(120, 120, 200, 128);
		selectionBoxNode.drawBorder = false;
		
		graphicalNode.addChild(selectionBoxNode);
		graphicalNode.addChild(photoContainerNode);
		photoContainerNode.addChild(photoNode);
		photoContainerNode.addChild(frameNode);
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
		updateGraphicalNodes();
		super.paintComponent(graphics);
	}
	
	private void updateGraphicalNodes() {
		photoContainerNode.setPosition(getWidth() / 2, getHeight() / 2);
		if (isPressed) {
			photoContainerNode.setScale(pressedScale, pressedScale);
			photoContainerNode.setRotation(0);
		} else if (isRollover) {
			photoContainerNode.setScale(rolloverScale, rolloverScale);
			photoContainerNode.setRotation(rolloverRotate);
		} else {
			photoContainerNode.setScale(1, 1);
			photoContainerNode.setRotation(0);
		}
		selectionBoxNode.isVisible = isSelected;
		selectionBoxNode.p2 = new Point(getWidth(), getHeight());
	}
}
