package com.vsenna.stackable_barrels;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import com.vsenna.stackable_barrels.network.BarrelSizePayload;

public class StackableBarrelScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final int barrelSize; // Guarda o tamanho dinâmico (ex: 27, 54, 81...)

    public StackableBarrelScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(27));
    }

    public StackableBarrelScreenHandler(int syncId, PlayerInventory playerInventory, BarrelSizePayload payload) {
        this(syncId, playerInventory, new SimpleInventory(payload.size()));
    }

    public StackableBarrelScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(StackableBarrels.STACKABLE_BARREL_SCREEN_HANDLER, syncId);
        this.inventory = inventory;
        this.barrelSize = inventory.size();
        inventory.onOpen(playerInventory.player);

        // 1. Cria TODOS os slots do silo (mas os joga para o Limbo em Y=-2000)
        for (int i = 0; i < this.barrelSize; ++i) {
            this.addSlot(new Slot(inventory, i, -2000, -2000));
        }

        // 2. Inventário do Jogador
        for (int m = 0; m < 3; ++m) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 140 + m * 18));
            }
        }

        // 3. Hotbar do Jogador
        for (int l = 0; l < 9; ++l) {
            this.addSlot(new Slot(playerInventory, l, 8 + l * 18, 198));
        }
    }

    public int getBarrelSize() {
        return this.barrelSize;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            // A lógica de Shift-Click agora entende o tamanho exato da torre
            if (invSlot < this.barrelSize) {
                // Movendo do Barril para o Jogador
                if (!this.insertItem(originalStack, this.barrelSize, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Movendo do Jogador para o Barril (Ele tenta preencher todos os espaços escondidos sozinho!)
                if (!this.insertItem(originalStack, 0, this.barrelSize, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }
}