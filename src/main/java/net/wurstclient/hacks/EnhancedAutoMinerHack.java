package net.wurstclient.hacks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.ai.PathFinder;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.util.BlockBreaker;

import java.util.*;
import java.util.stream.Collectors;

@SearchTags({"auto mine", "AutoBreak", "auto break", "EnhancedAutoMinerHack", "Enhanced Auto Miner Hack"})
public class EnhancedAutoMinerHack extends Hack implements UpdateListener {

    private PathFinder pathFinder;
    private BlockPos nextOreToMine;
    private final int radius = 10;


    public EnhancedAutoMinerHack()
    {
        super("EnhancedAutoMinerHack");
        setCategory(Category.BLOCKS);
    }
    @Override
    public void onUpdate() {
        if (pathFinder == null || MC.player == null) {
            return;
        }

        // Continue path finding or moving
        if (!pathFinder.isDone() && !pathFinder.isFailed()) {
            pathFinder.think(); // Continue thinking towards the target

            if (pathFinder.isDone()) {
                // Reached the ore, mine it
                mineBlock(nextOreToMine);
                // Prepare for the next ore, if any
                findOresAndStartPathFinding();
            }

            if (pathFinder.isFailed()){
                System.out.println("Failed to find path");
            }
        }
    }

    @Override
    public void onEnable()
    {
        findOresAndStartPathFinding();

        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    public void onDisable()
    {
        EVENTS.remove(UpdateListener.class, this);
    }

    public void findOresAndStartPathFinding() {
        BlockPos playerPos = MC.player.getBlockPos();
        Map<BlockPos, BlockState> ores = findOresAroundPlayer(MC.world, playerPos, radius); // Example radius

        if (!ores.isEmpty()) {
            // Sort the ore positions by distance to the player
            List<BlockPos> sortedOrePositions = ores.keySet().stream()
                    .sorted(Comparator.comparingDouble(pos -> pos.getSquaredDistance(playerPos)))
                    .collect(Collectors.toList());

            // Get the closest ore
            nextOreToMine = sortedOrePositions.get(0);
            pathFinder = new PathFinder(nextOreToMine);
            pathFinder.think(); // Start thinking towards the first ore
        }
        else {
            onAllOresMined();
        }
    }

//    public BlockPos getNextStepTowardsTarget() {
//        // This assumes 'path' is a List<BlockPos> representing the calculated path to the target.
//        // You might need to adapt this based on how your PathFinder class stores the path.
//        if (pathFinder.getPath() != null && !pathFinder.getPath().isEmpty() && pathFinder.getPath().size() > 1) {
//            // The next step is the second item in the path list (index 1),
//            // assuming index 0 is the player's current position.
//            return pathFinder.getPath().get(1);
//        }
//        return null;
//    }



    private void mineBlock(BlockPos blockPos) {
        if(MC.player.getAbilities().creativeMode)
            BlockBreaker.breakBlocksWithPacketSpam(Arrays.asList(blockPos));
        else
            BlockBreaker.breakOneBlock(blockPos);
    }

    private void onAllOresMined() {
        // Reset the pathfinder and ore target
        pathFinder = null;
        nextOreToMine = null;

        // Turn off
        WURST.getHax().enhancedAutoMinerHack.setEnabled(false);
    }


    //Moving east increases the X coordinate.
    //Moving west decreases the X coordinate.
    //Moving south increases the Z coordinate.
    //Moving north decreases the Z coordinate.
//    public void rotatePlayerToDirection(ClientPlayerEntity player, Direction direction) {
//        if (player == null || direction == null) {
//            return; // Early exit if player or direction is null
//        }
//
//        float newYaw = 0.0F; // Default to South
//        switch (direction) {
//            case NORTH:
//                newYaw = 180.0F;
//                break;
//            case SOUTH:
//                newYaw = 0.0F;
//                break;
//            case EAST:
//                newYaw = -90.0F;
//                break;
//            case WEST:
//                newYaw = 90.0F;
//                break;
//            default:
//                return; // Invalid direction, early exit
//        }
//
//        // Set the player's yaw. Note: This doesn't instantly snap the camera, it changes the orientation which might be smoothed by the client.
//        player.setYaw(newYaw);
//
//        // Optionally, set the head rotation to match the body if you want immediate visual feedback.
//        player.headYaw = newYaw;
//    }

//    public Direction getPlayerLookDirection(ClientPlayerEntity player) {
//        // Ensure the player object is not null
//        if (player == null) {
//            return null;
//        }
//
//        // Get the yaw rotation
//        float yaw = player.getYaw(1.0F);
//
//        // Normalize yaw to 0-360 range
//        yaw = (yaw % 360 + 360) % 360;
//
//        if(yaw >= 45 && yaw < 135) {
//            return Direction.EAST;
//        } else if(yaw >= 135 && yaw < 225) {
//            return Direction.SOUTH;
//        } else if(yaw >= 225 && yaw < 315) {
//            return Direction.WEST;
//        } else {
//            return Direction.NORTH;
//        }
//    }

    private enum Direction {
        NORTH, SOUTH, EAST, WEST
    }

    /**
     * Gets the blocks around the player within a specified radius.
     *
     * @param world The world the player is in.
     * @param playerPos The current position of the player.
     * @param radius The radius around the player to check for blocks.
     * @return A map of block positions to their corresponding block states.
     */
    public Map<BlockPos, BlockState> findOresAroundPlayer(World world, BlockPos playerPos, int radius) {
        Map<BlockPos, BlockState> ores = new HashMap<>();
        // Define the range based on the given radius
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    BlockState blockState = world.getBlockState(pos);
                    if (isOreBlock(blockState.getBlock())) {
                        ores.put(pos, blockState);
                    }
                }
            }
        }
        return ores;
    }

    private boolean isOreBlock(Block block) {
        // This method should return true if the block is considered an ore.
        // You might need to adjust the logic based on your definition of what constitutes an ore.
        // For demonstration, let's consider a few common ores:
        return block == Blocks.COAL_ORE || block == Blocks.IRON_ORE || block == Blocks.GOLD_ORE ||
                block == Blocks.DIAMOND_ORE || block == Blocks.EMERALD_ORE || block == Blocks.LAPIS_ORE ||
                block == Blocks.REDSTONE_ORE || block == Blocks.NETHER_GOLD_ORE || block == Blocks.NETHER_QUARTZ_ORE;
    }
}


