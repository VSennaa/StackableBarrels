package com.vsenna.stackable_barrels;

import net.fabricmc.api.ModInitializer;

import net.minecraft.block.AbstractBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.block.Block;

public class StackableBarrels implements ModInitializer {
	public static final String MOD_ID = "stackablebarrels";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


    public static final Block BARRIL_TESTE = new Block(AbstractBlock.Settings.create().strength(2.0f));


    @Override
	public void onInitialize() {
        LOGGER.info("Inicializando o mod Stackable Barrels!");
        Identifier idDoBloco = new Identifier(MOD_ID, "barril_teste");
        Registry.register(Registries.BLOCK, idDoBloco, BARRIL_TESTE);

        // 3. Registrando o Item do Bloco (para ele poder ir para o inventário do jogador)
        Registry.register(Registries.ITEM, idDoBloco, new BlockItem(BARRIL_TESTE, new Item.Settings()));
    }
}