package edu.normality.smartdashboard.foxacidconfigure;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.normality.smartdashboard.foxacid.FOXACID;
import edu.wpi.first.smartdashboard.gui.StaticWidget;
import edu.wpi.first.smartdashboard.properties.Property;

public class FOXACIDCONFIGURE extends StaticWidget {
	
    public static final String	    NAME	       = "FOXACID " + FOXACID.VERSION + " - Configurer";
	
	public static final int kMinHue = 45,
							kMinSat = 120,
							kMinVal = 35,
							kMaxHue = 100,
							kMaxSat = 255,
							kMaxVal = 255,
							kMinHeight = 30,
							kMinWidth = 45,
							kMinAspect = 275,
							kMaxAspect = 1000,
							kHitDistance = 100;

	public static JSlider minHueSlider;

	public static JSlider minSatSlider;

	public static JSlider minValSlider;

	public static JSlider maxHueSlider;

	public static JSlider maxSatSlider;

	public static JSlider maxValSlider;
	
	public static JSlider minHeightSlider;
	
	public static JSlider minWidthSlider;
	
	public static JSlider minAspectSlider;
	
	public static JSlider maxAspectSlider;
	
	public static JSlider hitDistanceSlider;
	
	public static  JLabel minHueLabel = new JLabel("minHue");
	public static  JLabel minSatLabel = new JLabel("minSat");
	public static  JLabel minValLabel = new JLabel("minVal");
	public static  JLabel maxHueLabel = new JLabel("maxHue");
	public static  JLabel maxSatLabel = new JLabel("maxSat");
	public static  JLabel maxValLabel = new JLabel("maxVal");
	public static  JLabel minHeightLabel = new JLabel("minHeight");
	public static  JLabel minWidthLabel = new JLabel("minWidth");
	public static  JLabel minAspectLabel = new JLabel("minAspect");
	public static  JLabel maxAspectLabel = new JLabel("maxAspect");
	public static  JLabel hitDistanceLabel = new JLabel("hitDistance");
	
	JPanel sliders;	
	
