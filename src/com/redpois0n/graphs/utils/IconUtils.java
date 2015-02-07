package com.redpois0n.graphs.utils;

import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

public class IconUtils {

	private static final Map<String, ImageIcon> CACHE = new HashMap<String, ImageIcon>();

	public static final ImageIcon NO_ICON;

	static {
		NO_ICON = getIcon("404");
	}

	public static ImageIcon getIcon(String path) {
		ImageIcon icon = null;

		if (CACHE.containsKey(path)) {
			icon = CACHE.get(path);
		} else {
			try {
				icon = new ImageIcon(IconUtils.class.getResource("/icons/" + path + ".png"));
			} catch (Exception e) {
				e.printStackTrace();
				icon = NO_ICON;
			}

			CACHE.put(path, icon);
		}

		return icon;
	}

}