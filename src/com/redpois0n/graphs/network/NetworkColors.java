package com.redpois0n.graphs.network;

import java.awt.Color;

public class NetworkColors implements INetworkColors {

	@Override
	public Color getInnerFillColor() {
		return Color.black;
	}

	@Override
	public Color getBorderColor() {
		return Color.white;
	}

	@Override
	public Color getDownloadColor() {
		return new Color(0x0000FF);
	}

	@Override
	public Color getUploadColor() {
		return new Color(0x00FF00);
	}
	
}
