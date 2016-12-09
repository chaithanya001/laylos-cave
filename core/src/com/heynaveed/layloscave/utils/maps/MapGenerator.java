package com.heynaveed.layloscave.utils.maps;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.heynaveed.layloscave.GameApp;
import com.heynaveed.layloscave.states.MapState;
import com.heynaveed.layloscave.universe.Portal;
import com.heynaveed.layloscave.universe.maps.HubMap;
import com.heynaveed.layloscave.universe.maps.StageMap;
import com.heynaveed.layloscave.universe.maps.TunnelMap;

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


public final class MapGenerator {

    private static final int MAX_PORTAL_PLATFORM_LENGTH = 12;
    private static final int MAX_PORTAL_PLATFORM_HEIGHT = 4;
    private static final int[] GROUND_IDS = {1, 2, 3, 4, 5};
    private static final int[] CAVE_IDS = {6, 7, 8, 9, 10};
    public static final int PLATFORM_MIN_X = 20;
    public static final int PLATFORM_MAX_X = 180;
    public static final int PLATFORM_MIN_Y = 20;
    public static final int PLATFORM_MAX_Y = 280;
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    public static final String MAP_PATH = "maps/";
    private static final String TEMPLATE_PATH = "template";
    public static final String TMX_EXTENSION = ".tmx";
    private static final int HUB_MAP_HEIGHT = 200;
    private static final int HUB_MAP_WIDTH = 300;
    private static final int CAVERN_MAP_WIDTH = 191;
    private static final int CAVERN_MAP_HEIGHT = 111;
    public static final int TUNNEL_MAP_HEIGHT = 200;
    public static final int TUNNEL_MAP_WIDTH = 300;
    private static int hubNumber = 0;
    private static int cavernNumber = 0;
    private static int tunnelNumber = 0;
    private static int[][] workingTileIDSet;
    private static int[] finalTileIDSet;
    private static String encodedString;
    private static Element mapFileRoot;
    private static ArrayList<TileVector[]> platformPositions;
    private static ArrayList<TileVector> portalPositions;
    private static ArrayList<Boolean> portalFacing;
    private static int objectID = 1000;
    private static FileHandle newMap;
    private static MapState workingMapState;
    private static int levelNumber = 0;
    private static int workingWidth;
    public static int workingHeight;

    private static final Random random = new Random();
    private static HubMap hubMap;
    private static StageMap stageMap;
    private static TunnelMap tunnelMap;

    public static void main(String[] args) throws IOException{
        GameApp.CONFIGURATION = "Desktop";
//        new MapGenerator().buildMap(MapState.HUB);
        new MapGenerator().buildMap(MapState.STAGE);
//        new MapGenerator().buildMap(MapState.TUNNEL);
    }

