package ru.givler.netherlock.handler;

import net.minecraft.block.material.Material;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ru.givler.netherlock.NetherLock;
import ru.givler.netherlock.block.AdminPortalSize;
import ru.givler.netherlock.config.NetherLockConfig;

public class PortalEventHandler {

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;
        if (!NetherLockConfig.canCreatePortal) return;
        if (event.entityPlayer.getHeldItem() == null) return;

        if (event.entityPlayer.worldObj.isRemote) return;

        net.minecraft.item.Item held = event.entityPlayer.getHeldItem().getItem();
        if (held != net.minecraft.init.Items.flint_and_steel && held != net.minecraft.init.Items.fire_charge) return;

        World world = event.entityPlayer.worldObj;
        int x = event.x, y = event.y, z = event.z;

        if (world.getBlock(x, y, z) != NetherLock.adminObsidian) return;

        int[][] neighbors = {{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};
        for (int[] d : neighbors) {
            int nx = x + d[0], ny = y + d[1], nz = z + d[2];
            if (world.getBlock(nx, ny, nz).getMaterial() != Material.air) continue;

            AdminPortalSize size1 = new AdminPortalSize(world, nx, ny, nz, 1);
            AdminPortalSize size2 = new AdminPortalSize(world, nx, ny, nz, 2);

            if (size1.isValid() && size1.getPortalCount() == 0) {
                size1.fillWithAdminPortal();
                event.setCanceled(true);
                return;
            } else if (size2.isValid() && size2.getPortalCount() == 0) {
                size2.fillWithAdminPortal();
                event.setCanceled(true);
                return;
            }
        }
    }
}