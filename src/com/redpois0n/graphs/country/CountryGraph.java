package com.redpois0n.graphs.country;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import com.redpois0n.DesignMode;

@SuppressWarnings("serial")
public class CountryGraph extends JComponent {

	private final List<Country> countries = new ArrayList<Country>();

	private ICountryColors colors;

	private boolean showIso2 = true;
	private boolean drawNumber = true;
	private boolean isActive = true;

	public CountryGraph(ICountryColors colors) {
		this.colors = colors;
	}

	@Override
	public void paintComponent(Graphics g) {
		if (DesignMode.DESIGNERMODE) {
			return;
		}
		
		// draw inner color
		g.setColor(colors.getInnerFillColor());
		g.fillRect(1, 1, this.getWidth() - 1, this.getHeight() - 1);

		// draw background rectangles
		g.setColor(colors.getBorderColor());
		g.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);

		int max = 0;

		// sort countries
		Collections.sort(countries, new Comparator<Country>() {
			public int compare(Country country, Country country1) {
				return country.getNumber() - country1.getNumber();
			}
		});

		// get highest
		for (int i = 0; i < countries.size(); i++) {
			Country country = countries.get(i);

			if (country.getNumber() > max) {
				max = country.getNumber();
			}
		}
		
		

		// draw lines and flags
		int pos = 0;
		
		for (int i = countries.size() - 1; i > 0; i--) {
			Country country = countries.get(i);

			int value = (int) (((float) country.getNumber() / (float) max) * this.getHeight()) - 20;

			int x = 15 + pos++ * 20;

			if (value < 20) {
				value += 20;
			}

			g.setColor(getMainColor(country.getFlag()));
			g.fillRect(x, this.getHeight() - value - 1, 10, this.getHeight());

			g.drawImage(country.getFlag().getImage(), x - 3, this.getHeight() - value - 14, country.getFlag().getIconWidth(), country.getFlag().getIconHeight(), null);

			if (drawNumber) {
				AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians(-90));
				Font f = new Font("Arial", Font.BOLD, 12);
				g.setFont(value < 20 ? f : f.deriveFont(at));
					
				
				g.setColor(colors.getTextColor());
				g.drawString(country.getNumber() + "", value < 20 ? x + 2 : x + 10, value < 20 ? this.getHeight() - value - 17: this.getHeight() - value + 25);
			}
		}

		onUpdate(countries, 15 + countries.size() * 20);
	}

	public void onUpdate(List<Country> sortedList, int recommendedX) {

	}
	
	public void setActive(boolean b) {
		isActive = b;
	}
	
	public boolean isActive() {
		return isActive;
	}

	public void add(Country country) {
		for (int i = 0; i < countries.size(); i++) {
			Country old = countries.get(i);
			if (old.equals(country)) {
				old.setNumber(country.getNumber());
				return;
			}
		}
		
		countries.add(country);
	}

	public void remove(Country country) {
		countries.remove(country);
	}

	public boolean isShowIso2() {
		return showIso2;
	}

	public void setShowIso2(boolean showIso2) {
		this.showIso2 = showIso2;
	}

	public boolean isDrawNumber() {
		return drawNumber;
	}

	public void setDrawNumber(boolean drawNumber) {
		this.drawNumber = drawNumber;
	}

	public void clear() {
		countries.clear();
	}
	
	public static Color getMainColor(ImageIcon icon) {
		BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_RGB);

		image.createGraphics();
		image.getGraphics().drawImage(icon.getImage(), 0, 0, null);
		int clr = image.getRGB(image.getWidth() / 2, image.getHeight() / 2);

		image.getGraphics().dispose();

		return new Color(clr);
	}

}