package com.heynaveed.layloscave.universe.maps;

import com.heynaveed.layloscave.states.MapState;
import com.heynaveed.layloscave.universe.Map;
import com.heynaveed.layloscave.utils.maps.MapBuilder;
import com.heynaveed.layloscave.utils.maps.PathDirection;
import com.heynaveed.layloscave.utils.maps.Room;

import java.util.ArrayList;


public final class StageMap extends Map implements MapBuilder {

    private static final int MAX_BLOCK_PER_PATH = 12;
    private static final int MAX_BLOCKS = 16;
    private static final int CAVERN_BLOCK_WIDTH = 43;
    private static final int CAVERN_BLOCK_HEIGHT = 23;
    private static final int[] TOP_LEFT_X_BLOCK_POSITIONS = {17, 45, 73, 101};
    private static final int[] TOP_LEFT_Y_BLOCK_POSITIONS = {17, 65, 113, 161};
    private static final int[] X_BLOCK_MIDPOINTS = {28, 28, 28, 28, 56, 56, 56, 56, 84, 84, 84, 84, 112, 112, 112, 112};
    private static final int[] Y_BLOCK_MIDPOINTS = {38, 86, 134, 182, 38, 86, 134, 182, 38, 86, 134, 182, 38, 86, 134, 182};

    private static final ArrayList<Room> ROOMS = new ArrayList<Room>();
    private static final ArrayList<Integer> cavernBlockPath = new ArrayList<Integer>();

    public StageMap(int height, int width) {
        super(height, width);
        mapState = MapState.STAGE;

        initialiseWorkingValues();
        initialiseTileIDSet();
        generateTerrain();
        populateTileIDSet();
    }

    @Override
    public void initialiseWorkingValues(){
        ROOMS.clear();
        cavernBlockPath.clear();

        for(int i = 1; i <= MAX_BLOCKS; i++)
            ROOMS.add(new Room(i));
    }

    @Override
    public void initialiseTileIDSet() {
        for(int x = 0; x < tileIDSet.length; x++){
            for(int y = 0; y < tileIDSet[x].length; y++)
                tileIDSet[x][y] = randomTileID(CAVE_IDS);
        }

        for(int i = 0; i < TOP_LEFT_X_BLOCK_POSITIONS.length; i++){
            for(int j = 0; j < TOP_LEFT_Y_BLOCK_POSITIONS.length; j++){
                for(int x = TOP_LEFT_X_BLOCK_POSITIONS[i]; x < TOP_LEFT_X_BLOCK_POSITIONS[i]+CAVERN_BLOCK_HEIGHT; x++){
                    for(int y = TOP_LEFT_Y_BLOCK_POSITIONS[j]; y < TOP_LEFT_Y_BLOCK_POSITIONS[j]+CAVERN_BLOCK_WIDTH; y++)
                        tileIDSet[x][y] = 0;
                }
            }
        }
    }

