package com.heynaveed.layloscave.utils.maps;


import java.util.ArrayList;
import java.util.Random;

/**
 * Created by naveed.shihab on 16/11/2016.
 */

final class HubMap {

    private static final Random random = new Random();

    static TileVector WORKING_POSITION;
    static PathDirection.Hub CURRENT_DIRECTION;
    static int SEGMENT_LENGTH;

    private static final int PATH_SPACING = 7;
    private static final int MAX_SEGMENTS = 250;
    private static final int[] VERTICAL_SEGMENT_SIZES = {8, 9, 10, 11, 12, 13};
    private static final int[] HORIZONTAL_SEGMENT_SIZES = {20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35};
    private static final ArrayList<Segment> PATH_HUB_SEGMENTS = new ArrayList<Segment>();
    private static final ArrayList<TileVector[]> individualSegmentPositions = new ArrayList<TileVector[]>();
    private static final PathDirection.Hub[] directionStates = truncateDirectionArray(PathDirection.Hub.values());
    private static final boolean[] directionPotential = new boolean[directionStates.length];
    private static final ArrayList<PathDirection.Hub> finalDirectionStates = new ArrayList<PathDirection.Hub>();


    public HubMap build(){
        initialise();
        createPath();
//        System.out.println("Segments: " + PATH_HUB_SEGMENTS.size());
        return this;
    }

    private void initialise() {
        PATH_HUB_SEGMENTS.clear();
        individualSegmentPositions.clear();
        finalDirectionStates.clear();

        WORKING_POSITION = new TileVector(
                random.nextInt(MapGenerator.PLATFORM_MAX_X - MapGenerator.PLATFORM_MIN_X) + MapGenerator.PLATFORM_MIN_X,
                random.nextInt(MapGenerator.PLATFORM_MAX_Y - MapGenerator.PLATFORM_MIN_Y) + MapGenerator.PLATFORM_MIN_Y);
        CURRENT_DIRECTION = decideDirection();
//        printCurrentDirection();
        SEGMENT_LENGTH = 15;
        appendSegment();
    }

    private void createPath() {
        for (int i = 1; i < MAX_SEGMENTS; i++) {
            finalDirectionStates.clear();

            if(CURRENT_DIRECTION != PathDirection.Hub.NONE)
                WORKING_POSITION = getWorkingPosition();
            else
                WORKING_POSITION = calculateRandomTileVector();

            CURRENT_DIRECTION = decideDirection();

//            printCurrentDirection();
            SEGMENT_LENGTH = randomSegmentLength();
            appendSegment();
        }

//        printAveragePoint();
    }

    private TileVector calculateRandomTileVector(){
        TileVector tileVector = new TileVector(
                random.nextInt(MapGenerator.PLATFORM_MAX_X - MapGenerator.PLATFORM_MIN_X) + MapGenerator.PLATFORM_MIN_X,
                random.nextInt(MapGenerator.PLATFORM_MAX_Y - MapGenerator.PLATFORM_MIN_Y) + MapGenerator.PLATFORM_MIN_Y);

        for(int i = 0; i < PATH_HUB_SEGMENTS.size(); i++){
            for(int j = 0; j < PATH_HUB_SEGMENTS.get(i).getTileVectorsAsArray().length; j++){
                if(tileVector.x() == PATH_HUB_SEGMENTS.get(i).getTileVectorsAsArray()[j].x()
                        && tileVector.y() == PATH_HUB_SEGMENTS.get(i).getTileVectorsAsArray()[j].y())
                    calculateRandomTileVector();
                else break;
            }
        }

        return tileVector;
    }

    private void printAveragePoint(){
        float x = 0;
        float y = 0;

        for(int i = 0; i < PATH_HUB_SEGMENTS.size(); i++){
            float tempX = 0;
            float tempY = 0;
            for(int j = 0; j < PATH_HUB_SEGMENTS.get(i).getTileVectorsAsArray().length; j++){
                tempX += PATH_HUB_SEGMENTS.get(i).getTileVectorsAsArray()[j].x();
                tempY += PATH_HUB_SEGMENTS.get(i).getTileVectorsAsArray()[j].y();
            }
            x += (tempX/ PATH_HUB_SEGMENTS.get(i).getTileVectorsAsArray().length);
            y += (tempY/ PATH_HUB_SEGMENTS.get(i).getTileVectorsAsArray().length);
        }

        x = x / PATH_HUB_SEGMENTS.size();
        y = y / PATH_HUB_SEGMENTS.size();

        System.out.println("AVERAGE POINT: (" + (int)y + ", " + (int)x + ")");
    }

