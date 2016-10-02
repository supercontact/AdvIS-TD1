import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;

public class PhotoContainer extends JLayeredPane implements MouseMotionListener{

	private static final long serialVersionUID = 1L;
	
	public final int controlPanelOpaqueHeight = 100;
	public final int controlPanelTransparentHeight = 200;
	
	public PhotoComponent mainPhoto;
	public FadePanel controlPanel;
	public FadePanel controlPanelEditMode;
	
	private JScrollPane scrollPane;
	
	private Image background;
	
	private Point mousePos;
	private FadePanel currentControlPanel;
	private float controlPanelAlphaMultiplier = 1;
	
	public PhotoContainer() {
		initialize();
	}
	
	private void initialize() {
		scrollPane = new JScrollPane();
		add(scrollPane, 999);
		
		scrollPane.setWheelScrollingEnabled(false);
		scrollPane.getViewport().setOpaque(false);
		scrollPane.setOpaque(false);
		
		background = ResourceManager.backgroundImage;
	}
	
	public void setMainPhotoComponent(PhotoComponent mainPhoto) {
		this.mainPhoto = mainPhoto;
		mainPhoto.container = this;
		scrollPane.setViewportView(mainPhoto);
		mainPhoto.addMouseMotionListener(this);
	}
	
	public void requestControlPanelHiding(float alphaMultiplier) {
		controlPanelAlphaMultiplier = alphaMultiplier;
		updateControlPanelFade();
	}
	
	@Override
	public void paintComponent(Graphics graphics) {
		paintBackground(graphics);
		
		updateScrollPane();
		updateControlPanel();
	}
	
	private void paintBackground(Graphics graphics) {
		// The background stays fixed when scrolling
		int w = getWidth();
		int h = getHeight();
		int imgW = background.getWidth(null);
		int imgH = background.getHeight(null);
		if (w * imgH > h * imgW) {
			// Width is too large
			int imgH2 = h * imgW / w;
			graphics.drawImage(background, 0, 0, w, h, 0, (imgH - imgH2) / 2, imgW, (imgH + imgH2) / 2, null);
		} else {
			// Height is too large
			int imgW2 = w * imgH / h;
			graphics.drawImage(background, 0, 0, w, h, (imgW - imgW2) / 2, 0, (imgW + imgW2) / 2, imgH, null);
		}
	}
	
	private void updateScrollPane() {
		scrollPane.setLocation(new Point(0, 0));
		scrollPane.setSize(getSize().width + 2, getSize().height + 1); // To avoid the 1 pixel margin at the border
		scrollPane.revalidate();
	}
	
	private void updateControlPanel() {
		if (controlPanel == null || controlPanelEditMode == null) return;
		if (!mainPhoto.isFlipped() && currentControlPanel != controlPanel) {
			controlPanelEditMode.setVisible(false);
			currentControlPanel = controlPanel;
		} else if (mainPhoto.isFlipped() && currentControlPanel != controlPanelEditMode) {
			controlPanel.setVisible(false);
			currentControlPanel = controlPanelEditMode;
		}
		
		Dimension panelSize = currentControlPanel.getPreferredSize();
		currentControlPanel.setSize(panelSize);
		Point location = new Point();
		location.x = getWidth() / 2 - panelSize.width / 2;
		location.y = getHeight() - panelSize.height - 20;
		currentControlPanel.setLocation(location);
	}

	private void updateControlPanelFade() {
		if (currentControlPanel == null) return;
		int height = getHeight() - mousePos.y;
		if (height <= controlPanelOpaqueHeight) {
			currentControlPanel.setAlpha(controlPanelAlphaMultiplier);
			currentControlPanel.setVisible(true);
		} else if (height >= controlPanelTransparentHeight) {
			currentControlPanel.setAlpha(0);
			currentControlPanel.setVisible(false);
		} else {
			currentControlPanel.setAlpha(controlPanelAlphaMultiplier * (height - controlPanelTransparentHeight) / (controlPanelOpaqueHeight - controlPanelTransparentHeight));
			currentControlPanel.setVisible(true);
		}
		currentControlPanel.repaint();
	}

	

	@Override
	public void mouseMoved(MouseEvent e) {
		mousePos = getMousePosition(true);
		updateControlPanelFade();
	}
	@Override
	public void mouseDragged(MouseEvent e) {
		mousePos = getMousePosition(true);
		updateControlPanelFade();
	}
}