    @Override
    public void generateTerrain() {
        int startingBlockPosition = RANDOM.nextInt(MAX_BLOCKS);
        cavernBlockPath.add(startingBlockPosition);
        ROOMS.get(startingBlockPosition).setPathBlock(true);

        for(int i = 1; i < MAX_BLOCK_PER_PATH; i++) {
            Room currentBlock = ROOMS.get(cavernBlockPath.get(cavernBlockPath.size()-1));
            ArrayList<PathDirection.Stage> potentialDirections = new ArrayList<PathDirection.Stage>();

            if (currentBlock.getRoomNumber() > 4)
                potentialDirections.add(PathDirection.Stage.UP);
            if (currentBlock.getRoomNumber() < 13)
                potentialDirections.add(PathDirection.Stage.DOWN);
            if (currentBlock.getRoomNumber() % 4 != 0)
                potentialDirections.add(PathDirection.Stage.RIGHT);
            if (currentBlock.getRoomNumber() % 4 != 1)
                potentialDirections.add(PathDirection.Stage.LEFT);

            check_loop:
            for (int j = 0; j < potentialDirections.size(); j++) {
                for (int k = 0; k < cavernBlockPath.size(); k++) {
                    if (currentBlock.getRoomNumber() + potentialDirections.get(j).direction == cavernBlockPath.get(k) + 1) {
                        potentialDirections.remove(j);
                        j--;
                        continue check_loop;
                    }
                }
            }

            if(i == 1){
                potentialDirections.remove(PathDirection.Stage.DOWN);
                potentialDirections.remove(PathDirection.Stage.UP);
            }

            if(!potentialDirections.isEmpty()) {
                PathDirection.Stage pathDirection = potentialDirections.get(RANDOM.nextInt(potentialDirections.size()));
                int nextBlockPosition = (currentBlock.getRoomNumber()-1) + pathDirection.direction;
                cavernBlockPath.add(nextBlockPosition);
                ROOMS.get(nextBlockPosition).setPathBlock(true);
                ROOMS.get(currentBlock.getRoomNumber()-1).setDirection(pathDirection);
            }
            else break;
        }
    }

    @Override
    public void populateTileIDSet() {
        for(int i = 0; i < ROOMS.size(); i++){
            if(!ROOMS.get(i).isPathBlock()){

                int tempX = TOP_LEFT_X_BLOCK_POSITIONS[0];
                int tempY = ((i%4)*48)+17;

                tempX_loop:
                for(int j = 0; j < TOP_LEFT_X_BLOCK_POSITIONS.length; j++){
                    if(i < (j+1)*4) {
                        tempX = TOP_LEFT_X_BLOCK_POSITIONS[j];
                        break tempX_loop;
                    }
                }

                for(int y = tempY; y <= tempY+44; y++){
                    for(int x = tempX; x <= tempX+24; x++)
                        tileIDSet[x][y] = randomTileID(CAVE_IDS);
                }
            }

            if(ROOMS.get(i).getDirection().equals(PathDirection.Stage.UP)){
                for(int j = X_BLOCK_MIDPOINTS[i]; j > X_BLOCK_MIDPOINTS[i]-28; j--){
                    for(int k = Y_BLOCK_MIDPOINTS[i]-2; k <= Y_BLOCK_MIDPOINTS[i]+2; k++)
                        tileIDSet[j][k] = 0;
                }
            }
            else if(ROOMS.get(i).getDirection().equals(PathDirection.Stage.DOWN)){
                for(int j = X_BLOCK_MIDPOINTS[i]; j < X_BLOCK_MIDPOINTS[i]+28; j++){
                    for(int k = Y_BLOCK_MIDPOINTS[i]-2; k <= Y_BLOCK_MIDPOINTS[i]+2; k++)
                        tileIDSet[j][k] = 0;
                }
            }
            else if(ROOMS.get(i).getDirection().equals(PathDirection.Stage.LEFT)){
                for(int j = Y_BLOCK_MIDPOINTS[i]; j > Y_BLOCK_MIDPOINTS[i]-45; j--){
                    for(int k = X_BLOCK_MIDPOINTS[i]-4; k <= X_BLOCK_MIDPOINTS[i]+4; k++)
                        tileIDSet[k][j] = 0;
                }
            }
            else if(ROOMS.get(i).getDirection().equals(PathDirection.Stage.RIGHT)){
                for(int j = Y_BLOCK_MIDPOINTS[i]; j < Y_BLOCK_MIDPOINTS[i]+45; j++){
                    for(int k = X_BLOCK_MIDPOINTS[i]-4; k <= X_BLOCK_MIDPOINTS[i]+4; k++)
                        tileIDSet[k][j] = 0;
                }
            }
        }
    }

    public ArrayList<Room> getCaverns(){
        return ROOMS;
    }

    public ArrayList<Integer> getCavernBlockPath(){
        return cavernBlockPath;
    }
}
