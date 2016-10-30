package scene;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

// This class represents the base of a scene-graph node. It does not contain any graphical content.
// Every node can be a container, or serve as root.

public class Node implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// Make this field transient to prevent all the parents being serialized.
	// Need to call reconstruct() to reassign parent.
	private transient Node parent;
	
	// Controlled parameters, to be accessed via getter/setters
	private ArrayList<Node> children;
	private double posX = 0, posY = 0;
	private double rotation = 0;
	private double scaleX = 1, scaleY = 1;
	private AffineTransform transform;
	private AffineTransform customTransform;
	
	// Public parameters, can be accessed freely
	public boolean isVisible = true;
	public Float strokeWidth = null;
	public Color strokeColor = null;
	public Color fillColor = null;
	public Color textColor = null;
	public Integer textSize = null;
	public Integer textStyle = null;
	public String font = null;
	public float alpha = 1;
	public Shape clip = null;
	public boolean showBounds = false;
	
	public Node() {
		transform = new AffineTransform();
		children = new ArrayList<>();
	}
	
	// Getters & setters
	public Node getParent() {
		return parent;
	}
	public void setParent(Node parent) {
		if (this.parent != null) {
			this.parent.removeChild(this);
		}
		if (parent != null) {
			parent.addChild(this);
		}
	}
	
	public int getChildCount() {
		return children.size();
	}
	public Node getChild(int index) {
		return children.get(index);
	}
	public List<Node> getChildren() {
		return new ChildrenView(this);
	}
	public void addChild(Node child) {
		if (child.parent != null) {
			child.parent.removeChild(child);
		}
		children.add(child);
		child.parent = this;
	}
	public void removeChild(Node child) {
		if (children.remove(child)) {
			child.parent = null;
		}
	}
	public void removeAllChildren() {
		for (Node child : children) {
			child.parent = null;
		}
		children.clear();
	}
	
	// Transform: The AffineTransform of the node is obtained by applying in this order:
	// 1 - Scaling, 2 - Rotation, 3 - Translation
	// Which means Tfinal = Ttranslation * Trotation * Tscaling
	public double getX() {
		return posX;
	}
	public double getY() {
		return posY;
	}
	public Point getPosition() {
		return new Point((int)posX, (int)posY);
	}
	public void setPosition(double x, double y) {
		posX = x;
		posY = y;
		updateTransform();
	}
	public void setPosition(Point pos) {
		posX = pos.x;
		posY = pos.y;
		updateTransform();
	}
	
	public double getRotation() {
		return rotation;
	}
	public void setRotation(double rotation) {
		this.rotation = rotation;
		updateTransform();
	}
	
	public double getScaleX() {
		return scaleX;
	}
	public double getScaleY() {
		return scaleY;
	}
	public void setScale(double scaleX, double scaleY) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		updateTransform();
	}
	
	public AffineTransform getTransform() {
		if (customTransform != null) {
			return customTransform;
		} else {
			return transform;
		}
	}
	// Set a custom AffineTransform.
	// Any attempt to modify the position/rotation/scaling afterwards will cancel its effect.
	public void setTransform(AffineTransform transform) {
		customTransform = transform;
	}
	
	public AffineTransform getGlobalTransform() {
		if (parent == null) {
			return getTransform();
		} else {
			AffineTransform result = new AffineTransform(parent.getGlobalTransform());
			result.concatenate(getTransform());
			return result;
		}
	}
	
	
	private void updateTransform() {
		transform = new AffineTransform();
		transform.concatenate(AffineTransform.getTranslateInstance(posX, posY));
		transform.concatenate(AffineTransform.getRotateInstance(rotation));
		transform.concatenate(AffineTransform.getScaleInstance(scaleX, scaleY));
		customTransform = null;
	}
	
	// Get the bounds of the node (including its children) in the parent coordinate system;
	public Rectangle getBounds() {
		Rectangle result = transformBounds(getContentBounds(), getTransform());
		for (Node child : children) {
			result = result.union(transformBounds(child.getBounds(), getTransform()));
		}
		return result;
	}
	
	// Get the bounds of the node (including its children) in the local coordinate system;
	public Rectangle getLocalBounds() {
		Rectangle result = getContentBounds();
		for (Node child : children) {
			result = result.union(child.getBounds());
		}
		return result;
	}
	
	// Get the bounds of the node's graphical content (not including the children) in the local coordinate system.
	// To be overridden
	public Rectangle getContentBounds() {
		return new Rectangle(0, 0, -1, -1);
	}
	
	public boolean isContainedInGlobalRegion(Rectangle rect) {
		AffineTransform globalTransform = getGlobalTransform();
		Rectangle localBounds = getLocalBounds();
		ArrayList<Point2D> boundsCorners = new ArrayList<>();
		boundsCorners.add(globalTransform.transform(new Point(localBounds.x, localBounds.y), null));
		boundsCorners.add(globalTransform.transform(new Point(localBounds.x + localBounds.width, localBounds.y), null));
		boundsCorners.add(globalTransform.transform(new Point(localBounds.x, localBounds.y + localBounds.height), null));
		boundsCorners.add(globalTransform.transform(new Point(localBounds.x + localBounds.width, localBounds.y + localBounds.height), null));
		for (Point2D p : boundsCorners) {
			if (!rect.contains(p)) return false;
		}
		return true;
	}
	
	// Reassign the correct parent after being loaded from a serialized state
	public void reconstruct() {
		for (Node child : children) {
			child.parent = this;
			child.reconstruct();
		}
	}
	
	// Painting methods
	public void paint(Context context) {
		if (!isVisible) return;
		
		// Set context
		context.newState();
		context.concatenateTransform(getTransform());
		context.combineAlpha(alpha);
		if (strokeWidth != null) context.setStrokeWidth(strokeWidth);
		if (strokeColor != null) context.setStrokeColor(strokeColor);
		if (fillColor != null) context.setFillColor(fillColor);
		if (textColor != null) context.setTextColor(textColor);
		if (textSize != null) context.setTextSize(textSize);
		if (textStyle != null) context.setTextStyle(textStyle);
		if (font != null) context.setFont(font);
		if (clip != null) context.combineClip(clip);
		context.apply();
		
		// Paint objects
		paintNode(context);
		paintChildren(context);
		if (context.showBounds || showBounds) {
			paintBounds(context);
		}
		
		// Reset context
		context.revertState();
	}
	
	// To be overridden
	public void paintNode(Context context) {
		
	}
	
	public void paintChildren(Context context) {
		for (Node node : children) {
			node.paint(context);
		}
	}
	
	// For debug use
	public void paintBounds(Context context) {
		context.apply();
		context.graphics.setClip(null);
		context.graphics.setComposite(AlphaComposite.SrcIn);
		context.graphics.setStroke(new BasicStroke(1));
		context.graphics.setColor(Color.ORANGE);
		context.graphics.draw(getLocalBounds());
		context.graphics.setColor(Color.RED);
		context.graphics.draw(getContentBounds());
	}
	
		
	// A utility function that calculate bounds from a list of points
	public static Rectangle calculateBoundsWithPoints(List<Point> listOfPoints) {
		Rectangle result = new Rectangle(0, 0, -1, -1);
		for (Point p : listOfPoints) {
			result.add(p);
		}
		return result;
	}
	
	// A utility function that calculate transformed bounds
	public static Rectangle transformBounds(Rectangle bounds, AffineTransform transform) {
		Point2D[] corners = new Point2D[] {
				new Point2D.Double(bounds.x, bounds.y),
				new Point2D.Double(bounds.x + bounds.width, bounds.y),
				new Point2D.Double(bounds.x + bounds.width, bounds.y + bounds.height),
				new Point2D.Double(bounds.x, bounds.y + bounds.height)};
		List<Point> list = new ArrayList<>(4);
		for (int i = 0; i < 4; i++) {
			list.add(Point2DtoPoint(transform.transform(corners[i], null)));
		}
		return calculateBoundsWithPoints(list);
	}
	
	private static Point Point2DtoPoint(Point2D p) {
		return new Point((int)p.getX(), (int)p.getY());
	}
	
	
	// A list view of the children. Has correct behaviors when modifying the list.
	private class ChildrenView extends AbstractList<Node> {
		
		private Node node;
		
		public ChildrenView(Node node) {
			this.node = node;
		}
		@Override
		public int size() {
			return node.children.size();
		}
		@Override
		public Node get(int index) {
			return node.children.get(index);
		}
		@Override
		public Node set(int index, Node child) {
			if (child.parent != null) {
				child.parent.removeChild(child);
			}
			Node oldChild = node.children.get(index);
			oldChild.parent = null;
			node.children.set(index, child);
			child.parent = node;
			return oldChild;
		}
		@Override
		public void add(int index, Node child) {
			if (child.parent != null) {
				child.parent.removeChild(child);
			}
			node.children.add(index, child);
			child.parent = node;
		}
		@Override
		public Node remove(int index) {
			Node oldChild = node.children.get(index);
			oldChild.parent = null;
			node.children.remove(index);
			return oldChild;
		}
	}
}