    private void appendSegment() {
        Segment newestSegment = new Segment();
        PATH_HUB_SEGMENTS.add(newestSegment);
        individualSegmentPositions.add(newestSegment.getTileVectorsAsArray());
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

        for (int i = 0; i < directionPotential.length; i++)
            directionPotential[i] = true;

        if (PATH_HUB_SEGMENTS.isEmpty()) {
            directionPotential[0] = false;
            directionPotential[1] = false;
        }

//        printDirectionPotential("After First Segment Check");

        if (upMinPotential < MapGenerator.PLATFORM_MIN_X)
            directionPotential[0] = false;
        if (downMinPotential > MapGenerator.PLATFORM_MAX_X)
            directionPotential[1] = false;
        if (leftMinPotential < MapGenerator.PLATFORM_MIN_Y)
            directionPotential[2] = false;
        if (rightMinPotential > MapGenerator.PLATFORM_MAX_Y)
            directionPotential[3] = false;

//        printDirectionPotential("After Map Limitation Check");

        if (!PATH_HUB_SEGMENTS.isEmpty()) {
            switch (getLastSegment(1).getDirection()) {
                case UP:
                case DOWN:
                    directionPotential[1] = false;
                    directionPotential[0] = false;
                    break;
                case LEFT:
                    directionPotential[3] = false;
                    break;
                case RIGHT:
                    directionPotential[2] = false;
                    break;
            }
        }

//        printDirectionPotential("After Previous Segment Check");

        up_loop:
        for (int x = WORKING_POSITION.x; x > upMaxPotential; x--) {
            for (int i = 0; i < PATH_HUB_SEGMENTS.size() - 1; i++) {
                TileVector[] currentSegment = PATH_HUB_SEGMENTS.get(i).getTileVectorsAsArray();

                for (int j = 0; j < currentSegment.length; j++) {
                    if (x == currentSegment[j].x && WORKING_POSITION.y == currentSegment[j].y) {
//                        System.out.println("[" + currentSegment[j].y + ", " + currentSegment[j].x + "]");
                        directionPotential[0] = false;
                        break up_loop;
                    }
                    for (int k = -PATH_SPACING; k < 0; k++) {
                        if (x == currentSegment[j].x && WORKING_POSITION.y + k == currentSegment[j].y) {
//                            System.out.println("[" + currentSegment[j].y + ", " + currentSegment[j].x + "]");
                            directionPotential[0] = false;
                            break up_loop;
                        }
                    }
                    for (int k = 1; k <= PATH_SPACING; k++) {
                        if (x == currentSegment[j].x && WORKING_POSITION.y + k == currentSegment[j].y) {
//                            System.out.println("[" + currentSegment[j].y + ", " + currentSegment[j].x + "]");
                            directionPotential[0] = false;
                            break up_loop;
                        }
                    }
                }
            }
        }

//        printDirectionPotential("After Up Direction Check");

        down_loop:
        for (int x = WORKING_POSITION.x; x < downMaxPotential; x++) {
            for (int i = 0; i < PATH_HUB_SEGMENTS.size() - 1; i++) {
                TileVector[] currentSegment = PATH_HUB_SEGMENTS.get(i).getTileVectorsAsArray();

                for (int j = 0; j < currentSegment.length; j++) {
                    if (x == currentSegment[j].x && WORKING_POSITION.y == currentSegment[j].y) {
//                        System.out.println("[" + currentSegment[j].y + ", " + currentSegment[j].x + "]");
                        directionPotential[1] = false;
                        break down_loop;
                    }
                    for (int k = -PATH_SPACING; k < 0; k++) {
                        if (x == currentSegment[j].x && WORKING_POSITION.y + k == currentSegment[j].y) {
//                            System.out.println("[" + currentSegment[j].y + ", " + currentSegment[j].x + "]");
                            directionPotential[1] = false;
                            break down_loop;
                        }
                    }
                    for (int k = 1; k <= PATH_SPACING; k++) {
                        if (x == currentSegment[j].x && WORKING_POSITION.y + k == currentSegment[j].y) {
//                            System.out.println("[" + currentSegment[j].y + ", " + currentSegment[j].x + "]");
                            directionPotential[1] = false;
                            break down_loop;
                        }
                    }
                }
            }
        }

//        printDirectionPotential("After Down Direction Check");

        left_loop:
        for (int y = WORKING_POSITION.y; y > leftMaxPotential; y--) {
            for (int i = 0; i < PATH_HUB_SEGMENTS.size() - 1; i++) {
                TileVector[] currentSegment = PATH_HUB_SEGMENTS.get(i).getTileVectorsAsArray();

                for (int j = 0; j < currentSegment.length; j++) {
                    if (WORKING_POSITION.x == currentSegment[j].x && y == currentSegment[j].y) {
//                        System.out.println("[" + currentSegment[j].y + ", " + currentSegment[j].x + "]");
                        directionPotential[2] = false;
                        break left_loop;
                    }
                    for (int k = -PATH_SPACING; k < 0; k++) {
                        if (y == currentSegment[j].y && WORKING_POSITION.x + k == currentSegment[j].x) {
//                            System.out.println("[" + currentSegment[j].y + ", " + currentSegment[j].x + "]");
                            directionPotential[2] = false;
                            break left_loop;
                        }
                    }
                    for (int k = 1; k <= PATH_SPACING; k++) {
                        if (y == currentSegment[j].y && WORKING_POSITION.x + k == currentSegment[j].x) {
//                            System.out.println("[" + currentSegment[j].y + ", " + currentSegment[j].x + "]");
                            directionPotential[2] = false;
                            break left_loop;
                        }
                    }
                }
            }
        }

//        printDirectionPotential("After Left Direction Check");

        right_loop:
        for (int y = WORKING_POSITION.y; y < rightMaxPotential; y++) {
            for (int i = 0; i < PATH_HUB_SEGMENTS.size() - 1; i++) {
                TileVector[] currentSegment = PATH_HUB_SEGMENTS.get(i).getTileVectorsAsArray();

                for (int j = 0; j < currentSegment.length; j++) {
                    if (WORKING_POSITION.x == currentSegment[j].x && y == currentSegment[j].y) {
//                        System.out.println("[" + currentSegment[j].y + ", " + currentSegment[j].x + "]");
                        directionPotential[3] = false;
                        break right_loop;
                    }
                    for (int k = -PATH_SPACING; k < 0; k++) {
                        if (y == currentSegment[j].y && WORKING_POSITION.x + k == currentSegment[j].x) {
//                            System.out.println("[" + currentSegment[j].y + ", " + currentSegment[j].x + "]");
                            directionPotential[3] = false;
                            break right_loop;
                        }
                    }
                    for (int k = 1; k <= PATH_SPACING; k++) {
                        if (y == currentSegment[j].y && WORKING_POSITION.x + k == currentSegment[j].x) {
//                            System.out.println("[" + currentSegment[j].y + ", " + currentSegment[j].x + "]");
                            directionPotential[3] = false;
                            break right_loop;
                        }
                    }
                }
            }
        }

//        printDirectionPotential("After Right Direction Check");

        for (int i = 0; i < directionPotential.length; i++) {
            if (directionPotential[i])
                break;
            else if (i == directionPotential.length - 1)
                return PathDirection.Hub.NONE;
        }

//        printDirectionPotential("Final Array");

        for (int i = 0; i < directionPotential.length; i++) {
            if (directionPotential[i])
                finalDirectionStates.add(directionStates[i]);
        }

        if (!finalDirectionStates.isEmpty())
            return finalDirectionStates.get(random.nextInt(finalDirectionStates.size()));
        else
            return PathDirection.Hub.NONE;
    }