	public void init() {
		sliders = new JPanel();
		JButton jB = new JButton("Reset Values");
		jB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				minHueSlider.setValue(kMinHue);
				minValSlider.setValue(kMinVal);
				minSatSlider.setValue(kMinSat);
				maxHueSlider.setValue(kMaxHue);
				maxValSlider.setValue(kMaxVal);
				maxSatSlider.setValue(kMaxSat);
				minHeightSlider.setValue(kMinHeight);
				minWidthSlider.setValue(kMinWidth);
				minAspectSlider.setValue(kMinAspect);
				maxAspectSlider.setValue(kMaxAspect);
				hitDistanceSlider.setValue(kHitDistance);
			}
		});
		sliders.setLayout(new BoxLayout(sliders, BoxLayout.Y_AXIS));
		JLabel dontTouchThis = new JLabel(
				"<html><center>If you're wondering if you should<br>change these values,<br>you <b>should not</b>.</center>");
		
		minHueSlider = new JSlider(JSlider.HORIZONTAL, 0, 180, kMinHue);
		minSatSlider = new JSlider(JSlider.HORIZONTAL, 0, 255, kMinSat);
		minValSlider = new JSlider(JSlider.HORIZONTAL, 0, 255, kMinVal);
		maxHueSlider = new JSlider(JSlider.HORIZONTAL, 0, 180, kMaxHue);
		maxSatSlider = new JSlider(JSlider.HORIZONTAL, 0, 255, kMaxSat);
		maxValSlider = new JSlider(JSlider.HORIZONTAL, 0, 255, kMaxVal);
		minHeightSlider = new JSlider(JSlider.HORIZONTAL, 0, 500, kMinHeight);
		minWidthSlider = new JSlider(JSlider.HORIZONTAL, 0, 500, kMinWidth);
		minAspectSlider = new JSlider(JSlider.HORIZONTAL, 0, 1000, kMinAspect);
		maxAspectSlider = new JSlider(JSlider.HORIZONTAL, 0, 1000, kMaxAspect);
		hitDistanceSlider = new JSlider(JSlider.HORIZONTAL, 85, 110, kHitDistance);
		sliders.setPreferredSize(new Dimension(200, 640));
		
		minHueSlider.addChangeListener(new ChangeListener() {			
			public void stateChanged(ChangeEvent e) {
				FOXACIDCONFIGURE.minHueLabel.setText("minHue: " + FOXACIDCONFIGURE.minHueSlider.getValue());
			}
		});
		
		minValSlider.addChangeListener(new ChangeListener() {			
			public void stateChanged(ChangeEvent e) {
				FOXACIDCONFIGURE.minValLabel.setText("minVal: " + FOXACIDCONFIGURE.minValSlider.getValue());
			}
		});
		
		minSatSlider.addChangeListener(new ChangeListener() {			
			public void stateChanged(ChangeEvent e) {
				FOXACIDCONFIGURE.minSatLabel.setText("minSat: " + FOXACIDCONFIGURE.minSatSlider.getValue());
			}
		});
		
		maxHueSlider.addChangeListener(new ChangeListener() {			
			public void stateChanged(ChangeEvent e) {
				FOXACIDCONFIGURE.maxHueLabel.setText("maxHue: " + FOXACIDCONFIGURE.maxHueSlider.getValue());
			}
		});
		
		maxSatSlider.addChangeListener(new ChangeListener() {			
			public void stateChanged(ChangeEvent e) {
				FOXACIDCONFIGURE.maxSatLabel.setText("maxSat: " + FOXACIDCONFIGURE.maxSatSlider.getValue());
			}
		});
		
		maxValSlider.addChangeListener(new ChangeListener() {			
			public void stateChanged(ChangeEvent e) {
				FOXACIDCONFIGURE.maxValLabel.setText("maxVal: " + FOXACIDCONFIGURE.maxValSlider.getValue());
			}
		});
		
		minHeightSlider.addChangeListener(new ChangeListener() {			
			public void stateChanged(ChangeEvent e) {
				FOXACIDCONFIGURE.minHeightLabel.setText("minHeight: " + FOXACIDCONFIGURE.minHeightSlider.getValue());
			}
		});
		
		minWidthSlider.addChangeListener(new ChangeListener() {			
			public void stateChanged(ChangeEvent e) {
				FOXACIDCONFIGURE.minWidthLabel.setText("minWidth: " + FOXACIDCONFIGURE.minWidthSlider.getValue());
			}
		});

		minAspectSlider.addChangeListener(new ChangeListener() {			
			public void stateChanged(ChangeEvent e) {
				FOXACIDCONFIGURE.minAspectLabel.setText("minAspect: " + (double)FOXACIDCONFIGURE.minAspectSlider.getValue() / 500d);
			}
		});
		
		maxAspectSlider.addChangeListener(new ChangeListener() {			
			public void stateChanged(ChangeEvent e) {
				FOXACIDCONFIGURE.maxAspectLabel.setText("maxAspect: " + (double)FOXACIDCONFIGURE.maxAspectSlider.getValue() / 200d);
			}
		});
		
		hitDistanceSlider.addChangeListener(new ChangeListener() {			
			public void stateChanged(ChangeEvent e) {
				FOXACIDCONFIGURE.hitDistanceLabel.setText("hitDistance: " + (double)FOXACIDCONFIGURE.hitDistanceSlider.getValue());
			}
		});
		
		FOXACIDCONFIGURE.minHueLabel.setText("minHue: " + FOXACIDCONFIGURE.minHueSlider.getValue());
		FOXACIDCONFIGURE.maxHueLabel.setText("maxHue: " + FOXACIDCONFIGURE.maxHueSlider.getValue());
		FOXACIDCONFIGURE.minSatLabel.setText("minSat: " + FOXACIDCONFIGURE.minSatSlider.getValue());
		FOXACIDCONFIGURE.maxSatLabel.setText("maxSat: " + FOXACIDCONFIGURE.maxSatSlider.getValue());
		FOXACIDCONFIGURE.minValLabel.setText("minVal: " + FOXACIDCONFIGURE.minValSlider.getValue());
		FOXACIDCONFIGURE.maxValLabel.setText("maxVal: " + FOXACIDCONFIGURE.maxValSlider.getValue());
		FOXACIDCONFIGURE.minHeightLabel.setText("minHeight: " + FOXACIDCONFIGURE.minHeightSlider.getValue());
		FOXACIDCONFIGURE.minWidthLabel.setText("minWidth: " + FOXACIDCONFIGURE.minWidthSlider.getValue());
		FOXACIDCONFIGURE.minAspectLabel.setText("minAspect: " + (double)FOXACIDCONFIGURE.minAspectSlider.getValue() / 500d);
		FOXACIDCONFIGURE.maxAspectLabel.setText("maxAspect: " + (double)FOXACIDCONFIGURE.maxAspectSlider.getValue() / 200d);
		FOXACIDCONFIGURE.hitDistanceLabel.setText("hitDistance: " + FOXACIDCONFIGURE.hitDistanceSlider.getValue());

		sliders.add(jB);
		sliders.add(Box.createRigidArea(new Dimension(0, 20)));
		sliders.add(dontTouchThis);
		sliders.add(Box.createRigidArea(new Dimension(0, 50)));
		sliders.add(minHueLabel);
		sliders.add(minHueSlider);
		sliders.add(minSatLabel);
		sliders.add(minSatSlider);
		sliders.add(minValLabel);
		sliders.add(minValSlider);
		sliders.add(maxHueLabel);
		sliders.add(maxHueSlider);
		sliders.add(maxSatLabel);
		sliders.add(maxSatSlider);
		sliders.add(maxValLabel);
		sliders.add(maxValSlider);
		sliders.add(minHeightLabel);
		sliders.add(minHeightSlider);
		sliders.add(minWidthLabel);
		sliders.add(minWidthSlider);
		sliders.add(minAspectLabel);
		sliders.add(minAspectSlider);
		sliders.add(maxAspectLabel);
		sliders.add(maxAspectSlider);
		sliders.add(hitDistanceLabel);
		sliders.add(hitDistanceSlider);
		
		
		add(sliders);
	}

	@Override
	public void propertyChanged(Property arg0) {
		// TODO Auto-generated method stub
		
	}	
	
	public static int getMinHue() {
		return minHueSlider.getValue();
	}
	
	public static int getMinSat() {
		return minSatSlider.getValue();
	}
	
	public static int getMinVal() {
		return minValSlider.getValue();
	}
	
	public static int getMaxHue() {
		return maxHueSlider.getValue();
	}
	
	public static int getMaxSat() {
		return maxSatSlider.getValue();
	}
	
	public static int getMaxVal() {
		return maxValSlider.getValue();
	}
	
	public static int getMinHeight() {
		return minHeightSlider.getValue();
	}
	
	public static int getMinWidth() {
		return minWidthSlider.getValue();
	}
	
	public static double getMinAspect() {
		return (double)minAspectSlider.getValue() / 500d;
	}
	
	public static double getMaxAspect() {
		return (double)maxAspectSlider.getValue() / 200d;
	}
	
	public static int getHitDistance() {
		return hitDistanceSlider.getValue();
	}
}