package ru.givler.netherlock.handler;

import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.common.registry.GameData;
import java.lang.reflect.Field;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.RegistryNamespaced;
import net.minecraft.util.RegistrySimple;

public class BlockHandler {

    public static void replaceBlock(
            Block toReplace,
            Class<? extends Block> blockClass,
            Class<? extends ItemBlock> itemBlockClass) {

        Class<?>[] classTest = new Class[4];
        Exception exception = null;

        try {
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);

            for (Field blockField : Blocks.class.getDeclaredFields()) {
                if (!Block.class.isAssignableFrom(blockField.getType())) continue;

                Block block = (Block) blockField.get(null);
                if (block != toReplace) continue;

                String registryName = Block.blockRegistry.getNameForObject(block);

                int id = Block.getIdFromBlock(block);
                ItemBlock item = (ItemBlock) Item.getItemFromBlock(block);
                Block newBlock = blockClass.newInstance();

                FMLControlledNamespacedRegistry<Block> blockRegistry = GameData.getBlockRegistry();

                Field nameToObjectMap = RegistrySimple.class.getDeclaredFields()[1];
                nameToObjectMap.setAccessible(true);
                ((Map<String, Block>) nameToObjectMap.get(blockRegistry)).put(registryName, newBlock);

                Field idToObjectMap = RegistryNamespaced.class.getDeclaredFields()[0];
                idToObjectMap.setAccessible(true);
                // func_148746_a(obj, id) — put(obj, id)
                ((ObjectIntIdentityMap) idToObjectMap.get(blockRegistry)).func_148746_a(newBlock, id);
                blockField.setAccessible(true);
                modifiersField.setInt(blockField, blockField.getModifiers() & ~java.lang.reflect.Modifier.FINAL);
                blockField.set(null, newBlock);
                ItemBlock newItemBlock = itemBlockClass
                        .getConstructor(Block.class)
                        .newInstance(newBlock);

                FMLControlledNamespacedRegistry<Item> itemRegistry = GameData.getItemRegistry();
                ((Map<String, ItemBlock>) nameToObjectMap.get(itemRegistry)).put(registryName, newItemBlock);
                ((ObjectIntIdentityMap) idToObjectMap.get(itemRegistry)).func_148746_a(newItemBlock, id);
                classTest[0] = blockField.get(null).getClass();
                classTest[1] = Block.blockRegistry.getObjectById(id).getClass();
                classTest[2] = ((ItemBlock) Item.getItemFromBlock(newBlock)).field_150939_a.getClass();
                classTest[3] = Item.getItemFromBlock(newBlock).getClass();
            }

        } catch (Exception e) {
            exception = e;
        }

        if (classTest[0] != classTest[1]
                || classTest[0] != classTest[2]
                || classTest[0] == null
                || classTest[3] != itemBlockClass) {
            throw new RuntimeException(
                    "unable to replace block " + toReplace.getUnlocalizedName()
                            + "! Debug info to report: "
                            + classTest[0] + "," + classTest[1] + ","
                            + classTest[2] + "," + classTest[3],
                    exception);
        }
    }
}