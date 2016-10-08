package scene;

import java.awt.Rectangle;
import java.awt.Shape;

public class ShapeNode extends Node {
	
	public Shape shape;
	public boolean drawBorder = true;
	public boolean fillInside = true;
	
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
		if (fillInside) {
			context.beginFill();
			context.graphics.fill(s);
		}
		if (drawBorder) {
			context.beginStroke();
			context.graphics.draw(s);
		}
	}
	
}
