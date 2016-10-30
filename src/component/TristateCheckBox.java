package component;

import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;

import custom.ResourceManager;

public class TristateCheckBox extends JCheckBox {

    private static final long serialVersionUID = 1L;
    
    private static Icon selectedIcon;
    private static Icon unselectedIcon;
    private static Icon halfselectedIcon;
    
    private boolean halfState;
    
    public TristateCheckBox() {
    	if (selectedIcon == null) {
	    	selectedIcon = new ImageIcon(ResourceManager.checkBoxIconSelected);
	    	unselectedIcon = new ImageIcon(ResourceManager.checkBoxIconUnselected);
	    	halfselectedIcon = new ImageIcon(ResourceManager.checkBoxIconHalfselected);
    	}
    }

    @Override
    public void paint(Graphics g) {
        if (isSelected()) {
            halfState = false;
        }
        setIcon(halfState ? halfselectedIcon : isSelected() ? selectedIcon : unselectedIcon);
        super.paint(g);
    }

    public boolean isHalfSelected() {
        return halfState;
    }

    public void setHalfSelected(boolean halfState) {
        this.halfState = halfState;
        if (halfState) {
            setSelected(false);
            repaint();
        }
    }
}