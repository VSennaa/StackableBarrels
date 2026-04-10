package com.vsenna.stackable_barrels;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
 
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
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import com.vsenna.stackable_barrels.network.BarrelSizePayload;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.screen.ScreenHandlerType;

public class StackableBarrels implements ModInitializer {
	public static final String MOD_ID = "stackablebarrels";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Block STACKABLE_BARREL = new StackableBarrelBlock(AbstractBlock.Settings.create().strength(2.0f));
    public static final BlockEntityType<StackableBarrelBlockEntity> STACKABLE_BARREL_BLOCK_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            new Identifier(MOD_ID, "stackable_barrel"),
            BlockEntityType.Builder.create(StackableBarrelBlockEntity::new, STACKABLE_BARREL).build()
    );

    // Registro da Interface
    public static final ExtendedScreenHandlerType<StackableBarrelScreenHandler, BarrelSizePayload> STACKABLE_BARREL_SCREEN_HANDLER = Registry.register(
            Registries.SCREEN_HANDLER,
            new Identifier(MOD_ID, "stackable_barrel"),
            new ExtendedScreenHandlerType<>(StackableBarrelScreenHandler::new, BarrelSizePayload.CODEC)
    );
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

        // Integração com Fabric Transfer API
        ItemStorage.SIDED.registerForBlockEntities((blockEntity, context) -> {
            if (blockEntity instanceof StackableBarrelBlockEntity barrelBe) {
                net.minecraft.world.World world = barrelBe.getWorld();
                net.minecraft.util.math.BlockPos pos = barrelBe.getPos();

                // Evita crashes caso o mundo ainda esteja carregando
                if (world == null) return null;

                // Scan Top-to-Bottom (Procura o topo)
                net.minecraft.util.math.BlockPos topPos = pos;
                while (world.getBlockState(topPos.up()).isOf(STACKABLE_BARREL)) {
                    topPos = topPos.up();
                }

                // Desce coletando a rede
                java.util.List<net.minecraft.inventory.Inventory> network = new java.util.ArrayList<>();
                net.minecraft.util.math.BlockPos currentPos = topPos;

                while (world.getBlockState(currentPos).isOf(STACKABLE_BARREL)) {
                    net.minecraft.block.entity.BlockEntity be = world.getBlockEntity(currentPos);
                    if (be instanceof StackableBarrelBlockEntity b) {
                        network.add(b);
                    }
                    currentPos = currentPos.down();
                }

                // Retorna o inventário combinado. Passamos o 'context' (Direction) para o funil saber o lado!
                return InventoryStorage.of(new CombinedInventory(network), context);
            }

            // O Fabric exige 'null' quando não achar o armazenamento, em vez de Optional
            return null;
        }, STACKABLE_BARREL_BLOCK_ENTITY);
    }

}