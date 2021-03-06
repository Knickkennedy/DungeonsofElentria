package roguelike.level;

import java.awt.Color;
import java.util.*;
import roguelike.utility.*;
import roguelike.items.*;
import roguelike.mob.BaseEntity;
import roguelike.mob.Player;

public class Level{
	public Tile[][] map;
	public char[][] pathMap;
	public Point stairsDown;
	public Point stairsUp;
	public int width;
	public int height;
	public int dangerLevel;
	public String levelID;
	public int levelNumber;
	public Point start;
	public List <Point> frontier = new ArrayList <Point> ();
	public List <Point> deadEnds = new ArrayList <Point> ();
	public List <Point> potentialDoors = new ArrayList <Point> ();
	public List <Point> connections = new ArrayList <Point> ();
	public List <Room> rooms = new ArrayList <Room> ();
	public List <Point> cTR = new ArrayList<Point> ();
	public List <BaseEntity> mobs = new ArrayList <BaseEntity> ();
	public List <Point> extraDoors = new ArrayList <Point> ();
	public List <Item> items = new ArrayList <Item> ();
	public int maxRoomSize;
	public int minRoomSize;
	public int numRoomTries;
	public Player player;
	public boolean[][] roomFlag;
	public boolean[][] connected;
	public boolean[][] revealed;
	

	public Level(int width, int height){
		this.width = width;
		this.height = height;
		pathMap = new char[width][height];
		this.levelNumber = 0;
		map = new Tile[width][height];
		this.minRoomSize = 3;
		this.maxRoomSize = 7;
		this.numRoomTries = 100;
		this.connected = new boolean[width][height];
		this.roomFlag = new boolean[width][height];
		this.revealed = new boolean[width][height];
		this.levelID = "Dungeon Floor";
		}

	public Level(Tile[][] map, int screenWidth, int mapHeight, String levelID){
		this.width = screenWidth;
		this.height = mapHeight;
		pathMap = new char[width][height];
		this.levelNumber = 0;
		this.map = map;
		this.revealed = new boolean[width][height];
		this.levelID = levelID;
		stairsUp = findStairsUp();
		stairsDown = findStairsDown();
		setPathFinding();
	}

	public void setDangerLevel(int depth){ this.dangerLevel = depth; }
	
	public void setPlayer(Player player){
		this.player = player;
	}
	
	public Player getPlayer(){
		return this.player;
	}

	public boolean isWall(Point p){
		return map[p.x][p.y] == Tile.WALL || map[p.x][p.y] == Tile.PERM_WALL;
	}

	public void remove(BaseEntity otherEntity){
		mobs.remove(otherEntity);
	}
	
	public void update(){
		List <BaseEntity> toUpdate = new ArrayList <> (mobs);
		for(BaseEntity bE : toUpdate){
			bE.update();
		}
	}

	public BaseEntity checkForMob(int x, int y){
		for(BaseEntity bE : mobs){
			if(bE.x == x && bE.y == y){ return bE; }
		}
		return null;
	}
	
	public Item checkItems(int x, int y){
		for(Item item : items){
			if(item.x == x && item.y == y){
				return item;
			}
		}
		return null;
	}

	public void removeItem(Item item){
		items.remove(item);
	}
	
	public char glyph(int x, int y){
		BaseEntity Entity = checkForMob(x, y);
		Item item = checkItems(x, y);
		if(Entity != null){
			return Entity.glyph();
		}
		if(item != null){
			return item.getGlyph();
		}
		return tile(x, y).glyph();
		}
	
	public char baseGlyph(int x, int y) {
		return tile(x, y).glyph();
	}
	
	public boolean isGround(int x, int y){
		return !(map[x][y] == Tile.DOOR_CLOSED || map[x][y] == Tile.WALL || map[x][y] == Tile.PERM_WALL || map[x][y] == Tile.MOUNTAIN);
	}
	
	public Color color(int x, int y){
		BaseEntity Entity = checkForMob(x, y);
		Item item = checkItems(x, y);
		if(Entity != null){
			return Entity.color();
		}
		if(item != null){
			return item.getColor();
		}
		return tile(x, y).color();
	}
	
	public Color baseColor(int x, int y) {
		return tile(x, y).color();
	}
	
	public Level buildStandardLevel(){
		initializeMap();
		placeRoom();
		placeRooms();
		startMaze();
		findConnections();
		placeAllDoors();
		removeAllDeadEnds();
		placeStairs();
		setPathFinding();
		return this;}
	
