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

	private double width,height;
	private double centerX, centerY;
	private double radius_small,radius_big;
	private String base_paint,ball_paint;
	private double smallX, smallY;
	public DoubleProperty elevator, aileron;
	
	public JoyStick(@NamedArg("radius_small") Double radius_small, @NamedArg("radius_big") Double radius_big){
		this.radius_small = radius_small;
		this.radius_big = radius_big;
		this.base_paint="#E3E0E2";
		this.ball_paint="#515151";
		elevator = new SimpleDoubleProperty();
		aileron = new SimpleDoubleProperty();
		Platform.runLater(()->{
		    this.width = getWidth(); 
			this.height = getHeight();
			centerX = width/2;
			centerY = height/2;
			smallX = centerX;
			smallY = centerY;
			redraw();
		});
	}
	
	public void drawCenteredCircle(GraphicsContext g, double x, double y, double r) {
		  x = x-(r/2);
		  y = y-(r/2);
		  g.fillOval(x,y,r,r);
		  g.setFill(Paint.valueOf("#0000"));
		  g.strokeOval(x,y,r,r);
		}
	public void redraw() {		
		GraphicsContext gc = getGraphicsContext2D();
		gc.clearRect(0, 0, width, height);
		gc.setFill(Paint.valueOf(base_paint));
		drawCenteredCircle(gc,centerX,centerY,radius_big);
		gc.setFill(Paint.valueOf(ball_paint));
		drawCenteredCircle(gc, smallX, smallY,radius_small);
	}
	public void setMouseHandler(MouseEvent e) {
		double r = Math.sqrt(Math.pow((centerX  - e.getX()),2) + Math.pow((centerY - e.getY()), 2));
		if(r < radius_big/2){
			smallX = e.getX();
			smallY = e.getY();
		}
		else {
			smallX = (radius_big/2 * (e.getX() - centerX))/r + centerX;
			smallY = (radius_big/2 * (e.getY() - centerY))/r + centerY;
			smallX = Math.round((smallX*100))/100;
			smallY =Math.round((smallY*100))/100;
			
		}
		elevator.setValue((smallY - centerY)*2/radius_big);
		aileron.setValue((smallX - centerX)*2/radius_big);
		redraw();
	}
	
	public void setMouseEventHandlers(){
		this.setOnMousePressed((e)->{setMouseHandler(e);});
		this.setOnMouseDragged((e)-> {setMouseHandler(e);});
		this.setOnMouseReleased((e)->{
			smallX = centerX;
			smallY = centerY;
			elevator.setValue(0);
			aileron.setValue(0);
			redraw();
		});
	}
}
