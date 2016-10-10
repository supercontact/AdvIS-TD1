package scene;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Line2D;

// This node displays a straight line.
public class StraightLineNode extends PrimitiveNode {
	
	private static final long serialVersionUID = 1L;
	
	public StraightLineNode(Point p1, Point p2) {
		super(p1, p2);
	}
	
	@Override
	Shape getShape() {
		return new Line2D.Double(p1.x, p1.y, p2.x, p2.y);
	}
	
}