	public void setPathFinding() {
		for(int x = 0; x < this.width; x++) {
			for(int y = 0; y < this.height; y++) {
				pathMap[x][y] = map[x][y].glyph();
			}
		}
	}

	public Point findStairsUp(){
		for(int x = 0; x < this.width; x++) {
			for(int y = 0; y < this.height; y++) {
				if(tile(x, y).glyph() == '<'){
					Point newPoint = new Point(x, y);
					return newPoint;
				}
			}
		}
		return null;
	}

	public Point findStairsDown(){
		for(int x = 0; x < this.width; x++) {
			for(int y = 0; y < this.height; y++) {
				if(tile(x, y).glyph() == '>'){
					Point newPoint = new Point(x, y);
					return newPoint;
				}
			}
		}
		for(int x = 0; x < this.width; x++) {
			for(int y = 0; y < this.height; y++) {
				if(tile(x, y).glyph() == '*'){
					Point newPoint = new Point(x, y);
					return newPoint;
				}
			}
		}
		return null;
	}

	public void removeAllDeadEnds(){
		for(int i = 0; i < 100; i++){
			removeDeadEnds();
		}
	}
	
	public boolean isClosedDoor(int x, int y){
		return map[x][y] == Tile.DOOR_CLOSED;
	}

	public void addAtSpecificLocation(Item item, int x, int y){
		item.x = x;
		item.y = y;
		items.add(item);
	}
	
	public void newEntityAtSpecificLocation(BaseEntity entity, int x, int y){
		entity.x = x;
		entity.y = y;
		entity.setLevel(this);
		entity.getAi().initializePathFinding();
		mobs.add(entity);
	}
	
	public boolean hasItemAlready(int x, int y){
		for(Item itemToCompare : items){
			if(itemToCompare.x == x && itemToCompare.y == y){
				return true;
			}
		}
		return false;
	}
	
	public void newEntityAtEmptyLocation(BaseEntity entity){
		int x;
		int y;
		do{
			x = RandomGen.rand(0, width - 1);
			y = RandomGen.rand(0, height - 1);
		}
		while(!isGround(x, y) || checkForMob(x,y) != null);
		
		entity.x = x;
		entity.y = y;
		entity.setLevel(this);
		entity.getAi().initializePathFinding();
		mobs.add(entity);
	}
	
	public void addAtUpStaircase(BaseEntity entity){
		entity.x = stairsUp.x;
		entity.y = stairsUp.y;
		entity.setLevel(this);
		entity.getAi().initializePathFinding();
		mobs.add(entity);
	}
	
	public void addAtDownStaircase(BaseEntity entity){
		entity.x = stairsDown.x;
		entity.y = stairsDown.y;
		entity.setLevel(this);
		entity.getAi().initializePathFinding();
		mobs.add(entity);
	}
	
	public void placeStairs(){
		Collections.shuffle(rooms);
		Room upstairs = rooms.get(0);
		Room downstairs = rooms.get(rooms.size() - 1);
		int x1 = RandomGen.rand(upstairs.x1 + 1, upstairs.x2 - 1);
		int y1 = RandomGen.rand(upstairs.y1 + 1, upstairs.y2 - 1);
		int x2 = RandomGen.rand(downstairs.x1 + 1, downstairs.x2 - 1);
		int y2 = RandomGen.rand(downstairs.y1 + 1, downstairs.y2 - 1);
		map[x1][y1] = Tile.STAIRS_UP;
		map[x2][y2] = Tile.STAIRS_DOWN;
		stairsUp = new Point(x1, y1);
		stairsDown = new Point(x2, y2);
	}

	public void initializeMap(){
		for (int x = 0; x < this.width; x++){
			for(int y = 0; y < this.height; y++){
				map[x][y] = Tile.WALL;
				connected[x][y] = false;
				revealed[x][y] = false;
			}
		}
	}

	public void placeRooms(){
		for(int i = 0; i < numRoomTries; i++){
			placeRoom();
		}
	}

	public void placeAllDoors(){
		Room tempRoom = rooms.get(RandomGen.rand(0, rooms.size() - 1));
		floodFill(tempRoom.centerX, tempRoom.centerY);
		while(!connections.isEmpty()){
			findDoors();
			placeDoor();
			createExtraDoors();
			removeExtraConnectors();
		}
	}
	
	public void createExtraDoors(){
		Collections.shuffle(extraDoors);
		for(int i = 0; i < RandomGen.rand(1, 3); i++){
			Point check = extraDoors.get(RandomGen.rand(0, extraDoors.size() - 1));
			if(!hasDoorNeighbor(check)){
				map[check.x][check.y] = Tile.DOOR_CLOSED;
			}
		}
		extraDoors.clear();
	}

