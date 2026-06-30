package ru.givler.netherlock.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPortal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import ru.givler.netherlock.config.NetherLockConfig;
import ru.givler.netherlock.teleport.AdminTeleporter;

import java.util.Random;

public class BlockAdminPortal extends BlockPortal {

    public BlockAdminPortal() {
        setBlockName("admin_portal");
        setBlockTextureName("netherlock:admin_portal");
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        if (world.isRemote) return;
        if (!NetherLockConfig.canTeleport) return;
        if (entity.timeUntilPortal > 0) return;
        if (!(entity instanceof EntityPlayerMP)) return;

        EntityPlayerMP player = (EntityPlayerMP) entity;
        player.timeUntilPortal = 10 * 20; // кулдаун 10 сек

        int targetDim = (world.provider.dimensionId == 0) ? -1 : 0;

        MinecraftServer server = MinecraftServer.getServer();
        WorldServer targetWorld = server.worldServerForDimension(targetDim);
        AdminTeleporter teleporter = new AdminTeleporter(targetWorld);

        server.getConfigurationManager()
                .transferPlayerToDimension(player, targetDim, teleporter);
    }

    @Override
    public void onBlockDestroyedByPlayer(World world, int x, int y, int z, int meta) {
        world.notifyBlocksOfNeighborChange(x, y, z, this);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        AdminPortalSize size1 = new AdminPortalSize(world, x, y, z, 1);
        AdminPortalSize size2 = new AdminPortalSize(world, x, y, z, 2);

        if (!size1.isValid() && !size2.isValid()) {
            world.setBlock(x, y, z, Blocks.air);
        }
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random rand) {}
}