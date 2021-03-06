package scene;

import java.awt.Rectangle;
import java.awt.Shape;

// This node displays a Shape object.
// It can also be used as a clipping mask.

public class ShapeNode extends Node {
	
	private static final long serialVersionUID = 1L;
	
	public Shape shape;
	public boolean drawBorder = true;
	public boolean fillInside = true;
	
	private boolean clipMode = false;
	
	public ShapeNode() {}
	public ShapeNode(Shape shape) {
		this.shape = shape;
	}
	
	// Can be overridden by more specific subclasses.
	Shape getShape() {
		return shape;
	}
	
	@Override
	public Rectangle getContentBounds() {
		return getShape().getBounds();
	}
	
	@Override
	public void paintNode(Context context) {
		Shape s = getShape();
		if (s == null || clipMode) return;
		if (fillInside) {
			context.beginFill();
			context.graphics.fill(s);
		}
		if (drawBorder) {
			context.beginStroke();
			context.graphics.draw(s);
		}
	}
	
	public void useAsClip(boolean useShapeAsClip) {
		if (useShapeAsClip) {
			clipMode = true;
			clip = getShape();
		} else {
			clipMode = false;
			clip = null;
		}
	}
	
}