	public boolean hasDoorNeighbor(Point p){
		for(Point direction : Point.cardinal){
			if(tile(p.getNeighbor(direction)) == Tile.DOOR_CLOSED) return true;
		}
		return false;
	}

	public void placeDoor(){
		Point door = potentialDoors.get(RandomGen.rand(0, potentialDoors.size() - 1));
		while (hasDoorNeighbor(door)) {
			door = potentialDoors.get(RandomGen.rand(0, potentialDoors.size() - 1));
		}
		map[door.x][door.y] = Tile.DOOR_CLOSED;
		floodFill(door.x, door.y);
		extraDoors.addAll(potentialDoors);
		potentialDoors.clear();
	}
	public void findDoors(){
		Collections.shuffle(connections);
		for(Point p : connections){
			if((connected[p.x - 1][p.y]) && (!connected[p.x + 1][p.y])){
				potentialDoors.add(p);
			}
			if((connected[p.x + 1][p.y]) && (!connected[p.x - 1][p.y])){
				potentialDoors.add(p);
			}
			if((connected[p.x][p.y - 1]) && (!connected[p.x][p.y + 1])){
				potentialDoors.add(p);
			}
			if((connected[p.x][p.y + 1]) && (!connected[p.x][p.y - 1])){
				potentialDoors.add(p);
			}
		}
	}
	public void removeExtraConnectors(){
		for(Point p : connections){
			if(connected[p.x - 1][p.y] && connected[p.x + 1][p.y]){
				cTR.add(p);
			}
			if(connected[p.x][p.y - 1] && connected[p.x][p.y + 1]){
				cTR.add(p);
			}
		}
		for(Point p : cTR){
			connections.remove(p);
		}
		cTR.clear();
	}
	public void floodFill(int x, int y){
		if(((map[x][y] == Tile.FLOOR) || (map[x][y] == Tile.DOOR_CLOSED)) && (!connected[x][y])){
			connected[x][y] = true;
		}
		else{
			return;
		}
		floodFill(x + 1, y);
		floodFill(x - 1, y);
		floodFill(x, y + 1);
		floodFill(x, y - 1);
	}
	
	public void findConnections(){
		for(int i = 1; i < width - 1; i++){
			for(int j = 1; j < height - 1; j++){
				if((map[i][j] == Tile.WALL)
						&& (map[i - 1][j] == Tile.FLOOR)
						&& (map[i + 1][j] == Tile.FLOOR)
						&& (roomFlag[i + 1][j] || roomFlag[i - 1][j])){
					connections.add(new Point(i, j));
				}
				if((map[i][j] == Tile.WALL)
						&& (map[i][j - 1] == Tile.FLOOR)
						&& (map[i][j + 1] == Tile.FLOOR)
						&& (roomFlag[i][j - 1] || roomFlag[i][j + 1])){
					connections.add(new Point(i, j));
				}
			}
		}
	}
	public void startMaze(){
		for(int i = 1; i < width - 2; i++){
			for(int j = 1; j < height - 2; j++){
				if(isSolid(i, j)){
					generateMaze(i, j);
				}
			}
		}
	}
	
	public boolean isSolid(int x, int y){
		if((map[x][y] == Tile.WALL) 
			&& (map[x + 1][y] == Tile.WALL)
			&& (map[x - 1][y] == Tile.WALL)
			&& (map[x][y - 1] == Tile.WALL)
			&& (map[x][y + 1] == Tile.WALL)
			&& (map[x+1][y+1] == Tile.WALL)
			&& (map[x+1][y-1] == Tile.WALL)
			&& (map[x-1][y+1] == Tile.WALL)
			&& (map[x-1][y-1] == Tile.WALL)){
				return true;
			}
		else{
			return false;
		}
	}
	
	public void generateMaze(int x, int y){
		Point start = new Point(x, y);
		buildFrontier(start);
		carvePath(start);
		updateFrontier();
		while(!frontier.isEmpty()){
			Point current = frontier.remove(RandomGen.rand(0, frontier.size() - 1));
			buildFrontier(current);
			carvePath(current);
			updateFrontier();
		}
	}

	public boolean isInBounds(Point p){
		return isHorizontallyInBounds(p.x) && isVerticallyInBounds(p.y);
	}

	public boolean isInBounds(int x, int y){
		return isHorizontallyInBounds(x) && isVerticallyInBounds(y);
	}

	public boolean isHorizontallyInBounds(int x){
		return x > 0 && x < width - 1;
	}

	public boolean isVerticallyInBounds(int y){
		return y > 0 && y < height - 1;
	}

