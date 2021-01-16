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

	public int[][] mapHeights;
	double area;
	public StringProperty solution;
	public BooleanProperty serverUp;
	public int initial_X;
	public int initial_Y;
	public double initial_Lat;
	public double initial_Long;
	public DoubleProperty dest_X, dest_Y;
	public DoubleProperty airplane_X, airplane_Y;
	public DoubleProperty heading;
	
	private Image airplaneIcon;
	private Image destIcon;
	private Image routeIcon;
	private Image gridSnapshot;
	
	public MapGrid() {
		this.airplane_X = new SimpleDoubleProperty();
		this.airplane_Y = new SimpleDoubleProperty();
		this.heading=new SimpleDoubleProperty();		
		this.dest_X = new SimpleDoubleProperty(); 
		this.dest_Y = new SimpleDoubleProperty();
		this.solution = new SimpleStringProperty();
		this.serverUp = new SimpleBooleanProperty();
		
	}
	
	public double recSizeHeight() {return this.getHeight()  / this.mapHeights.length;}
	public double recSizeWidth() { return this.getWidth()  / this.mapHeights[0].length;}
	
	public void initIcons(Image planeImage, Image destImage, Image routeImage) {
		this.airplaneIcon = planeImage;
		this.destIcon = destImage;
		this.routeIcon = routeImage;
	}
	
	public void setMapHeights(int[][] mapHeights, double area,double initialLat,double initialLong) {
		this.mapHeights = mapHeights;
		this.initial_Lat=initialLat;
		this.initial_Long=initialLong;
		this.area = area;
		
		redraw();
	}
	
	// find max height to define our heights scale.
	private double initColorScale() {
		int max = 0;
		for (int i = 0; i < mapHeights.length; i++) {
			for (int j = 0; j < mapHeights[0].length; j++) {
				int value = mapHeights[i][j];
				if (value > max) {
					max = value;
				}
			}
		}
		return ((double) max / 255);
	}

	// normalize cell color.
	private String scaleCellColor(int height, double scale) {
		int scaledVal = (int) (height / scale);
		String red = Integer.toHexString(255 - scaledVal);
		String green = Integer.toHexString(scaledVal);
		
		if (red.length() < 2)
			red = "0" + red;
		
		if (green.length() < 2)
			green = "0" + green;

		return "#" + red + green + "00";
	}
	
	public void drawIcon(GraphicsContext gc, Image im, double x, double y ,double w ,double h ,double d) {
		gc.save();
		gc.translate(x, y);	
		gc.rotate(d);
		gc.translate(-x, -y);
		gc.drawImage(im, x - w/2, y - h/2, w, h);
		gc.restore();
	}
	 
	public void redraw() {
		if (mapHeights != null) {
			GraphicsContext gc = getGraphicsContext2D();
			
			double w = this.recSizeWidth();
			double h = this.recSizeHeight();
			
			gc.clearRect(0, 0, this.getWidth(), this.getHeight());
			
			if(gridSnapshot == null) {
				double scale = initColorScale();

				for (int row = 0; row < mapHeights.length; row++) {
					for (int col = 0; col < mapHeights[0].length; col++) {
						String color = scaleCellColor(mapHeights[row][col], scale);
						gc.setFill(Paint.valueOf(color));
						gc.fillRect(col * w, row * h, w, h);
					}
				}
				
				gridSnapshot = this.snapshot(null, null);
			}
			
			gc.drawImage(gridSnapshot, 0, 0);
			int imageSize = 5;
			
			drawSolutionPath();
			
			drawIcon(gc,destIcon ,dest_X.doubleValue(), dest_Y.doubleValue(), this.recSizeWidth() * imageSize * 1.5 , this.recSizeHeight() * imageSize * 1.5, 0);
			drawIcon(gc,airplaneIcon ,airplane_X.get(),airplane_Y.get(),this.recSizeWidth() * imageSize * 2 , this.recSizeHeight() * imageSize * 2, heading.get());
		}
	}
	
	public void drawSolutionPath()
	{
		GraphicsContext gc = getGraphicsContext2D();
		
		double w=this.recSizeWidth();
		double h=this.recSizeHeight();
		
		String sol = solution.get();
		
		if(sol != null && !sol.isEmpty()) {
			int dest_X = this.initial_X;
			int dest_Y = this.initial_Y;
			String routInstructions[] = sol.split(",");
			
			int numsToAvg = 5;
			double avg = 0;
			
			for(int i = 0; i < routInstructions.length ; i++)
			{
				switch(routInstructions[i]) {
				  case "Up":
				    avg +=0;
				    dest_Y--;
				    break;
				  case "Down":
					  avg += 180;
					  dest_Y++;
					    break;
				  case "Right":
					  avg +=90;
					  dest_X++;
				    break;
				  case "Left":
					  avg += 270;
					  dest_X--;
					break;
				}
				
				if(i % numsToAvg == numsToAvg - 1) {
					drawIcon(gc,routeIcon,w * dest_X, h * dest_Y, w * 2, h * 2,(int) avg/numsToAvg);
					avg = 0;
				}
			}
		}
	}
}