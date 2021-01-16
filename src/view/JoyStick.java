package view;

import javafx.scene.paint.Paint;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;

public class JoyStick extends Canvas {
	private double width, height, outerX, outerY, innerRadius, outerRadius, innerX, innerY;
	private String background_color,innerRadiusColor;
	public DoubleProperty elevator, aileron;
	
	public JoyStick(@NamedArg("inner_radius") Double inner_radius, @NamedArg("outer_radius") Double outer_radius){
		this.innerRadius = inner_radius;
		this.outerRadius = outer_radius;
		
		this.background_color = "#66ccff";
		this.innerRadiusColor = "#4e4e4e";
		
		elevator = new SimpleDoubleProperty();
		aileron = new SimpleDoubleProperty();
		
		Platform.runLater(()->{
		    this.width = getWidth(); 
			this.height = getHeight();
			outerX = width / 2;
			outerY = height / 2;
			innerX = outerX;
			innerY = outerY;
			redraw();
		});
	}
	
	public void drawCenteredCircle(GraphicsContext g, double x, double y, double r) {
		  x -= r / 2;
		  y -= r / 2;
		  g.fillOval(x, y, r, r);
		  g.setFill(Paint.valueOf("#000000"));
		  g.strokeOval(x, y, r, r);
	}
	
	public void redraw() {		
		GraphicsContext gc = getGraphicsContext2D();
		
		gc.clearRect(0, 0, width, height);
		
		gc.setFill(Paint.valueOf(background_color));
		drawCenteredCircle(gc, outerX, outerY, outerRadius);
		
		gc.setFill(Paint.valueOf(innerRadiusColor));
		drawCenteredCircle(gc, innerX, innerY, innerRadius);
	}
	public void setMouseHandler(MouseEvent e) {
		double r = Math.sqrt(Math.pow((outerX  - e.getX()),2) + Math.pow((outerY - e.getY()), 2));
		
		if(r < outerRadius/4){
			innerX = e.getX();
			innerY = e.getY();
		}
		else {
			innerX = (outerRadius / 4 * (e.getX() - outerX)) / r + outerX;
			innerY = (outerRadius / 4 * (e.getY() - outerY)) / r + outerY;
			innerX = Math.round((innerX*100))/100;
			innerY =Math.round((innerY*100))/100;
			
		}
		
		elevator.setValue((innerY - outerY) * 2 /outerRadius);
		aileron.setValue((innerX - outerX) * 2 /outerRadius);
		redraw();
	}
	
	public void setMouseEventHandlers(){
		this.setOnMousePressed((e)->{setMouseHandler(e);});
		this.setOnMouseDragged((e)-> {setMouseHandler(e);});
		this.setOnMouseReleased((e)-> {
			innerX = outerX;
			innerY = outerY;
			elevator.setValue(0);
			aileron.setValue(0);
			redraw();
		});
	}
}
