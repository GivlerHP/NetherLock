package ru.givler.netherlock.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPortal;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import ru.givler.netherlock.NetherLock;

public class BlockNullPortal extends BlockPortal {

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        // телепортация заблокирована
    }

    @Override
    public boolean func_150000_e(World world, int x, int y, int z) {
        return false;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        // Не трогаем adminPortal — он управляет собой сам
        if (world.getBlock(x, y, z) == NetherLock.adminPortal) return;
        super.onNeighborBlockChange(world, x, y, z, block);
    }
}