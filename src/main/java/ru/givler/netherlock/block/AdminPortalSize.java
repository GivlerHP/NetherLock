package ru.givler.netherlock.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import ru.givler.netherlock.NetherLock;

public class AdminPortalSize {

    private final World world;
    private final int axis;
    private final int dirWidth;
    private final int dirOther;
    private int portalCount = 0;
    private ChunkCoordinates start;
    private int width;
    private int height;

    public AdminPortalSize(World world, int x, int y, int z, int axis) {
        this.world = world;
        this.axis = axis;
        this.dirWidth = BlockPortal.field_150001_a[axis][1];
        this.dirOther = BlockPortal.field_150001_a[axis][0];

        while (y > 0 && isEmptyBlock(world.getBlock(x, y - 1, z))) {
            y--;
        }

        int leftEdge = findEdge(x, y, z, dirOther) - 1;
        if (leftEdge >= 0) {
            int sx = x + leftEdge * Direction.offsetX[dirOther];
            int sz = z + leftEdge * Direction.offsetZ[dirOther];
            this.start = new ChunkCoordinates(sx, y, sz);
            this.width = findEdge(sx, y, sz, dirWidth);
            if (this.width < 2 || this.width > 21) {
                this.start = null;
                this.width = 0;
            }
        }

        if (this.start != null) {
            this.height = findHeight();
        }
    }

    private int findEdge(int x, int y, int z, int dir) {
        int ox = Direction.offsetX[dir];
        int oz = Direction.offsetZ[dir];

        int i;
        for (i = 0; i < 22; i++) {
            Block block = world.getBlock(x + ox * i, y, z + oz * i);
            if (!isEmptyBlock(block)) break;

            Block below = world.getBlock(x + ox * i, y - 1, z + oz * i);
            if (below != NetherLock.adminObsidian) break;
        }

        Block edge = world.getBlock(x + ox * i, y, z + oz * i);
        return edge == NetherLock.adminObsidian ? i : 0;
    }

    private int findHeight() {
        int owX = Direction.offsetX[dirWidth];
        int owZ = Direction.offsetZ[dirWidth];

        int h;
        outer:
        for (h = 0; h < 21; h++) {
            int y = start.posY + h;
            for (int i = 0; i < width; i++) {
                int bx = start.posX + i * owX;
                int bz = start.posZ + i * owZ;
                Block block = world.getBlock(bx, y, bz);
                if (!isEmptyBlock(block)) break outer;

                if (block == NetherLock.adminPortal) portalCount++;

                if (i == 0) {
                    Block side = world.getBlock(bx + Direction.offsetX[dirOther], y, bz + Direction.offsetZ[dirOther]);
                    if (side != NetherLock.adminObsidian) break outer;
                } else if (i == width - 1) {
                    Block side = world.getBlock(bx + Direction.offsetX[dirWidth], y, bz + Direction.offsetZ[dirWidth]);
                    if (side != NetherLock.adminObsidian) break outer;
                }
            }
        }

        for (int i = 0; i < width; i++) {
            int bx = start.posX + i * owX;
            int bz = start.posZ + i * owZ;
            if (world.getBlock(bx, start.posY + h, bz) != NetherLock.adminObsidian) {
                h = 0;
                break;
            }
        }

        if (h >= 3 && h <= 21) return h;
        start = null;
        width = 0;
        return 0;
    }

    private boolean isEmptyBlock(Block block) {
        return block.getMaterial() == Material.air
                || block == Blocks.fire
                || block == NetherLock.adminPortal;
    }


    public void clearPortalBlocks() {
        int owX = Direction.offsetX[dirWidth];
        int owZ = Direction.offsetZ[dirWidth];
        for (int i = 0; i < width; i++) {
            int bx = start.posX + i * owX;
            int bz = start.posZ + i * owZ;
            for (int j = 0; j < height; j++) {
                Block block = world.getBlock(bx, start.posY + j, bz);
                if (block == NetherLock.adminPortal) {
                    world.setBlock(bx, start.posY + j, bz, Blocks.air, 0, 3);
                }
            }
        }
    }

    public boolean isValid() {
        return start != null && width >= 2 && width <= 21 && height >= 3 && height <= 21;
    }

    public int getPortalCount() { return portalCount; }
    public int getWidth()       { return width; }
    public int getHeight()      { return height; }

    public void fillWithAdminPortal() {
        int owX = Direction.offsetX[dirWidth];
        int owZ = Direction.offsetZ[dirWidth];
        for (int i = 0; i < width; i++) {
            int bx = start.posX + i * owX;
            int bz = start.posZ + i * owZ;
            for (int j = 0; j < height; j++) {
                world.setBlock(bx, start.posY + j, bz, NetherLock.adminPortal, 0, 3);
            }
        }
    }
}