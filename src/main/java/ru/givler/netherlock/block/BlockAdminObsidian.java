package ru.givler.netherlock.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.world.World;

public class BlockAdminObsidian extends Block {
    public BlockAdminObsidian() {
        super(Material.rock);
        setBlockName("admin_obsidian");
        setBlockTextureName("netherlock:admin_obsidian");
        setCreativeTab(CreativeTabs.tabBlock);
        setHardness(-1.0F);
        setResistance(6000000.0F);
    }

    @Override
    public void onBlockDestroyedByPlayer(World world, int x, int y, int z, int meta) {
        if (world.isRemote) return;

        AdminPortalSize size1 = new AdminPortalSize(world, x, y, z, 1);
        AdminPortalSize size2 = new AdminPortalSize(world, x, y, z, 2);

        AdminPortalSize size = size1.isValid() ? size1 : (size2.isValid() ? size2 : null);
        if (size == null) return;

        size.clearPortalBlocks();
    }
}