	public boolean isDirectionallySolid(Point p, Point direction){
		List <Point> directionalNeighbors = p.getDirectionalNeighbors(direction);
		for(Point toCheck : directionalNeighbors){
			if(isInBounds(toCheck) && !isWall(toCheck)){
				return false;
			}
		}
		return true;
	}

	public void buildFrontier(Point p){
		for(Point direction : Point.cardinal){
			if(isInBounds(p.getNeighbor(direction))){
				if(isDirectionallySolid(p, direction)){
					frontier.add(p.getNeighbor(direction));
				}
			}
			else continue;
		}
	}
	
	public void updateFrontier(){
		List <Point> toRemove = new ArrayList<>();
		for(Point p : frontier){
			if(!isValidMazeLocation(p)){
				toRemove.add(p);
			}
		}
		for(Point point : toRemove){
			frontier.remove(point);
		}
	}

	public boolean isValidMazeLocation(Point p){
		List <Point> neighbors = p.getFrontierNeighbors(p.directionFromParent);
		int floorCount = 0;
		for(Point point : neighbors){
			if(isInBounds(point)) {
				if (tile(point) == Tile.FLOOR) {
					floorCount++;
				}
			}
		}
		return floorCount == 0;
	}
	
	public void carvePath(Point s){
		map[s.x][s.y] = Tile.FLOOR;
	}
	public void placeRoom(){
		int h = RandomGen.rand(minRoomSize, maxRoomSize);
		if(h % 2 == 0){
			h = h + 1;
		}
		int w = RandomGen.rand(h, maxRoomSize);
		if(w % 2 == 0){
			w = w + 1;
		}
		int x = RandomGen.rand(0, (this.width - w - 2));
		int y = RandomGen.rand(0, (this.height - h - 2));
		if(x % 2 == 0){
			x += 1;
		}
		if(y % 2 == 0){
			y += 1;
		}
		
		Room newRoom = new Room(x, y, w, h);
		
		boolean failed = false;
		for(Room otherRoom : rooms){
			if(newRoom.intersects(otherRoom)){
				failed = true;
				break;
			}
		}
		if(!failed){
			createRoom(newRoom);
			rooms.add(newRoom);
		}
	}
	public void createRoom(Room newRoom){
		for(int i = 0; i < newRoom.width ; i++){
			for(int j = 0; j < newRoom.height; j++){
				map[newRoom.getX1() + i][newRoom.getY1() + j] = Tile.FLOOR;
				roomFlag[newRoom.getX1() + i][newRoom.getY1() + j] = true;
			}
		}
	}
	
	public void removeDeadEnds(){
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				int wallCount = 0;
				if(map[i][j] == Tile.FLOOR){
					if((map[i - 1][j] == Tile.WALL) || (map[i - 1][j] == Tile.PERM_WALL)){
						wallCount++;
					}
					if((map[i + 1][j] == Tile.WALL) || (map[i + 1][j] == Tile.PERM_WALL)){
						wallCount++;
					}
					if((map[i][j - 1] == Tile.WALL) || (map[i][j - 1] == Tile.PERM_WALL)){
						wallCount++;
					}
					if((map[i][j + 1] == Tile.WALL) || (map[i][j + 1] == Tile.PERM_WALL)){
						wallCount++;
					}
					if(wallCount >= 3){
						Point newP = new Point(i, j);
						deadEnds.add(newP);
					}
				}
				if(map[i][j] == Tile.DOOR_CLOSED){
					if((map[i - 1][j] == Tile.WALL) || (map[i - 1][j] == Tile.PERM_WALL)){
						wallCount++;
					}
					if((map[i + 1][j] == Tile.WALL) || (map[i + 1][j] == Tile.PERM_WALL)){
						wallCount++;
					}
					if((map[i][j - 1] == Tile.WALL) || (map[i][j - 1] == Tile.PERM_WALL)){
						wallCount++;
					}
					if((map[i][j + 1] == Tile.WALL) || (map[i][j + 1] == Tile.PERM_WALL)){
						wallCount++;
					}
					if(wallCount >= 3){
						Point newP = new Point(i, j);
						deadEnds.add(newP);
					}
				}
			}
		}
		for(Point p : deadEnds){
			map[p.x][p.y] = Tile.WALL;
		}
		deadEnds.clear();
	}

	public Tile tile(Point p){
		return map[p.x][p.y];
	}

	public Tile tile(int x, int y){
		if (x < 0 || x >= width || y < 0 || y >= height)
			return Tile.BOUNDS;
		else
			return map[x][y];}

}

