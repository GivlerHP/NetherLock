package ru.givler.netherlock;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.common.MinecraftForge;
import ru.givler.netherlock.block.BlockAdminObsidian;
import ru.givler.netherlock.block.BlockAdminPortal;
import ru.givler.netherlock.block.BlockNullPortal;
import ru.givler.netherlock.config.NetherLockConfig;
import ru.givler.netherlock.handler.BlockHandler;
import ru.givler.netherlock.handler.PortalEventHandler;

@Mod(modid= NetherLock.ID, name= NetherLock.NAME, version= NetherLock.VERSION)
public class NetherLock {
	public static final String ID = "netherlock";
	public static final String NAME = "NetherLock";
	public static final String VERSION = "@VERSION@";
	
	@Instance(ID)
	public static NetherLock instance;

	public static Block adminObsidian;
	public static Block adminPortal;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		NetherLockConfig.load(event.getSuggestedConfigurationFile());

		adminObsidian = new BlockAdminObsidian();
		GameRegistry.registerBlock(adminObsidian, "admin_obsidian");

	    adminPortal = new BlockAdminPortal();
		GameRegistry.registerBlock(adminPortal, "admin_portal");

		BlockHandler.replaceBlock(Blocks.portal, BlockNullPortal.class, ItemBlock.class);
		MinecraftForge.EVENT_BUS.register(new PortalEventHandler());
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event){}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) { }

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
	}
}
