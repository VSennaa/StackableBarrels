package com.vsenna.stackable_barrels;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

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

    public static final Block STACKABLE_BARREL = new Block(AbstractBlock.Settings.create().strength(2.0f));

    // Aba
    public static final ItemGroup MOD_TAB = Registry.register(Registries.ITEM_GROUP,
            new Identifier(MOD_ID, "aba_barris"),
            FabricItemGroup.builder()
                    // Puxa o nome lá do arquivo de tradução (lang)
                    .displayName(Text.translatable("itemGroup.stackablebarrels.aba_barris"))
                    // Define qual item será o ícone da aba
                    .icon(() -> new ItemStack(STACKABLE_BARREL))
                    // Adiciona os itens dentro da aba
                    .entries((context, entries) -> {
                        entries.add(STACKABLE_BARREL);
                    })
                    .build());

    @Override
	public void onInitialize() {
        LOGGER.info("Inicializando o mod Stackable Barrels!");
        Identifier idDoBloco = new Identifier(MOD_ID, "stackable_barrel");
        Registry.register(Registries.BLOCK, idDoBloco, STACKABLE_BARREL);

        // 3. Registrando o Item do Bloco (para ele poder ir para o inventário do jogador)
        Registry.register(Registries.ITEM, idDoBloco, new BlockItem(STACKABLE_BARREL, new Item.Settings()));
    }
}