    public MapGenerator buildMap(MapState mapState) throws IOException{
        workingMapState = mapState;
        determineWorkingVariables();
        createNewMapFile();
        loadMapRoot();

        switch(workingMapState){
            case HUB:
                hubMap = new HubMap(workingHeight, workingWidth);
                workingTileIDSet = hubMap.getTileIDSet();
                determinePortalPositions();
                break;
            case STAGE:
                stageMap = new StageMap(workingHeight, workingWidth);
                workingTileIDSet = stageMap.getTileIDSet();
                break;
            case TUNNEL:
                tunnelMap = new TunnelMap(workingHeight, workingWidth);
                workingTileIDSet = tunnelMap.getTileIDSet();
                break;
        }

        deleteOldObjects();
        calculateTerrainObjectList();
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
            for (int y = 0; y < HUB_MAP_HEIGHT; y++) {
                for (int x = 0; x < HUB_MAP_WIDTH; x++) {
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
                    finalTileIDSet[y * HUB_MAP_WIDTH + x] = unsignedByteToInt(temp[0]) | unsignedByteToInt(temp[1]) << 8
                            | unsignedByteToInt(temp[2]) << 16 | unsignedByteToInt(temp[3]) << 24;
                }
            }
        }
        catch(IOException e){}
        finally { StreamUtils.closeQuietly(is); }
    }

    private static void determineWorkingVariables(){
        switch(workingMapState){
            case HUB:
                levelNumber = ++hubNumber;
                workingWidth = HUB_MAP_WIDTH;
                workingHeight = HUB_MAP_HEIGHT;
                break;
            case STAGE:
                levelNumber = ++cavernNumber;
                workingWidth = CAVERN_MAP_WIDTH;
                workingHeight = CAVERN_MAP_HEIGHT;
                break;
            case TUNNEL:
                levelNumber = ++tunnelNumber;
                workingWidth = TUNNEL_MAP_WIDTH;
                workingHeight = TUNNEL_MAP_HEIGHT;
            default:
                levelNumber = 1;
        }

        workingTileIDSet = new int[workingHeight][workingWidth];
        finalTileIDSet = new int[workingWidth*workingHeight];
    }

    private static void createNewMapFile() throws IOException{

        if(GameApp.CONFIGURATION.equals("Desktop")) {
            FileChannel source = null;
            FileChannel destination = null;
            File templateMap = new File(MAP_PATH + TEMPLATE_PATH + workingMapState.name + TMX_EXTENSION);
            File newMap = new File(MAP_PATH + workingMapState.name + levelNumber + TMX_EXTENSION);

            try {
                source = new FileInputStream(templateMap).getChannel();
                destination = new FileOutputStream(newMap).getChannel();
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
            FileHandle templateMap = Gdx.files.internal(MAP_PATH + TEMPLATE_PATH + workingMapState.name + TMX_EXTENSION);
            newMap = Gdx.files.local(workingMapState.name + levelNumber + TMX_EXTENSION);
            newMap.writeString(templateMap.readString(), false);
            FileHandle tileMapGutterOriginal = Gdx.files.internal("maps/tileMapGutter.png");
            FileHandle tileSetOriginal = Gdx.files.internal("maps/tileSet.png");
            tileMapGutterOriginal.copyTo(Gdx.files.local("tileMapGutter.png"));
            tileSetOriginal.copyTo(Gdx.files.local("tileSet.png"));
        }
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

        switch(workingMapState){
            case HUB:
                int padding = 10;

                int x = random.nextInt(PLATFORM_MAX_X - PLATFORM_MIN_X) + PLATFORM_MIN_X;
                int y = random.nextInt(PLATFORM_MAX_Y - PLATFORM_MIN_Y) + PLATFORM_MIN_Y;

                for (int i = -padding; i <= padding; i++) {
                    for (int j = -padding; j <= padding; j++) {
                        if (i == padding) {
                            if (workingTileIDSet[x + i][y + j] == 0)
                                return getRandomStartingPosition();
                        } else if (workingTileIDSet[x + i][y + j] != 0)
                            return getRandomStartingPosition();
                    }
                }

                return new Vector2(GameApp.toPPM(y) * 64, GameApp.toPPM(workingHeight - x - (padding - 2)) * 64);
            case STAGE:
                TileVector tileVector = stageMap.getCaverns().get(stageMap.getCavernBlockPath().get(0)).getMidPoint();
                return new Vector2(tileVectorToWorldPosition(new TileVector(tileVector.x + 10, tileVector.y)));
            default: TUNNEL:
                return new Vector2(tileVectorToWorldPosition(new TileVector(184, 26)));
        }
    }

    private static void determinePortalPositions(){
        portalPositions = new ArrayList<TileVector>();
        portalFacing = new ArrayList<Boolean>();
        ArrayList<TileVector> potentialPositions = new ArrayList<TileVector>();
        ArrayList<Boolean> potentialFacing = new ArrayList<Boolean>();

        int minX = 30, minY = 40;
        int maxX = HUB_MAP_HEIGHT - minX;
        int maxY = HUB_MAP_WIDTH - minY;

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
        int y1 = random.nextInt(HUB_MAP_WIDTH /2 - minY)+ HUB_MAP_WIDTH /2;
        int y2 = random.nextInt(HUB_MAP_WIDTH /2 - minY)+minY;

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

    private static void calculateTerrainObjectList(){

        ArrayList<TileVector[]> caveObjectList = new ArrayList<TileVector[]>();
        int[] GROUND_CAVE_IDS = new int[CAVE_IDS.length + GROUND_IDS.length];
        for(int i = 0; i < CAVE_IDS.length; i++)
            GROUND_CAVE_IDS[i] = CAVE_IDS[i];
        for(int i = 0; i < GROUND_IDS.length; i++)
            GROUND_CAVE_IDS[i+(CAVE_IDS.length)] = GROUND_IDS[i];

        for(int i = 0; i < workingTileIDSet.length; i++){
            TileVector[] tileVector = new TileVector[workingWidth];
            int buffer = 0;
            for(int j = 0; j < workingTileIDSet[i].length; j++, buffer++){

                if (!doesTileMatchArray(GROUND_CAVE_IDS, workingTileIDSet[i][j]) || j == workingWidth -1){
                    if(buffer > 1) {
                        TileVector[] truncatedVector = new TileVector[buffer];
                        for (int l = 0; l < truncatedVector.length; l++)
                            truncatedVector[l] = tileVector[l];
                        tileVector = new TileVector[workingWidth];
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
        for (int anArray : array) {
            if (number == anArray)
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

            File file = new File(MAP_PATH + workingMapState.name + levelNumber + TMX_EXTENSION);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = reader.readLine();
            }
            reader.close();
            mapFileRoot = new XmlReader().parse(sb.toString());
        }
        else if(GameApp.CONFIGURATION.equals("Android"))
            mapFileRoot = new XmlReader().parse(newMap.readString());
    }

    private static void updateTerrainLayer() throws IOException{
        for(int i = 0; i < mapFileRoot.getChildCount(); i++){
            if(mapFileRoot.getChild(i).getAttribute("name").equals("terrain"))
                mapFileRoot.getChild(i).getChildByName("data").setText(encodedString);
        }
    }

    private static void writeToMap() throws IOException{

        if(GameApp.CONFIGURATION.equals("Desktop")) {
            PrintWriter writer = new PrintWriter(new File(MAP_PATH + workingMapState.name + levelNumber + TMX_EXTENSION));
            writer.print("");
            writer.print(XML_HEADER + "\n" + mapFileRoot.toString());
            writer.close();
        }
        else if(GameApp.CONFIGURATION.equals("Android"))
            newMap.writeString(XML_HEADER + "\n" + mapFileRoot.toString(), false);
    }

    private Vector2 tileVectorToWorldPosition(TileVector tileVector) {
        return new Vector2(GameApp.toPPM(tileVector.y()) * 64, GameApp.toPPM(workingHeight - tileVector.x()) * 64);
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

    private static int[] convertToFinalArray(int[][] array){

        int[] newArray = new int[workingWidth * workingHeight];
        int count = 0;

        for(int i = 0; i < array.length; i++){
            for(int j = 0; j < array[i].length; j++, count++)
                newArray[count] = array[i][j];
        }

        return newArray;
    }

    private static int[][] convertToWorkingArray(int[] array){
        int[][] newArray = new int[workingWidth][workingHeight];
        int j = 0;

        for(int i = 0; i < array.length; i++){
            newArray[j][i] = array[i];

            if(i+1 % workingWidth == 0)
                j++;
        }

        return newArray;
    }

    private static int generateObjectID(){
        return objectID++;
    }

    public ArrayList<TileVector> getPortalPositions(){
        return portalPositions;
    }

    public ArrayList<Boolean> getPortalFacing(){ return portalFacing; }
}
