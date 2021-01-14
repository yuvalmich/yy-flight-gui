package view;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;

public class MapGrid extends Canvas {

	public int[][] mapData;
	double area;
	Image planeImage;
	Image destinationImage;
	Image arrowImage;
	Image gridSnapshot;
	public int startXcord;
	public int startYcord;
	public double initialLat;
	public double initialLong;
	public  DoubleProperty destinationXcord, destinationYcord;
	public DoubleProperty planeXcord, planeYcord;
	public DoubleProperty heading;
	public StringProperty solution;
	public BooleanProperty serverUp;

	
	public MapGrid() {
		this.planeXcord = new SimpleDoubleProperty();
		this.planeYcord = new SimpleDoubleProperty();
		this.heading=new SimpleDoubleProperty();		
		this.destinationXcord = new SimpleDoubleProperty();//TODO: make the conversion on the model side so we wont have to mess with it here. 
		this.destinationYcord = new SimpleDoubleProperty();
		this.solution = new SimpleStringProperty();
		this.serverUp = new SimpleBooleanProperty();
		
	}
	public void setImages(Image planeImage, Image destinationImage, Image arrowImage) {
		this.planeImage = planeImage;
		this.destinationImage = destinationImage;
		this.arrowImage = arrowImage;
	}
	
	
	public double recSizeHeight() {return this.getHeight()  / this.mapData.length;}
	public double recSizeWidth() { return this.getWidth()  / this.mapData[0].length;}
	
	public void setMapData(int[][] mapData, double area,double initialLat,double initialLong) {
		this.mapData = mapData;
		this.initialLat=initialLat;
		this.initialLong=initialLong;
		this.area = area;
		redraw();
	}
	private double calcColorScale() {
		int max = 0;// finding max height to define our heights scale.
		for (int i = 0; i < mapData.length; i++) {
			for (int j = 0; j < mapData[0].length; j++) {
				int value = mapData[i][j];
				if (value > max) {
					max = value;
				}
			}
		}
		return ((double) max / 255);
	}

	// normalizes cell color given the scale range and the cell height.
	private String calcCellColor(int height, double scale) {
		int normalized = (int) (height / scale);
		String red = Integer.toHexString(255 - normalized);
		String green = Integer.toHexString(normalized);
		if (red.length() < 2)
			red = "0" + red;
		if (green.length() < 2)
			green = "0" + green;

		String color = "#" + red + green + "00";
		return color;
	}
	
	public void drawImage(GraphicsContext gc, Image im, double x, double y ,double w ,double h ,double d) {
		gc.save();
		gc.translate(x, y);	
		gc.rotate(d);
		gc.translate(-x, -y);
		gc.drawImage(im, x - w/2, y - h/2, w, h);
		gc.restore();
	}
	 
	public void redraw() {
		if (mapData != null) {
			GraphicsContext gc = getGraphicsContext2D();
			//clearing the whole canvas
			double w = this.recSizeWidth();
			double h = this.recSizeHeight();
			gc.clearRect(0, 0, this.getWidth(), this.getHeight());
				if(gridSnapshot == null) {
				double scale = calcColorScale();

				//coloring the map-grid
				for (int i = 0; i < mapData.length; i++) {
					for (int j = 0; j < mapData[0].length; j++) {
						String color = calcCellColor(mapData[i][j], scale);
						gc.setFill(Paint.valueOf(color));
						gc.fillRect(j * w, i * h, w, h);
					}
				}
				gridSnapshot = this.snapshot(null, null);
			}
			gc.drawImage(gridSnapshot, 0, 0);
			int imgSize = 5;
			drawSolutionPath();
			drawImage(gc,planeImage ,planeXcord.get(),planeYcord.get(),this.recSizeWidth() * imgSize , this.recSizeHeight() * imgSize ,heading.get());
			drawImage(gc,destinationImage ,destinationXcord.doubleValue(), destinationYcord.doubleValue(), this.recSizeWidth() * imgSize , this.recSizeHeight() * imgSize, 0);
			}
		}
	
	public void drawSolutionPath()
	{
		GraphicsContext gc = getGraphicsContext2D();
		double w=this.recSizeWidth();
		double h=this.recSizeHeight();
		String sol = solution.get();
		if(sol!=null && sol != "") {
			int desXDataCord = this.startXcord;
			int desYDataCord = this.startYcord;
			String n[] = sol.split(",");
			int numsToAvg = 5;
			double avg = 0;
			
			for(int i = 0; i < n.length ; i++)
			{
				switch(n[i]) {
				  case "Up":
				    avg +=0;
				    desYDataCord--;
				    break;
				  case "Right":
					  avg +=90;
					  desXDataCord++;
				    break;
				  case "Down":
					  avg += 180;
					  desYDataCord++;
					    break;
				  case "Left":
					  avg += 270;
					  desXDataCord--;
					break;
				
				}
				if(i % numsToAvg == numsToAvg - 1) {
					drawImage(gc,arrowImage,w * desXDataCord, h * desYDataCord, w * 2, h * 2,(int) avg/numsToAvg);
					avg = 0;
				}
			}
				
		}
		
	}
	


}