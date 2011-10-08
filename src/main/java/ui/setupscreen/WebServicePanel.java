package ui.setupscreen;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.SocketException;
import java.util.Hashtable;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import webservice.Webservice;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class WebServicePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private Webservice webservice;
	private JLabel image;
	
	public WebServicePanel(Webservice service) {
		super(new GridBagLayout());
		this.webservice = service;
		
		com.google.zxing.Writer writer = new QRCodeWriter();

		
		this.setMinimumSize(new Dimension(600, 0));

		this.setPreferredSize(new Dimension(600, 0));

		final JPanel webServicePanel = this;
		webServicePanel.setBorder(BorderFactory.createTitledBorder("Web Interface"));
		
		JButton startButton = new JButton("Start");
		JButton stopButton = new JButton("Stop");
		final JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		ActionListener urlButton = new ActionListener() {
			@Override
			public synchronized void actionPerformed(ActionEvent e) {
				panel.removeAll();
				try {
					webservice.start();
					generateUrlButtons(panel);

					
				} catch (Exception e1) {
					panel.add(new JLabel(e1.getMessage()));
				}				
				
			}
		};
		try {
			generateUrlButtons(panel);
		} catch (Exception e1) {
			panel.add(new JLabel(e1.getMessage()));
		}	
		startButton.addActionListener(urlButton);
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.removeAll();
				image.setIcon(null);
				revalidate();
				validate();
				try {
					webservice.stop();		
					
				} catch (Exception e1) {
					panel.add(new JLabel(e1.getMessage()));
				}				
				
			}
		});

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.add(startButton);
		buttonsPanel.add(stopButton);
		
		webServicePanel.add(buttonsPanel, gbc);
		image = new JLabel();
		webServicePanel.add(image, gbc);
		webServicePanel.add(panel);
		
	}
	
	private void generateUrlButtons(final JPanel panel) throws SocketException {
		panel.removeAll();
		GroupLayout layout = new GroupLayout(panel);		
		panel.setLayout(layout);
		
		List<String> interfaces = webservice.getInterfaces();
		
		GroupLayout.SequentialGroup hozGroup = layout.createSequentialGroup();
		GroupLayout.SequentialGroup vertGroup = layout.createSequentialGroup();		
		GroupLayout.ParallelGroup vertGroup1 = layout.createParallelGroup();
		GroupLayout.ParallelGroup vertGroup2 = layout.createParallelGroup();
		
		vertGroup.addGroup(vertGroup1);
		vertGroup.addGroup(vertGroup2);
		
		for (final String s : interfaces) {
			JButton link = new JButton(s);
			link.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					java.net.URI uri;
					try {
						uri = new java.net.URI(s);
						java.awt.Desktop.getDesktop().browse(uri);
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null, ex.getMessage());
					}
					
				}
			});
			
			JButton qrButton = new JButton("QRCode");
			qrButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					try {
						showURL(s);
					} catch (WriterException ex) {
						JOptionPane.showMessageDialog(null, ex.getMessage());
					}
				}
			});
			vertGroup1.addComponent(link);
			vertGroup2.addComponent(qrButton);
			hozGroup.addGroup(layout.createParallelGroup().addComponent(link).addComponent(qrButton));
		}
		layout.setVerticalGroup(hozGroup);
		layout.setHorizontalGroup(vertGroup);
		this.invalidate();
		((JFrame)(Object)this.getRootPane()).pack();
		
		revalidate();
		validate();
	}
	
	private void showURL(String url) throws WriterException {
		QRCodeWriter qrcw = new QRCodeWriter();
		BitMatrix bm = qrcw.encode(url, BarcodeFormat.QR_CODE, 200, 200, new Hashtable<String,String>());
		BufferedImage bi = MatrixToImageWriter.toBufferedImage(bm);
		

		image.setIcon(new ImageIcon(bi));
	}
	
}
