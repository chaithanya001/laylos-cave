package com.heynaveed.layloscave.utils.maps.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.heynaveed.layloscave.GameApp;
import com.heynaveed.layloscave.states.MapState;
import com.heynaveed.layloscave.states.PathDirectionState;
import com.heynaveed.layloscave.universe.Portal;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by naveed.shihab on 27/10/2016.
 */

public final class MapGenerator {

    private static final int MAX_PORTAL_PLATFORM_LENGTH = 12;
    private static final int MAX_PORTAL_PLATFORM_HEIGHT = 4;
    private static final int[] GROUND_IDS = {1, 2, 3, 4, 5};
    private static final int[] CAVE_IDS = {6, 7, 8, 9, 10};
    public static final int PLATFORM_MIN_X = 20;
    public static final int PLATFORM_MAX_X = 180;
    public static final int PLATFORM_MIN_Y = 20;
    public static final int PLATFORM_MAX_Y = 280;
    private static final int PATH_PADDING = 8;
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    public static final String MAP_PATH = "maps/";
    public static final String TEMPLATE_PATH = "template";
    public static final String TMX_EXTENSION = ".tmx";
    private static final int MIN_X_CLEAN_PADDING = 20;
    private static final int MAX_X_CLEAN_PADDING = 160;
    private static final int MIN_Y_CLEAN_PADDING = 20;
    private static final int MAX_Y_CLEAN_PADDING = 270;
    public static final int HEIGHT = 200;
    public static final int WIDTH = 300;
    private static int hubNumber = 0;
    private static int[][] workingTileIDSet = new int[HEIGHT][WIDTH];
    private static int[] finalTileIDSet = new int[WIDTH * HEIGHT];
    private static String encodedString;
    private static Element mapFileRoot;
    private static ArrayList<TileVector[]> platformPositions;
    private static ArrayList<TileVector> portalPositions;
    private static ArrayList<Boolean> portalFacing;
    private static Path path;
    private static int objectID = 1000;
    private static FileHandle newHubMap;

    private static final Random random = new Random();

    public static void main(String[] args) throws IOException{
        GameApp.CONFIGURATION = "Desktop";
        new MapGenerator().buildPathMap();
    }

    public MapGenerator buildPathMap() throws IOException{
        createNewMapFile();
        loadMapRoot();
        generateMapBase();
        cleanMapNoise();
        generatePathPlatforms();
        determinePortalPositions();
        deleteOldObjects();
        calculateCaveObjectList();
        generateObjects();
        compressTileIDSet();
        updateTerrainLayer();
        writeToMap();
        return this;
    }

