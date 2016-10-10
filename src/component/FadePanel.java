package component;
import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

// This class is an extended version of JPanel that supports transparency.
public class FadePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private float alpha = 1;

	public FadePanel() {
		super();
	}
	
	public void setAlpha(float alpha) {
		this.alpha = Math.min(Math.max(alpha, 0), 1);
	}

	@Override
	public void paint(Graphics graphics) {
		Graphics2D g = (Graphics2D)graphics;
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		super.paint(graphics);
	}
}
