package com.vsenna.stackable_barrels;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import com.vsenna.stackable_barrels.network.BarrelSizePayload;
import net.minecraft.text.Text;
import net.minecraft.inventory.Inventory;
import java.util.ArrayList;
import java.util.List;
public class StackableBarrelBlock extends BlockWithEntity implements InventoryProvider {
 
     // 1. Instanciamos o Codec usando a própria classe do bloco

    public static final MapCodec<StackableBarrelBlock> CODEC = createCodec(StackableBarrelBlock::new);

    public StackableBarrelBlock(Settings settings) {
        super(settings);
    }

    // 2. O método abstrato obrigatório que a IDE estava pedindo
    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new StackableBarrelBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
 
    @Override
    public SidedInventory getInventory(BlockState state, WorldAccess world, BlockPos pos) {
        // Top-to-Bottom scan para Funis (Hoppers)
        BlockPos topPos = pos;
        while (world.getBlockState(topPos.up()).isOf(this)) {
            topPos = topPos.up();
        }
 
        List<Inventory> network = new ArrayList<>();
        BlockPos currentPos = topPos;
        while (world.getBlockState(currentPos).isOf(this)) {
            BlockEntity be = world.getBlockEntity(currentPos);
            if (be instanceof StackableBarrelBlockEntity barrelBe) {
                network.add(barrelBe);
            }
            currentPos = currentPos.down();
        }
        return new CombinedInventory(network);
    }
 
    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {

        if (!world.isClient) {
 
            // 1. Escaneia para CIMA para encontrar o topo da torre
            BlockPos topPos = pos;
            while (world.getBlockState(topPos.up()).isOf(this)) {
                topPos = topPos.up();
            }
 
            // 2. Escaneia para BAIXO a partir do topo, coletando todos os inventários conectados
            List<Inventory> network = new ArrayList<>();
            BlockPos currentPos = topPos;
 
            while (world.getBlockState(currentPos).isOf(this)) {
                BlockEntity be = world.getBlockEntity(currentPos);
                if (be instanceof StackableBarrelBlockEntity barrelBe) {
                    network.add(barrelBe);
                }
                currentPos = currentPos.down(); // Desce um bloco
            }
 
            // 3. Cria a interface virtual combinada e abre para o jogador
            CombinedInventory combinedInventory = new CombinedInventory(network);


            player.openHandledScreen(new ExtendedScreenHandlerFactory<BarrelSizePayload>() {
                @Override
                public BarrelSizePayload getScreenOpeningData(ServerPlayerEntity player) {
                    return new BarrelSizePayload(combinedInventory.size());
                }

                @Override
                public Text getDisplayName() {
                    return Text.translatable("block.stackablebarrels.stackable_barrel");
                }

                @Nullable
                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                    return new StackableBarrelScreenHandler(syncId, playerInventory, combinedInventory);
                }
            });
        }
        return ActionResult.SUCCESS;
    }
}