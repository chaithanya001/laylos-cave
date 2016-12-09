package com.heynaveed.layloscave.utils.maps;

import com.heynaveed.layloscave.states.MapState;
import java.util.ArrayList;


final class HubMap extends Map implements MapBuilder{

    private static final int PATH_PADDING = 8;
    private static final int PATH_SPACING = 7;
    private static final int MAX_SEGMENTS = 250;
    private static final int[] VERTICAL_SEGMENT_SIZES = {8, 9, 10, 11, 12, 13};
    private static final int[] HORIZONTAL_SEGMENT_SIZES = {20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35};
    private static final ArrayList<TileVector[]> INDIVIDUAL_SEGMENT_POSITIONS = new ArrayList<TileVector[]>();
    private static final PathDirection.Hub[] DIRECTION_STATES = truncateDirectionArray(PathDirection.Hub.values());
    private static final boolean[] DIRECTION_POTENTIAL = new boolean[DIRECTION_STATES.length];
    private static final ArrayList<PathDirection.Hub> FINAL_DIRECTION_STATES = new ArrayList<PathDirection.Hub>();
    private static final Path PATH = new Path();
    private static TileVector WORKING_POSITION;
    private static int SEGMENT_LENGTH;
    private static PathDirection.Hub CURRENT_DIRECTION;

    HubMap(int height, int width) {
        super(height, width);
        mapState = MapState.HUB;

        initialiseWorkingValues();
        initialiseTileIDSet();
        generateTerrain();
        populateTileIDSet();
    }

    @Override
    public void initialiseWorkingValues(){
        PATH.getNodes().clear();
        INDIVIDUAL_SEGMENT_POSITIONS.clear();
        FINAL_DIRECTION_STATES.clear();

        WORKING_POSITION = new TileVector(
                RANDOM.nextInt(MapGenerator.PLATFORM_MAX_X - MapGenerator.PLATFORM_MIN_X) + MapGenerator.PLATFORM_MIN_X,
                RANDOM.nextInt(MapGenerator.PLATFORM_MAX_Y - MapGenerator.PLATFORM_MIN_Y) + MapGenerator.PLATFORM_MIN_Y);
        CURRENT_DIRECTION = decideDirection();
        SEGMENT_LENGTH = 15;
        appendSegmentToPath();
    }

    @Override
    public void initialiseTileIDSet() {
        for (int x = 0; x < tileIDSet.length; x++) {
            for (int y = 0; y < tileIDSet[x].length; y++)
                tileIDSet[x][y] = 0;
        }
    }

    @Override
    public void generateTerrain() {

        applyCellularAutomata();
        smoothMap(5);
        cleanMapNoise();
        smoothMap(7);

        for (int i = 1; i < MAX_SEGMENTS; i++) {
            FINAL_DIRECTION_STATES.clear();

            if(CURRENT_DIRECTION != PathDirection.Hub.NONE)
                WORKING_POSITION = getWorkingPosition();
            else
                WORKING_POSITION = calculateRandomTileVector();

            CURRENT_DIRECTION = decideDirection();
            SEGMENT_LENGTH = randomSegmentLength();
            appendSegmentToPath();
        }
    }

