package component;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JComponent;

import scene.Context;
import scene.Node;

// This component displays a node representing a scene-graph.
public class GraphicalComponent extends JComponent {
	
	private static final long serialVersionUID = 1L;
	
	public Node graphicalNode;
	
	private boolean showBounds = false;
	
	public GraphicalComponent() {
	
	}

	@Override
	public void paintComponent(Graphics g) {
		if (graphicalNode != null) {
			Context context = new Context((Graphics2D)g);
			context.showBounds = showBounds;
			graphicalNode.paint(context);
		}
	}
	
	public void useNodeSize() {
		Rectangle bounds = graphicalNode.getBounds();
		setPreferredSize(new Dimension(bounds.width, bounds.height));
	}
	
	// For debug use
	public void showBounds() {
		showBounds = true;
		repaint();
	}
	
	public void hideBounds() {
		showBounds = false;
		repaint();
	}
}
