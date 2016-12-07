package com.heynaveed.layloscave.utils.maps;


import java.util.ArrayList;
import java.util.Random;

public class CavernPath {

    private static final Random random = new Random();
    private static final int MAX_BLOCK_PER_PATH = 12;
    private static final int MAX_BLOCKS = 16;
    private final ArrayList<Cavern> caverns;
    private final ArrayList<Integer> cavernBlockPath;

    public CavernPath(){
        caverns = new ArrayList<Cavern>();
        cavernBlockPath = new ArrayList<Integer>();
        initBlocks();
        determineBlockPathways();
    }

    private void initBlocks(){
        for(int i = 1; i <= MAX_BLOCKS; i++)
            caverns.add(new Cavern(i));
    }

    private void determineBlockPathways(){

        int startingBlockPosition = random.nextInt(MAX_BLOCKS);
        cavernBlockPath.add(startingBlockPosition);
        caverns.get(startingBlockPosition).setStartBlock(true).setPathBlock(true);

        for(int i = 1; i < MAX_BLOCK_PER_PATH; i++) {
            Cavern currentBlock = caverns.get(cavernBlockPath.get(cavernBlockPath.size()-1));
            ArrayList<PathDirection.Cavern> potentialDirections = new ArrayList<PathDirection.Cavern>();

            if (currentBlock.getBlockNumber() > 4)
                potentialDirections.add(PathDirection.Cavern.UP);
            if (currentBlock.getBlockNumber() < 13)
                potentialDirections.add(PathDirection.Cavern.DOWN);
            if (currentBlock.getBlockNumber() % 4 != 0)
                potentialDirections.add(PathDirection.Cavern.RIGHT);
            if (currentBlock.getBlockNumber() % 4 != 1)
                potentialDirections.add(PathDirection.Cavern.LEFT);

            check_loop:
            for (int j = 0; j < potentialDirections.size(); j++) {
                for (int k = 0; k < cavernBlockPath.size(); k++) {
                    if (currentBlock.getBlockNumber() + potentialDirections.get(j).direction == cavernBlockPath.get(k) + 1) {
                        potentialDirections.remove(j);
                        j--;
                        continue check_loop;
                    }
                }
            }

            if(i == 1){
                potentialDirections.remove(PathDirection.Cavern.DOWN);
                potentialDirections.remove(PathDirection.Cavern.UP);
            }

            if(!potentialDirections.isEmpty()) {
                PathDirection.Cavern pathDirection = potentialDirections.get(random.nextInt(potentialDirections.size()));
                int nextBlockPosition = (currentBlock.getBlockNumber()-1) + pathDirection.direction;
                cavernBlockPath.add(nextBlockPosition);
                caverns.get(nextBlockPosition).setPathBlock(true);
                caverns.get(currentBlock.getBlockNumber()-1).setDirection(pathDirection);
            }
            else break;
        }
    }

    public ArrayList<Cavern> getCaverns(){
        return caverns;
    }

    public ArrayList<Integer> getCavernBlockPath(){
        return cavernBlockPath;
    }
}