    public static void extractTileIDSet(byte[] data){

        InputStream is = null;
        try {
            is = new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(data), data.length));
            byte[] temp = new byte[4];
            int count = 0;
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    int read = is.read(temp);
                    for(int i = 0; i < temp.length; i++)
                        while (read < temp.length) {
                            int curr = is.read(temp, read, temp.length - read);
                            if (curr == -1)
                                break;
                            read += curr;
                        }
                    if (read != temp.length)
                        throw new GdxRuntimeException("Error Reading TMX Layer Data: Premature end of tile data");
                    finalTileIDSet[y * WIDTH + x] = unsignedByteToInt(temp[0]) | unsignedByteToInt(temp[1]) << 8
                            | unsignedByteToInt(temp[2]) << 16 | unsignedByteToInt(temp[3]) << 24;
                }
            }
        }
        catch(IOException e){}
        finally { StreamUtils.closeQuietly(is); }
    }

    private static void createNewMapFile() throws IOException{

        if(GameApp.CONFIGURATION.equals("Desktop")) {
            FileChannel source = null;
            FileChannel destination = null;
            File templateHubMap = new File(MAP_PATH + TEMPLATE_PATH + MapState.HUB.name + TMX_EXTENSION);
            File newHubMap = new File(MAP_PATH + MapState.HUB.name + ++hubNumber + TMX_EXTENSION);

            try {
                source = new FileInputStream(templateHubMap).getChannel();
                destination = new FileOutputStream(newHubMap).getChannel();
                destination.transferFrom(source, 0, source.size());
            } finally {
                if (source != null) {
                    source.close();
                }
                if (destination != null) {
                    destination.close();
                }
            }
        }
        else if(GameApp.CONFIGURATION.equals("Android")){
            FileHandle templateHubMap = Gdx.files.internal("maps/templateHubMap.tmx");
            newHubMap = Gdx.files.local(MapState.HUB.name + ++hubNumber + TMX_EXTENSION);
            newHubMap.writeString(templateHubMap.readString(), false);
            FileHandle tileMapGutterOriginal = Gdx.files.internal("maps/tileMapGutter.png");
            FileHandle tileMapGutter = Gdx.files.local("tileMapGutter.png");
            FileHandle tileSetOriginal = Gdx.files.internal("maps/tileSet.png");
            FileHandle tileSet = Gdx.files.local("tileSet.png");
            tileMapGutterOriginal.copyTo(tileMapGutter);
            tileSetOriginal.copyTo(tileSet);
        }
    }

    private static void generateMapBase() {
        int fillPercent = 45;

        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                if (isWallTile(i, j))
                    workingTileIDSet[i][j] = randomTileID(CAVE_IDS);
                else
                    workingTileIDSet[i][j] = (random.nextInt(100) < fillPercent) ? randomTileID(CAVE_IDS) : 0;
            }
        }

        smoothMap(5);
    }

    private static void cleanMapNoise(){
        for(int x = 0; x < HEIGHT; x++){
            for(int y = 0; y < WIDTH; y++){
                if(x > offsetRandom(MIN_X_CLEAN_PADDING) && x < offsetRandom(MAX_X_CLEAN_PADDING) && y > offsetRandom(MIN_Y_CLEAN_PADDING) && y < offsetRandom(MAX_Y_CLEAN_PADDING))
                    workingTileIDSet[x][y] = randomTileID(CAVE_IDS);
            }
        }
        smoothMap(7);
    }

    private static void deleteOldObjects(){
        for(int i = 0; i < mapFileRoot.getChildCount(); i++){
            if(mapFileRoot.getChild(i).getAttribute("name").equals("ground")){
                for(int j = mapFileRoot.getChild(i).getChildCount()-1; j >= 0; j--)
                    mapFileRoot.getChild(i).removeChild(j);
            }
        }
    }

    public Vector2 getRandomStartingPosition() {
        int padding = 10;
        int x = random.nextInt(MapGenerator.PLATFORM_MAX_X - MapGenerator.PLATFORM_MIN_X) + MapGenerator.PLATFORM_MIN_X;
        int y = random.nextInt(MapGenerator.PLATFORM_MAX_Y - MapGenerator.PLATFORM_MIN_Y) + MapGenerator.PLATFORM_MIN_Y;

        for (int i = -padding; i <= padding; i++) {
            for (int j = -padding; j <= padding; j++) {
                if (i == padding) {
                    if (workingTileIDSet[x + i][y + j] == 0)
                        return getRandomStartingPosition();
                } else if (workingTileIDSet[x + i][y + j] != 0)
                    return getRandomStartingPosition();
            }
        }

        return new Vector2(GameApp.toPPM(y) * 64, GameApp.toPPM(MapGenerator.HEIGHT - x - (padding - 2)) * 64);
    }

    private static void generatePathPlatforms(){
        path = new Path().build();
        platformPositions = path.getIndividualSegmentPositions();
        ArrayList<Segment> pathSegments = path.getPathSegments();

        for(int i = 0; i < platformPositions.size(); i++){
            for(int j = 0; j < platformPositions.get(i).length; j++)
                workingTileIDSet[platformPositions.get(i)[j].x][platformPositions.get(i)[j].y] = 0;
        }

        for(int i = 0; i < pathSegments.size(); i++){
            PathDirectionState directionState = pathSegments.get(i).getDirection();
            TileVector[] tileVector = pathSegments.get(i).getTileVectorsAsArray();

            boolean isXAxis = false;

            switch(directionState){
                case UP:
                case DOWN:
                    isXAxis = true;
                    break;
                case LEFT:
                case RIGHT:
                    isXAxis = false;
                    break;
            }


            for (TileVector tileVectors : tileVector) {
                    for (int k = -PATH_PADDING; k < 0; k++) {
                        if (isXAxis)
                            workingTileIDSet[tileVectors.x][tileVectors.y + k] = 0;
                        else
                            workingTileIDSet[tileVectors.x + k][tileVectors.y] = 0;
                    }
                    for (int k = 0; k < PATH_PADDING; k++) {
                        if (isXAxis)
                            workingTileIDSet[tileVectors.x][tileVectors.y + k] = 0;
                        else
                            workingTileIDSet[tileVectors.x + k][tileVectors.y] = 0;
                    }
            }
        }

        smoothMap(5);
    }

    private static void determinePortalPositions(){
        portalPositions = new ArrayList<TileVector>();
        portalFacing = new ArrayList<Boolean>();
        ArrayList<TileVector> potentialPositions = new ArrayList<TileVector>();
        ArrayList<Boolean> potentialFacing = new ArrayList<Boolean>();

        int minX = 30, minY = 40;
        int maxX = HEIGHT - minX;
        int maxY = WIDTH - minY;

        for(int x = minX; x <= maxX; x++){

            row_loop:
            for(int y = minY; y <= maxY; y++){

                if(workingTileIDSet[x][y] == 0 && doesTileMatchArray(CAVE_IDS, workingTileIDSet[x+1][y])
                        && doesTileMatchArray(CAVE_IDS, workingTileIDSet[x][y-1])) {

                    for(int i = y; i < y+MAX_PORTAL_PLATFORM_LENGTH; i++){
                        if (!doesTileMatchArray(CAVE_IDS, workingTileIDSet[x+1][i]))
                            continue row_loop;
                    }
                    for(int i = x; i > x-4; i--){
                        for(int j = y; j < y+MAX_PORTAL_PLATFORM_LENGTH; j++) {
                            if (workingTileIDSet[i][j] != 0)
                                continue row_loop;
                        }
                    }
                    for(int i = x; i < x+MAX_PORTAL_PLATFORM_HEIGHT; i++){
                        if(!doesTileMatchArray(CAVE_IDS, workingTileIDSet[i][y-1]))
                            continue row_loop;
                    }

                    potentialPositions.add(new TileVector(x-2, y));
                    potentialFacing.add(true);
                }
                else if(workingTileIDSet[x][y] == 0 && doesTileMatchArray(CAVE_IDS, workingTileIDSet[x+1][y])
                        && doesTileMatchArray(CAVE_IDS, workingTileIDSet[x][y+1])) {
                    for(int i = y; i > y-MAX_PORTAL_PLATFORM_LENGTH; i--){
                        if (!doesTileMatchArray(CAVE_IDS, workingTileIDSet[x+1][i]))
                            continue row_loop;
                    }
                    for(int i = x; i > x-4; i--){
                        for(int j = y; j > y-MAX_PORTAL_PLATFORM_LENGTH; j--) {
                            if (workingTileIDSet[i][j] != 0)
                                continue row_loop;
                        }
                    }
                    for(int i = x; i < x+MAX_PORTAL_PLATFORM_HEIGHT; i++){
                        if(!doesTileMatchArray(CAVE_IDS, workingTileIDSet[i][y+1]))
                            continue row_loop;
                    }

                    potentialPositions.add(new TileVector(x-2, y));
                    potentialFacing.add(false);
                }
            }

            if(potentialPositions.size() > 0) {
                int index = random.nextInt(potentialPositions.size());
                portalPositions.add(potentialPositions.get(index));
                portalFacing.add(potentialFacing.get(index));
            }

            potentialPositions.clear();
            potentialFacing.clear();
        }

        int portalPositionsSize = portalPositions.size();

        if(portalPositionsSize > Portal.MAX_PORTAL_NUMBER){
            do {
                portalPositions.remove(portalPositions.size()/2);
                portalFacing.remove(portalFacing.size()/2);
            } while (portalPositions.size() != Portal.MAX_PORTAL_NUMBER);
        }

        int x1 = 199, x2 = 199;
        int y1 = random.nextInt(MapGenerator.WIDTH/2 - minY)+MapGenerator.WIDTH/2;
        int y2 = random.nextInt(MapGenerator.WIDTH/2 - minY)+minY;

        for(int i = x1; workingTileIDSet[i][y1] != 0; i--)
            x1 = i;
        for(int i = x2; workingTileIDSet[i][y2] != 0; i--)
            x2 = i;

        portalPositions.add(new TileVector(x1-4, y1));
        portalPositions.add(new TileVector(x2-4, y2));
        portalFacing.add(false);
        portalFacing.add(true);

//        for(int i = 0; i < portalPositions.size(); i++) {
//            workingTileIDSet[portalPositions.get(i).x+1][portalPositions.get(i).y] = randomTileID(BOUNCY_IDS);
//            workingTileIDSet[portalPositions.get(i).x+2][portalPositions.get(i).y] = randomTileID(BOUNCY_IDS);
//            workingTileIDSet[portalPositions.get(i).x][portalPositions.get(i).y] = randomTileID(BOUNCY_IDS);
//            workingTileIDSet[portalPositions.get(i).x-1][portalPositions.get(i).y] = randomTileID(BOUNCY_IDS);
//            workingTileIDSet[portalPositions.get(i).x-2][portalPositions.get(i).y] = randomTileID(BOUNCY_IDS);
//            workingTileIDSet[portalPositions.get(i).x-3][portalPositions.get(i).y] = randomTileID(BOUNCY_IDS);
//        }
    }

    private static void calculateCaveObjectList(){

        ArrayList<TileVector[]> caveObjectList = new ArrayList<TileVector[]>();
        int[] GROUND_CAVE_IDS = new int[CAVE_IDS.length + GROUND_IDS.length];
        for(int i = 0; i < CAVE_IDS.length; i++)
            GROUND_CAVE_IDS[i] = CAVE_IDS[i];
        for(int i = 0; i < GROUND_IDS.length; i++)
            GROUND_CAVE_IDS[i+(CAVE_IDS.length)] = GROUND_IDS[i];

        for(int i = 0; i < workingTileIDSet.length; i++){
            TileVector[] tileVector = new TileVector[WIDTH];
            int buffer = 0;
            for(int j = 0; j < workingTileIDSet[i].length; j++, buffer++){

                if (!doesTileMatchArray(GROUND_CAVE_IDS, workingTileIDSet[i][j]) || j == WIDTH-1){
                    if(buffer > 1) {
                        TileVector[] truncatedVector = new TileVector[buffer];
                        for (int l = 0; l < truncatedVector.length; l++)
                            truncatedVector[l] = tileVector[l];
                        tileVector = new TileVector[WIDTH];
                        caveObjectList.add(truncatedVector);
                    }
                    buffer = 0;
                }
                tileVector[buffer] = new TileVector(i, j);
            }
        }

        platformPositions = caveObjectList;
    }

    private static boolean doesTileMatchArray(int[] array, int number){
        for(int i = 0; i < array.length; i++){
            if(number == array[i])
                return true;
        }

        return false;
    }

    private static void generateObjects() throws IOException{

        int offset = GameApp.TILE_LENGTH;

        TileVector[] vector;
        Element object;

        for(int i = 0; i < mapFileRoot.getChildCount(); i++){
            if(mapFileRoot.getChild(i).getAttribute("name").equals("ground")) {
                for(int j = 0; j < platformPositions.size(); j++) {
                    vector = platformPositions.get(j);
                    object = new Element("object", mapFileRoot.getChildByName("objectLayer"));
                    object.setAttribute("height", Integer.toString(GameApp.TILE_LENGTH));
                    object.setAttribute("width", Integer.toString(GameApp.TILE_LENGTH*vector.length - offset));
                    object.setAttribute("y", Integer.toString(GameApp.TILE_LENGTH*vector[0].x));
                    object.setAttribute("x", Integer.toString(GameApp.TILE_LENGTH*vector[0].y + offset));
                    object.setAttribute("id", Integer.toString(generateObjectID()));
                    mapFileRoot.getChild(i).addChild(object);
                }
            }
        }
    }

    private static void compressTileIDSet() throws IOException{
        finalTileIDSet = convertToFinalArray(workingTileIDSet);
        byte[] byteArray = intToUnsignedByte(finalTileIDSet);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(byteArray.length);
        GZIPOutputStream zipStream = new GZIPOutputStream(byteStream);
        zipStream.write(byteArray);
        zipStream.close();
        byteStream.close();
        byte[] compressedData = byteStream.toByteArray();
        encodedString = new String(Base64Coder.encode(compressedData));
    }

    private static void loadMapRoot() throws IOException{

        if(GameApp.CONFIGURATION.equals("Desktop")) {

            File file = new File(MAP_PATH + MapState.HUB.name + hubNumber + TMX_EXTENSION);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = reader.readLine();
            }

            mapFileRoot = new XmlReader().parse(sb.toString());
            reader.close();
        }
        else if(GameApp.CONFIGURATION.equals("Android"))
            mapFileRoot = new XmlReader().parse(newHubMap.readString());
    }

    private static void updateTerrainLayer() throws IOException{
        for(int i = 0; i < mapFileRoot.getChildCount(); i++){
            if(mapFileRoot.getChild(i).getAttribute("name").equals("terrain"))
                mapFileRoot.getChild(i).getChildByName("data").setText(encodedString);
        }
    }

    private static void writeToMap() throws IOException{

        if(GameApp.CONFIGURATION.equals("Desktop")) {
            PrintWriter writer = new PrintWriter(new File(MAP_PATH + MapState.HUB.name + hubNumber + TMX_EXTENSION));
            writer.print("");
            writer.print(XML_HEADER + "\n" + mapFileRoot.toString());
            writer.close();
        }
        else if(GameApp.CONFIGURATION.equals("Android"))
            newHubMap.writeString(XML_HEADER + "\n" + mapFileRoot.toString(), false);
    }

    private static void smoothMap(int iterations){
        for(int i = 0; i < iterations; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                for (int k = 0; k < WIDTH; k++) {
                    int neighbourWallTileCount = calculateSurroundingWallCount(j, k);

                    if (neighbourWallTileCount > 4)
                        workingTileIDSet[j][k] = randomTileID(CAVE_IDS);
                    else if (neighbourWallTileCount < 4)
                        workingTileIDSet[j][k] = 0;
                }
            }
        }
    }

    private static int offsetRandom(int value){
        return random.nextInt(15) + value;
    }

    private static int calculateSurroundingWallCount(int gridX, int gridY){
        int wallCount = 0;

        for(int neighbourX = gridX - 1; neighbourX <= gridX + 1; neighbourX++){
            for(int neighbourY = gridY -1; neighbourY <= gridY +1; neighbourY++){
                if(neighbourX >= 0 && neighbourX < HEIGHT && neighbourY >= 0 && neighbourY < WIDTH){
                    if(neighbourX != gridX || neighbourY != gridY)
                        wallCount += workingTileIDSet[neighbourX][neighbourY] == 0?0:1;
                }
                else
                    wallCount++;
            }
        }
        return wallCount;
    }

    private static byte[] intToUnsignedByte(int[] integerArray){
        ByteBuffer byteBuffer = ByteBuffer.allocate(integerArray.length * 4).order(ByteOrder.LITTLE_ENDIAN);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(integerArray);
        return byteBuffer.array();
    }

    private static int unsignedByteToInt (byte b) {
        return b & 0xFF;
    }

    private static boolean isWallTile(int x, int y){
        return x < 6 || x > HEIGHT -10 || y < 6 || y > WIDTH -8;
    }

    private static int[] convertToFinalArray(int[][] array){

        int[] newArray = new int[WIDTH * HEIGHT];
        int count = 0;

        for(int i = 0; i < array.length; i++){
            for(int j = 0; j < array[i].length; j++, count++){
                newArray[count] = array[i][j];
            }
        }

        return newArray;
    }

    private static int[][] convertToWorkingArray(int[] array){
        int[][] newArray = new int[WIDTH][HEIGHT];
        int j = 0;

        for(int i = 0; i < array.length; i++){
            newArray[j][i] = array[i];

            if(i+1 % WIDTH == 0)
                j++;
        }

        return newArray;
    }

    private static int generateObjectID(){
        return objectID++;
    }

    private static int randomTileID(int[] array){
        return array[random.nextInt(array.length)];
    }

    public ArrayList<TileVector> getPortalPositions(){
        return portalPositions;
    }

    public ArrayList<Boolean> getPortalFacing(){ return portalFacing; }
}
