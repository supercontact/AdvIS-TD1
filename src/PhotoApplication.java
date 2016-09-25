import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileNameExtensionFilter;

public class PhotoApplication extends JFrame{

    private static final long serialVersionUID = 1L;
    
    JLabel status = new JLabel();
    JMenuBar menuBar = new JMenuBar();;
    JMenu fileMenu = new JMenu("File");
    JMenu viewMenu = new JMenu("View");
    JMenuItem importItem = new JMenuItem("Import");
    JMenuItem deleteItem = new JMenuItem("Delete");
    JMenuItem quitItem = new JMenuItem("Quit");
    JMenuItem photoItem = new JMenuItem("Photo viewer");
    JMenuItem browserItem = new JMenuItem("Browser");
    JMenuItem splitItem = new JMenuItem("Split mode");
    JScrollPane body = new JScrollPane();
    PhotoComponent photoView = new PhotoComponent();
    JToolBar tool = new JToolBar();
    ArrayList<JToggleButton> categories = new ArrayList<>(); 

    public static void main(String[] args) {
    	SavedSettings.loadSettings();
        new PhotoApplication();
    }
   
    public PhotoApplication() {
        super("Assignment #1");
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       
        setPreferredSize(new Dimension(1024, 768));
        setMinimumSize(new Dimension(200, 200));
        setVisible(true);
       
        setupMenuBar();
        setupToolBar();
        setupMainArea();
       
        pack();
    }
    
    private void setupMenuBar() {
    	setJMenuBar(menuBar);
        
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
       
        fileMenu.add(importItem);
        fileMenu.add(deleteItem);
        fileMenu.add(quitItem);
        
        importItem.addActionListener(
        		event -> importImages()
        );
        deleteItem.addActionListener(
        		event -> showStatusText("Deleting current photo...(Not yet supported)")
        );
        quitItem.addActionListener(
        		event -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING))
        );
       
       
        viewMenu.add(photoItem);
        viewMenu.add(browserItem);
        viewMenu.add(splitItem);
        
        photoItem.addActionListener(
        		event -> showStatusText("Switched to photo viewer mode (Not yet supported).")
        );
        browserItem.addActionListener(
        		event -> showStatusText("Switched to browser mode (Not yet supported).")
        );
        splitItem.addActionListener(
        		event -> showStatusText("Switched to split mode (Not yet supported).")
        );
    }
    
    private void setupToolBar() {
    	add(tool, BorderLayout.NORTH);
    	
    	categories.add(new JToggleButton("Family"));
        categories.add(new JToggleButton("Vacation"));
        categories.add(new JToggleButton("School"));
        
        for (JToggleButton button : categories) {
        	button.setMinimumSize(new Dimension(80, 30));
        	button.setPreferredSize(new Dimension(80, 30));
        	button.setMaximumSize(new Dimension(80, 30));
        	tool.add(button);
        	 
        	button.addItemListener(
        			event -> {
        				if (event.getStateChange() == ItemEvent.SELECTED) {
        					showStatusText("Catogory " + ((JToggleButton)event.getItem()).getText() + " is selected (Not yet supported).");
        				} else {
        					showStatusText("Catogory " + ((JToggleButton)event.getItem()).getText() + " is deselected (Not yet supported).");
        				}
        			}
        	);
        }
    }
    
    private void setupMainArea() {
    	add(body, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        
        body.setViewportView(photoView);
       
        showStatusText("Status");
    }
    
	public void showStatusText(String text) {
		status.setText(text);
	}
	
	public void importImages() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(SavedSettings.settings.defaultFileLocation);
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif", "bmp"));
		
		int state = fileChooser.showOpenDialog(this);
		if (state == JFileChooser.CANCEL_OPTION) {
			showStatusText("Operation canceled!");
			return;
		}
		if (state == JFileChooser.ERROR_OPTION) {
			showStatusText("An error happened!");
			return;
		}
		File[] files = fileChooser.getSelectedFiles();
		photoView.addPhotos(files);
		SavedSettings.settings.defaultFileLocation = files[0].getParentFile();
		SavedSettings.saveSettings();
		showStatusText(files.length + " photo(s) are selected (currently only showing the first photo).");
	}

}