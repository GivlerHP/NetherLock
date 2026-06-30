package ru.givler.netherlock.teleport;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Direction;
import net.minecraft.util.LongHashMap;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import ru.givler.netherlock.NetherLock;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class AdminTeleporter extends Teleporter {

    private final WorldServer worldServerInstance;
    private final Random random;
    private final LongHashMap destinationCoordinateCache = new LongHashMap();
    private final List destinationCoordinateKeys = new ArrayList();

    public AdminTeleporter(WorldServer world) {
        super(world);
        this.worldServerInstance = world;
        this.random = new Random(world.getSeed());
    }

    @Override
    public void placeInPortal(Entity entity, double x, double y, double z, float yaw) {
        if (!this.placeInExistingPortal(entity, x, y, z, yaw)) {
            this.makePortal(entity);
            this.placeInExistingPortal(entity, x, y, z, yaw);
        }
    }

    @Override
    public boolean placeInExistingPortal(Entity entity, double x, double y, double z, float yaw) {
        short radius = 128;
        double bestDist = -1.0D;
        int bestX = 0, bestY = 0, bestZ = 0;

        int ex = MathHelper.floor_double(entity.posX);
        int ez = MathHelper.floor_double(entity.posZ);
        long cacheKey = ChunkCoordIntPair.chunkXZ2Int(ex, ez);
        boolean needSearch = true;

        if (this.destinationCoordinateCache.containsItem(cacheKey)) {
            PortalPosition pos = (PortalPosition) this.destinationCoordinateCache.getValueByKey(cacheKey);
            bestDist = 0.0D;
            bestX = pos.posX;
            bestY = pos.posY;
            bestZ = pos.posZ;
            pos.lastUpdateTime = this.worldServerInstance.getTotalWorldTime();
            needSearch = false;
        } else {
            for (int bx = ex - radius; bx <= ex + radius; bx++) {
                double dx = (double) bx + 0.5D - entity.posX;
                for (int bz = ez - radius; bz <= ez + radius; bz++) {
                    double dz = (double) bz + 0.5D - entity.posZ;
                    for (int by = worldServerInstance.getActualHeight() - 1; by >= 0; by--) {
                        if (worldServerInstance.getBlock(bx, by, bz) == NetherLock.adminPortal) {
                            // Спускаемся до нижнего блока портала
                            while (worldServerInstance.getBlock(bx, by - 1, bz) == NetherLock.adminPortal) {
                                by--;
                            }
                            double dy = (double) by + 0.5D - entity.posY;
                            double dist = dx * dx + dy * dy + dz * dz;
                            if (bestDist < 0.0D || dist < bestDist) {
                                bestDist = dist;
                                bestX = bx;
                                bestY = by;
                                bestZ = bz;
                            }
                        }
                    }
                }
            }
        }

        if (bestDist >= 0.0D) {
            if (needSearch) {
                this.destinationCoordinateCache.add(cacheKey,
                        new PortalPosition(bestX, bestY, bestZ, this.worldServerInstance.getTotalWorldTime()));
                this.destinationCoordinateKeys.add(cacheKey);
            }

            // Определяем ориентацию портала и выставляем позицию игрока
            double px = (double) bestX + 0.5D;
            double py = (double) bestY;
            double pz = (double) bestZ + 0.5D;

            entity.motionX = entity.motionY = entity.motionZ = 0.0D;
            entity.setLocationAndAngles(px, py, pz, entity.rotationYaw, entity.rotationPitch);
            return true;
        }

        return false;
    }

    @Override
    public boolean makePortal(Entity entity) {
        int ex = MathHelper.floor_double(entity.posX);
        int ey = MathHelper.floor_double(entity.posY);
        int ez = MathHelper.floor_double(entity.posZ);

        int bestX = ex, bestY = -1, bestZ = ez;
        double bestDist = -1.0D;

        for (int dx = -16; dx <= 16; dx++) {
            for (int dz = -16; dz <= 16; dz++) {
                int bx = ex + dx, bz = ez + dz;
                for (int by = worldServerInstance.getActualHeight() - 2; by > 30; by--) {
                    if (!worldServerInstance.isAirBlock(bx, by, bz)) continue;

                    while (by > 0 && worldServerInstance.isAirBlock(bx, by - 1, bz)) by--;

                    // Проверяем что есть 4 блока воздуха над твёрдым основанием
                    boolean clear = true;
                    for (int j = 1; j <= 4; j++) {
                        if (!worldServerInstance.isAirBlock(bx, by + j, bz)) {
                            clear = false;
                            break;
                        }
                    }
                    if (!clear) break;

                    double dist = (double)(dx * dx) + (double)((by - ey) * (by - ey)) + (double)(dz * dz);
                    if (bestDist < 0.0D || dist < bestDist) {
                        bestDist = dist;
                        bestX = bx;
                        bestY = by + 1;
                        bestZ = bz;
                    }
                    break;
                }
            }
        }

        if (bestY == -1) bestY = MathHelper.clamp_int(ey, 70, worldServerInstance.getActualHeight() - 10);

        int x = bestX, y = bestY, z = bestZ;

        // Рамка из adminObsidian + заполнение adminPortal (2 wide x 3 tall)
        for (int i = -1; i <= 2; i++)
            worldServerInstance.setBlock(x + i, y - 1, z, NetherLock.adminObsidian, 0, 3);

        for (int j = 0; j <= 2; j++) {
            worldServerInstance.setBlock(x - 1, y + j, z, NetherLock.adminObsidian, 0, 3);
            worldServerInstance.setBlock(x + 2, y + j, z, NetherLock.adminObsidian, 0, 3);
        }

        for (int i = -1; i <= 2; i++)
            worldServerInstance.setBlock(x + i, y + 3, z, NetherLock.adminObsidian, 0, 3);

        for (int i = 0; i < 2; i++)
            for (int j = 0; j < 3; j++)
                worldServerInstance.setBlock(x + i, y + j, z, NetherLock.adminPortal, 0, 3);

        entity.setLocationAndAngles(x + 0.5, y, z + 0.5, entity.rotationYaw, entity.rotationPitch);
        entity.motionX = entity.motionY = entity.motionZ = 0.0D;
        return true;
    }

    @Override
    public void removeStalePortalLocations(long worldTime) {
        if (worldTime % 100L == 0L) {
            Iterator iter = this.destinationCoordinateKeys.iterator();
            long expireTime = worldTime - 600L;
            while (iter.hasNext()) {
                Long key = (Long) iter.next();
                PortalPosition pos = (PortalPosition) this.destinationCoordinateCache.getValueByKey(key);
                if (pos == null || pos.lastUpdateTime < expireTime) {
                    iter.remove();
                    this.destinationCoordinateCache.remove(key);
                }
            }
        }
    }

    private class PortalPosition extends ChunkCoordinates {
        public long lastUpdateTime;

        public PortalPosition(int x, int y, int z, long time) {
            super(x, y, z);
            this.lastUpdateTime = time;
        }
    }
}