package roguelike.Screens;

import java.awt.event.KeyEvent;

import asciiPanel.AsciiPanel;
import roguelike.Mob.Player;
import roguelike.utility.Point;
import squidpony.squidmath.Bresenham;
import squidpony.squidmath.Coord;

public class TargetingScreen implements Screen{

	public Point currentDirection = new Point(0, 0);
	protected Player player;
	protected String caption;
	protected int x, y;
	protected Coord[] targetLine;
	
	public TargetingScreen(Player player, String caption) {
		this.player = player;
		this.x = player.x;
		this.y = player.y;
		this.caption = caption;
		targetLine = Bresenham.line2D_(player.x, player.y, player.x, player.y);
	}
	
	@Override
	public void displayOutput(AsciiPanel terminal) {
		for(int i = 0; i < targetLine.length; i++) {
			if(targetLine[i].x == player.x && targetLine[i].y == player.y && targetLine.length > 1) {
				terminal.write(player.glyph(), player.x, player.y, player.color());
			}
			else if(player.level().isGround(targetLine[i].x, targetLine[i].y)){
				terminal.write('X', targetLine[i].x, targetLine[i].y, AsciiPanel.brightYellow);
			} 
			else {
				terminal.write('X', targetLine[i].x, targetLine[i].y, AsciiPanel.brightRed);
			}
		}
		
	}

	public void updateTargetLine(Point direction) {
		if(x + direction.x >= player.level().width || x + direction.x < 0) {
                x += 0;
            }
            else if(y + direction.y >= player.level().height || y + direction.y < 0) {
                y += 0;
            }
            else {
                x += direction.x;
                y += direction.y;
            }
            Coord[] tempLine = Bresenham.line2D_(player.x, player.y, x, y);
            if(tempLine.length > 1) {
                targetLine = Bresenham.line2D_(tempLine[1].x, tempLine[1].y, x, y);
            }
            else{
                targetLine = Bresenham.line2D_(player.x, player.y, x, y);
        }
	}

	@Override
	public Screen respondToUserInput(KeyEvent key) {
		
		switch(key.getKeyCode()) {
			case KeyEvent.VK_NUMPAD4:{ currentDirection = Point.WEST; updateTargetLine(currentDirection); break;}
			case KeyEvent.VK_NUMPAD6:{ currentDirection = Point.EAST; updateTargetLine(currentDirection); break;}
			case KeyEvent.VK_NUMPAD8:{ currentDirection = Point.NORTH; updateTargetLine(currentDirection); break;}
			case KeyEvent.VK_NUMPAD2:{ currentDirection = Point.SOUTH; updateTargetLine(currentDirection); break;}
			case KeyEvent.VK_NUMPAD7:{ currentDirection = Point.NORTH_WEST; updateTargetLine(currentDirection); break;}
			case KeyEvent.VK_NUMPAD9:{ currentDirection = Point.NORTH_EAST; updateTargetLine(currentDirection);	break;}
			case KeyEvent.VK_NUMPAD1:{ currentDirection = Point.SOUTH_WEST;	updateTargetLine(currentDirection);	break;}
			case KeyEvent.VK_NUMPAD3:{ currentDirection = Point.SOUTH_EAST;	updateTargetLine(currentDirection);	break;}
			case KeyEvent.VK_NUMPAD5:{ currentDirection = Point.WAIT; updateTargetLine(currentDirection); break;}
			case KeyEvent.VK_ENTER:{ selectCoordinate(targetLine); return null;}
			case KeyEvent.VK_ESCAPE:{ return null; }
			default:{ currentDirection = Point.WAIT; updateTargetLine(currentDirection);}
		}
		
		return this;
	}
	
	public boolean isAcceptable(Coord target) { return true; }
	public void selectCoordinate(Coord[] targetLine) { }
	
}