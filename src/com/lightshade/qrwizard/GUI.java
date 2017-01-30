package com.lightshade.qrwizard;

/*
БІБЛІОТЕКИ, НЕОБХІДНІ ДЛЯ РОБОТИ ПРОГРАМИ
  - com.lightshade.qrwizard.core: власна бібліотека для проведення усіх операцій
  - javax.swing, java.awt: графічні бібліотеки Java
  - java.io, java.util, org.apache.commons.io: стандартні бібліотеки (логгінг, введення/виведення, файли, ...)
  - com.google.zxing.NotFoundException: помилка 'не знайдено код' з бібліотеки ZXing
 */

import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import com.lightshade.qrwizard.core.QrWizard;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Графічний інтерфейс програми
 * @author Олексій Філоненко
 * @version 1.0
 */
public class GUI {
	private static String version = "1.0";
	private static Logger log = Logger.getLogger(GUI.class.getName());
	private JFrame mainFrame;
    private JTextArea textArea;

    /**
     * При створенні інтерфейсу відбувається його вибудова
     */
    public GUI(){ prepareGUI(); }

    /**
     * Основний метод, запуск програми
     * @param args не використовується
     */
	public static void main(String[] args) {
        configureLogging();
		new GUI();
	}

	private static void configureLogging() {
        try {
            LogManager.getLogManager().readConfiguration(
                    GUI.class.getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            System.err.println("Could not setup logger configuration: " + e.toString());
        }
    }

	private static void centerWindow(Window frame) {
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
		int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
		frame.setLocation(x, y);
	}
	
	private void prepareGUI() {
        log.fine("Preparing GUI");
		mainFrame = new JFrame("QRWizard " + version + ", (c) 2017 Alex Filonenko");
		mainFrame.setSize(800,600);
		mainFrame.setLayout(new GridLayout(3,0));
		mainFrame.addWindowListener(new WindowAdapter() {
	         public void windowClosing(WindowEvent windowEvent){
		        System.exit(0);
			 }
		});
        JLabel titleLabel = new JLabel("QRWizard " + version, JLabel.CENTER);
		float newSize = 50;
		titleLabel.setFont(titleLabel.getFont().deriveFont(newSize));
		textArea = new JTextArea(2,20);
        JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel();
	    buttonPanel.setLayout(new FlowLayout());
        JButton encodeButton = new JButton("Закодувати");
	    encodeButton.addActionListener(new EncodeActionListener());
        JButton decodeButton = new JButton("Розкодувати");
	    decodeButton.addActionListener(new DecodeActionListener());
        JButton quitButton = new JButton("Вихід");
	    quitButton.addActionListener(new QuitActionListener());
	    buttonPanel.add(encodeButton);
	    buttonPanel.add(decodeButton);
	    buttonPanel.add(quitButton);
	    controlPanel.add(buttonPanel, BorderLayout.CENTER);
		mainFrame.add(titleLabel);
		mainFrame.add(textArea);
	    mainFrame.add(controlPanel);
		centerWindow(mainFrame);
        log.fine("Showing GUI");
		mainFrame.setVisible(true);
	}

    /**
     * Слухач подій для кнопки 'Вийти'
     */
    class QuitActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
            log.fine("Exit button pressed - exitting");
			System.exit(0);
	      }
	}

    /**
     * Слухач подій для кнопки 'Закодувати'
     */
    class EncodeActionListener implements ActionListener {
        private String getFileName() throws CancelException {
            String name = JOptionPane.showInputDialog(mainFrame, "Введіть ім'я для файла (бажано коротке): ");
            if (name == null) {
                throw new CancelException("Користувач відмінив введення імені файла");
            } else {
                return name;
            }
        }
		public void actionPerformed(ActionEvent e) {
            log.fine("Encode button pressed - launching encode() chain");
            String optionFileName;
            try {
                optionFileName = getFileName();
            } catch (CancelException ce) {
                ce.printStackTrace();
                return;
            }
            String text = textArea.getText();
            log.info("Started encoding '" + text + "' to file '" + optionFileName + "'");
			try {
				String filePath = QrWizard.encode(textArea.getText(), optionFileName);
				log.info("Encoding succesful\n");
				JOptionPane.showMessageDialog(null, "Код успішно створений та знаходиться у файлі " + filePath + ".",
						"Результат:", JOptionPane.INFORMATION_MESSAGE);
			} catch (WriterException | NullPointerException we) {
				JOptionPane.showMessageDialog(null, "Помилка запису у файл (вірогідно, що дані занадто великі)",
						"Помилка!", JOptionPane.ERROR_MESSAGE);
			}
        }
	}

    /**
     * Слухач подій для кнопки 'Розкодувати'
     */
	class DecodeActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
            log.fine("Decode button pressed - launching decode() chain");
			JFileChooser fileopen = new JFileChooser(".");
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Image files", "png", "jpg");
			fileopen.setFileFilter(filter);
			int ret = fileopen.showDialog(null, "Відкрити файл");
			if (ret == JFileChooser.APPROVE_OPTION) {
			    File file = fileopen.getSelectedFile();
			    try {
                    String path = file.getPath();
                    Image img = ImageIO.read(file);
                    Image resizedImg = img.getScaledInstance(200, 200, 200);
                    ImageIcon imgicon = new ImageIcon(resizedImg);
                    String ext = FilenameUtils.getExtension(path);
                    if ( !ext.equals("png") ) {
                        throw new FileExtensionException("Неправильне розширення файла");
                    }
                    log.info("Started decoding file '" + file + "'");
			    	String result = QrWizard.decode(file);
                    JLabel restext = new JLabel(result, SwingConstants.CENTER);
                    restext.setFont(new Font("Calibri", Font.PLAIN, 24));
                    log.info("Decoding succesful. Result: '" + result + "'\n");
					Pattern hyperlinkp = Pattern.compile("(?=[a-zA-Z])([a-zA-Z0-9-_]*[a-zA-Z][a-zA-Z0-9-_]*(?:\\.[a-zA-Z0-9-/?=]+)+)");
					Matcher matcher = hyperlinkp.matcher(result);
			    	if ( matcher.find() ) {
			    	    Object[] options = {"Перейти за посиланням",
                                            "Закрити"};
			    	    int action = JOptionPane.showOptionDialog(mainFrame, restext,
                                "Результат:", JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.QUESTION_MESSAGE, imgicon, options, options[1]);
			    	    if (action == JOptionPane.OK_OPTION) {
			    	        try {
			    	        	String url = matcher.group(1);
			    	            if ( !(url.startsWith("http")) ) {
			    	                url = "http://" + url;
                                }
			    	            Desktop.getDesktop().browse(new URL(url).toURI());
                            } catch (URISyntaxException e1) {
                                e1.printStackTrace();
                            }
                        }
					} else {
			    	    Object[] options = {
			    	            "Закрити"
                        };
                        JOptionPane.showOptionDialog(mainFrame, restext,
                                "Результат:", JOptionPane.YES_OPTION, JOptionPane.PLAIN_MESSAGE,
								imgicon, options, options[0]);
					}
			    } catch (IOException | NotFoundException ioe) {
			    	ioe.printStackTrace();
			    } catch (FileExtensionException fee) {
                    JOptionPane.showMessageDialog(mainFrame,
                            fee.getMessage(),
                            "Помилка!",
                            JOptionPane.ERROR_MESSAGE);
                }
			}
	      }
	}

    /**
     * Помилка, що виникає при невірному розширенні файла
     */
	class FileExtensionException extends Exception {
        FileExtensionException(String message) {
            super(message);
        }
    }

    /**
     * Помилка, що виникає при відміні дії користувачем
     */
    public class CancelException extends Exception {
        CancelException(String s) { super(s); }
    }

}