    @Override
    public void populateTileIDSet() {

        for(int i = 0; i < INDIVIDUAL_SEGMENT_POSITIONS.size(); i++){
            for(int j = 0; j < INDIVIDUAL_SEGMENT_POSITIONS.get(i).length; j++)
                tileIDSet[INDIVIDUAL_SEGMENT_POSITIONS.get(i)[j].x][INDIVIDUAL_SEGMENT_POSITIONS.get(i)[j].y] = 0;
        }

        for(int i = 0; i < PATH.getNodes().size(); i++){
            PathDirection.Hub directionState = PATH.getNodes().get(i).getHubDirection();
            TileVector[] tileVector = PATH.getNodes().get(i).getTileVectorsAsArray();

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
                        tileIDSet[tileVectors.x][tileVectors.y + k] = 0;
                    else
                        tileIDSet[tileVectors.x + k][tileVectors.y] = 0;
                }
                for (int k = 0; k < PATH_PADDING; k++) {
                    if (isXAxis)
                        tileIDSet[tileVectors.x][tileVectors.y + k] = 0;
                    else
                        tileIDSet[tileVectors.x + k][tileVectors.y] = 0;
                }
            }
        }

        smoothMap(5);
    }

    private void applyCellularAutomata(){
        int fillPercent = 45;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (isWallTile(i, j))
                    tileIDSet[i][j] = randomTileID(CAVE_IDS);
                else
                    tileIDSet[i][j] = (RANDOM.nextInt(100) < fillPercent) ? randomTileID(CAVE_IDS) : 0;
            }
        }
    }

    private void cleanMapNoise(){

        int min_x_clean_padding = 20;
        int max_x_clean_padding = 160;
        int min_y_clean_padding = 20;
        int max_y_clean_padding = 270;

        for(int x = 0; x < height; x++){
            for(int y = 0; y < width; y++){
                if(x > offsetRandom(min_x_clean_padding) && x < offsetRandom(max_x_clean_padding) && y > offsetRandom(min_y_clean_padding) && y < offsetRandom(max_y_clean_padding))
                    tileIDSet[x][y] = randomTileID(CAVE_IDS);
            }
        }
    }

    private TileVector calculateRandomTileVector(){
        TileVector tileVector = new TileVector(
                RANDOM.nextInt(MapGenerator.PLATFORM_MAX_X - MapGenerator.PLATFORM_MIN_X) + MapGenerator.PLATFORM_MIN_X,
                RANDOM.nextInt(MapGenerator.PLATFORM_MAX_Y - MapGenerator.PLATFORM_MIN_Y) + MapGenerator.PLATFORM_MIN_Y);

        for(int i = 0; i < PATH.getNodes().size(); i++){
            for(int j = 0; j < PATH.getNodes().get(i).getTileVectorsAsArray().length; j++){
                if(tileVector.x() == PATH.getNodes().get(i).getTileVectorsAsArray()[j].x()
                        && tileVector.y() == PATH.getNodes().get(i).getTileVectorsAsArray()[j].y())
                    calculateRandomTileVector();
                else break;
            }
        }

        return tileVector;
    }

    private void appendSegmentToPath() {
        TileVector[] temp = new TileVector[SEGMENT_LENGTH];
        for(int i = 0; i < SEGMENT_LENGTH; i++)
            temp[i] = new TileVector(WORKING_POSITION.x + (i*CURRENT_DIRECTION.x), WORKING_POSITION.y + (i*CURRENT_DIRECTION.y));

        Node newestNode = new Node(temp, CURRENT_DIRECTION);
        PATH.getNodes().add(newestNode);
        INDIVIDUAL_SEGMENT_POSITIONS.add(newestNode.getTileVectorsAsArray());
    }

    private PathDirection.Hub decideDirection() {

        int upMaxPotential = WORKING_POSITION.x()
                - VERTICAL_SEGMENT_SIZES[VERTICAL_SEGMENT_SIZES.length - 1] - PATH_SPACING*2;
        int downMaxPotential = WORKING_POSITION.x()
                + VERTICAL_SEGMENT_SIZES[VERTICAL_SEGMENT_SIZES.length - 1] + PATH_SPACING*2;
        int leftMaxPotential = WORKING_POSITION.y()
                - HORIZONTAL_SEGMENT_SIZES[HORIZONTAL_SEGMENT_SIZES.length - 1] - PATH_SPACING*2;
        int rightMaxPotential = WORKING_POSITION.y()
                + HORIZONTAL_SEGMENT_SIZES[HORIZONTAL_SEGMENT_SIZES.length - 1] + PATH_SPACING*2;
        int upMinPotential = WORKING_POSITION.x()
                - VERTICAL_SEGMENT_SIZES[VERTICAL_SEGMENT_SIZES.length - 1] - PATH_SPACING*2;
        int downMinPotential = WORKING_POSITION.x()
                + VERTICAL_SEGMENT_SIZES[VERTICAL_SEGMENT_SIZES.length - 1] + PATH_SPACING*2;
        int leftMinPotential = WORKING_POSITION.y()
                - HORIZONTAL_SEGMENT_SIZES[HORIZONTAL_SEGMENT_SIZES.length - 1] - PATH_SPACING*2;
        int rightMinPotential = WORKING_POSITION.y()
                + HORIZONTAL_SEGMENT_SIZES[HORIZONTAL_SEGMENT_SIZES.length - 1] + PATH_SPACING*2;

        for (int i = 0; i < DIRECTION_POTENTIAL.length; i++)
            DIRECTION_POTENTIAL[i] = true;

        if (PATH.getNodes().isEmpty()) {
            DIRECTION_POTENTIAL[0] = false;
            DIRECTION_POTENTIAL[1] = false;
        }

        if (upMinPotential < MapGenerator.PLATFORM_MIN_X)
            DIRECTION_POTENTIAL[0] = false;
        if (downMinPotential > MapGenerator.PLATFORM_MAX_X)
            DIRECTION_POTENTIAL[1] = false;
        if (leftMinPotential < MapGenerator.PLATFORM_MIN_Y)
            DIRECTION_POTENTIAL[2] = false;
        if (rightMinPotential > MapGenerator.PLATFORM_MAX_Y)
            DIRECTION_POTENTIAL[3] = false;

        if (!PATH.getNodes().isEmpty()) {
            switch (getLastNode(1).getHubDirection()) {
                case UP:
                case DOWN:
                    DIRECTION_POTENTIAL[1] = false;
                    DIRECTION_POTENTIAL[0] = false;
                    break;
                case LEFT:
                    DIRECTION_POTENTIAL[3] = false;
                    break;
                case RIGHT:
                    DIRECTION_POTENTIAL[2] = false;
                    break;
            }
        }

        up_loop:
        for (int x = WORKING_POSITION.x; x > upMaxPotential; x--) {
            for (int i = 0; i < PATH.getNodes().size() - 1; i++) {
                TileVector[] currentSegment = PATH.getNodes().get(i).getTileVectorsAsArray();

                for (int j = 0; j < currentSegment.length; j++) {
                    if (x == currentSegment[j].x && WORKING_POSITION.y == currentSegment[j].y) {
                        DIRECTION_POTENTIAL[0] = false;
                        break up_loop;
                    }
                    for (int k = -PATH_SPACING; k < 0; k++) {
                        if (x == currentSegment[j].x && WORKING_POSITION.y + k == currentSegment[j].y) {
                            DIRECTION_POTENTIAL[0] = false;
                            break up_loop;
                        }
                    }
                    for (int k = 1; k <= PATH_SPACING; k++) {
                        if (x == currentSegment[j].x && WORKING_POSITION.y + k == currentSegment[j].y) {
                            DIRECTION_POTENTIAL[0] = false;
                            break up_loop;
                        }
                    }
                }
            }
        }

        down_loop:
        for (int x = WORKING_POSITION.x; x < downMaxPotential; x++) {
            for (int i = 0; i < PATH.getNodes().size() - 1; i++) {
                TileVector[] currentSegment = PATH.getNodes().get(i).getTileVectorsAsArray();

                for (int j = 0; j < currentSegment.length; j++) {
                    if (x == currentSegment[j].x && WORKING_POSITION.y == currentSegment[j].y) {
                        DIRECTION_POTENTIAL[1] = false;
                        break down_loop;
                    }
                    for (int k = -PATH_SPACING; k < 0; k++) {
                        if (x == currentSegment[j].x && WORKING_POSITION.y + k == currentSegment[j].y) {
                            DIRECTION_POTENTIAL[1] = false;
                            break down_loop;
                        }
                    }
                    for (int k = 1; k <= PATH_SPACING; k++) {
                        if (x == currentSegment[j].x && WORKING_POSITION.y + k == currentSegment[j].y) {
                            DIRECTION_POTENTIAL[1] = false;
                            break down_loop;
                        }
                    }
                }
            }
        }

        left_loop:
        for (int y = WORKING_POSITION.y; y > leftMaxPotential; y--) {
            for (int i = 0; i < PATH.getNodes().size() - 1; i++) {
                TileVector[] currentSegment = PATH.getNodes().get(i).getTileVectorsAsArray();

                for (int j = 0; j < currentSegment.length; j++) {
                    if (WORKING_POSITION.x == currentSegment[j].x && y == currentSegment[j].y) {
                        DIRECTION_POTENTIAL[2] = false;
                        break left_loop;
                    }
                    for (int k = -PATH_SPACING; k < 0; k++) {
                        if (y == currentSegment[j].y && WORKING_POSITION.x + k == currentSegment[j].x) {
                            DIRECTION_POTENTIAL[2] = false;
                            break left_loop;
                        }
                    }
                    for (int k = 1; k <= PATH_SPACING; k++) {
                        if (y == currentSegment[j].y && WORKING_POSITION.x + k == currentSegment[j].x) {
                            DIRECTION_POTENTIAL[2] = false;
                            break left_loop;
                        }
                    }
                }
            }
        }

        right_loop:
        for (int y = WORKING_POSITION.y; y < rightMaxPotential; y++) {
            for (int i = 0; i < PATH.getNodes().size() - 1; i++) {
                TileVector[] currentSegment = PATH.getNodes().get(i).getTileVectorsAsArray();

                for (int j = 0; j < currentSegment.length; j++) {
                    if (WORKING_POSITION.x == currentSegment[j].x && y == currentSegment[j].y) {
                        DIRECTION_POTENTIAL[3] = false;
                        break right_loop;
                    }
                    for (int k = -PATH_SPACING; k < 0; k++) {
                        if (y == currentSegment[j].y && WORKING_POSITION.x + k == currentSegment[j].x) {
                            DIRECTION_POTENTIAL[3] = false;
                            break right_loop;
                        }
                    }
                    for (int k = 1; k <= PATH_SPACING; k++) {
                        if (y == currentSegment[j].y && WORKING_POSITION.x + k == currentSegment[j].x) {
                            DIRECTION_POTENTIAL[3] = false;
                            break right_loop;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < DIRECTION_POTENTIAL.length; i++) {
            if (DIRECTION_POTENTIAL[i])
                break;
            else if (i == DIRECTION_POTENTIAL.length - 1)
                return PathDirection.Hub.NONE;
        }

        for (int i = 0; i < DIRECTION_POTENTIAL.length; i++) {
            if (DIRECTION_POTENTIAL[i])
                FINAL_DIRECTION_STATES.add(DIRECTION_STATES[i]);
        }

        if (!FINAL_DIRECTION_STATES.isEmpty())
            return FINAL_DIRECTION_STATES.get(RANDOM.nextInt(FINAL_DIRECTION_STATES.size()));
        else
            return PathDirection.Hub.NONE;
    }

    private Node getLastNode(int previousNumber) {
        return PATH.getNodes().get(PATH.getNodes().size() - previousNumber);
    }

    private TileVector getWorkingPosition() {
        return getLastNode(1).getEndTilePos();
    }

    private int randomSegmentLength() {

        switch (CURRENT_DIRECTION) {
            case LEFT:
            case RIGHT:
                return HORIZONTAL_SEGMENT_SIZES[RANDOM.nextInt(HORIZONTAL_SEGMENT_SIZES.length)];
            case UP:
            case DOWN:
                return VERTICAL_SEGMENT_SIZES[RANDOM.nextInt(VERTICAL_SEGMENT_SIZES.length)];
        }

        return 0;
    }
}
