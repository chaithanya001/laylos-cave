package com.heynaveed.layloscave.utils.maps;

import java.util.ArrayList;


final class TreePath {

    private static final int MINIMUM_X_DIFFERENCE = 8;
    private static final int MIN_PLATFORM_LENGTH = 5;
    private static final int CHILDREN_PER_PARENT = 4;
    private static final ArrayList<TreeNode> TREE_NODEs = new ArrayList<TreeNode>();
    private static final ArrayList<TileVector[]> individualPlatformPositions = new ArrayList<TileVector[]>();
    private static final ArrayList<TileVector> globalPlatformPositions = new ArrayList<TileVector>();

    public TreePath build(int layerNum, TileVector[] rootTileVector){
        initialise(rootTileVector);
        createTree(layerNum);
        return this;
    }

    private void initialise(TileVector[] rootTileVectors){
//        TileVector[] rootTileVectors = calculateRootTileVector();
        TreeNode rootTreeNode = new TreeNode(rootTileVectors).isRootNode(true);
        TREE_NODEs.add(rootTreeNode);
        individualPlatformPositions.add(rootTileVectors);
        globalPlatformPositions.addAll(rootTreeNode.getTileVectorsAsList());
    }

    private TileVector[] calculateRootTileVector(){
        int rootPlatformLength = 8;
        TileVector[] rootTileVector = new TileVector[rootPlatformLength];
        int xPos;
        int yPos;

        for(int i = 0; i < rootPlatformLength; i++){
            xPos = (MapGenerator.PLATFORM_MAX_X + MapGenerator.PLATFORM_MIN_X)/2;
            yPos = (MapGenerator.PLATFORM_MAX_Y + MapGenerator.PLATFORM_MIN_Y)/2 - rootPlatformLength/2 + i;
            rootTileVector[i] = new TileVector(xPos, yPos);
        }

        return rootTileVector;
    }

    private void createTree(int layerNum){
        for(int i = 1; i <= layerNum; i++){
            for(int j = 1; j <= Math.pow(CHILDREN_PER_PARENT, i); j++) {
//                int platformLength = random.nextInt(5)+ MIN_PLATFORM_LENGTH;
                int platformLength = 8;
                int childNumber = (TREE_NODEs.size() % CHILDREN_PER_PARENT);
                int platformSpacing = 8;
//                int platformSpacing = random.nextInt(4) + MINIMUM_X_DIFFERENCE;
                childNumber = childNumber == 0 ?4 :childNumber;
                TreeNode parentTreeNode = TREE_NODEs.get(
                        calculateParentIndex(TREE_NODEs.size(), childNumber));
                TileVector[] tilePos = new TileVector[platformLength];


                switch(childNumber){
                    case 1:
                        for(int k = 0; k < tilePos.length; k++){
                            tilePos[k] = new TileVector(
                                    parentTreeNode.getLeftTilePos().x()- platformSpacing,
                                    parentTreeNode.getLeftTilePos().y() - tilePos.length + k - 1);
                        }
                        break;
                    case 2:
                        for(int k = 0; k < tilePos.length; k++){
                            tilePos[k] = new TileVector(
                                    parentTreeNode.getRightTilePos().x()- platformSpacing,
                                    parentTreeNode.getRightTilePos().y() + k + 1);
                        }
                        break;
                    case 3:
                        for(int k = 0; k < tilePos.length; k++){
                            tilePos[k] = new TileVector(
                                    parentTreeNode.getLeftTilePos().x()+ platformSpacing,
                                    parentTreeNode.getLeftTilePos().y() - tilePos.length + k - 1);
                        }
                        break;
                    case 4:
                        for(int k = 0; k < tilePos.length; k++){
                            tilePos[k] = new TileVector(
                                    parentTreeNode.getRightTilePos().x()+ platformSpacing,
                                    parentTreeNode.getRightTilePos().y() + k + 1);
                        }
                        break;
                }

                TreeNode newPlatform;
                boolean shouldSkip = false;

                check_loop:
                for(int k = 0; k < tilePos.length; k++) {
                    for(int l = 0; l < globalPlatformPositions.size(); l++){
                        TileVector tV = globalPlatformPositions.get(l);
                        if(tilePos[k] == tV
                                || (Math.abs(tilePos[k].x() - tV.x()) < MINIMUM_X_DIFFERENCE && Math.abs(tilePos[k].y() - tV.y()) < MIN_PLATFORM_LENGTH)
                                || tilePos[k].x() < MapGenerator.PLATFORM_MIN_X
                                || tilePos[k].x() > MapGenerator.PLATFORM_MAX_Y
                                || tilePos[k].y() < MapGenerator.PLATFORM_MIN_Y
                                || tilePos[k].y() > MapGenerator.PLATFORM_MAX_Y) {
                            shouldSkip = true;
                            break check_loop;
                        }
                    }
                }

                newPlatform = new TreeNode(tilePos);
                TREE_NODEs.add(newPlatform);

                if(!shouldSkip) {
                    globalPlatformPositions.addAll(newPlatform.getTileVectorsAsList());
                    individualPlatformPositions.add(newPlatform.getTileVectorsAsArray());
                }
            }
        }

//        System.out.println("Total TreePath Nodes: " + TREE_NODEs.size());
//        System.out.println("Actual Platforms: " + individualPlatformPositions.size());
    }

    private int calculateChildIndex(int parentIndex, int childNumber){
        return parentIndex*CHILDREN_PER_PARENT + childNumber;
    }

    private int calculateParentIndex(int childIndex, int childNumber){
        return (childIndex - childNumber)/CHILDREN_PER_PARENT;
    }

    public TileVector[] getGlobalPlatformPositions(){
        return globalPlatformPositions.toArray(new TileVector[globalPlatformPositions.size()]);
    }

    public ArrayList<TileVector[]> getIndividualPlatformPositions(){
        return individualPlatformPositions;
    }
}
