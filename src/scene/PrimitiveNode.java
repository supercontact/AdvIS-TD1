package scene;

import java.awt.Point;

public abstract class PrimitiveNode extends ShapeNode {
	
	private static final long serialVersionUID = 1L;
	
	public Point p1, p2;
	
	public PrimitiveNode() {}
	public PrimitiveNode(Point p1, Point p2) {
		this.p1 = p1;
		this.p2 = p2;
	}
	
}