    private Segment getLastSegment(int previousNumber) {
        return PATH_HUB_SEGMENTS.get(PATH_HUB_SEGMENTS.size() - previousNumber);
    }

    private TileVector getWorkingPosition() {
        return getLastSegment(1).getEndTilePos();
    }

    private void printDirectionPotential(String title) {
        System.out.println("============================================================================================");
        System.out.println(title + "\t-\tSegment No.: " + Integer.toString(PATH_HUB_SEGMENTS.size() + 1) + "\t-\tWorking Pos.: (" + WORKING_POSITION.x + ", " + WORKING_POSITION.y + ")");
        System.out.println("============================================================================================");
        for (int i = 0; i < directionPotential.length; i++)
            System.out.println(directionStates[i] + ": " + directionPotential[i]);
    }

    private void printCurrentDirection() {
        System.out.println("********************************************************************************************");
        System.out.println("Result: " + CURRENT_DIRECTION);
        System.out.println("********************************************************************************************");
    }

    private int randomSegmentLength() {

        switch (CURRENT_DIRECTION) {
            case LEFT:
            case RIGHT:
                return HORIZONTAL_SEGMENT_SIZES[random.nextInt(HORIZONTAL_SEGMENT_SIZES.length)];
            case UP:
            case DOWN:
                return VERTICAL_SEGMENT_SIZES[random.nextInt(VERTICAL_SEGMENT_SIZES.length)];
        }

        return 0;
    }

    private static PathDirection.Hub[] truncateDirectionArray(PathDirection.Hub[] array) {
        PathDirection.Hub[] temp = new PathDirection.Hub[array.length - 1];

        for (int i = 0; i < temp.length; i++)
            temp[i] = array[i];

        return temp;
    }

    public ArrayList<Segment> getPathSegments() {
        return PATH_HUB_SEGMENTS;
    }

    public ArrayList<TileVector[]> getIndividualSegmentPositions() {
        return individualSegmentPositions;
    }
}
