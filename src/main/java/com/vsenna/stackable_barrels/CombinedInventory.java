package com.vsenna.stackable_barrels;
 
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
 
import java.util.List;
 
public class CombinedInventory implements SidedInventory {
    private final List<Inventory> inventories;
 
    public CombinedInventory(List<Inventory> inventories) {
        this.inventories = inventories;
    }
 
    @Override
    public int size() {
        return inventories.size() * 27;
    }
 
    @Override
    public boolean isEmpty() {
        for (Inventory inv : inventories) {
            if (!inv.isEmpty()) return false;
        }
        return true;
    }
 
    // A mágica matemática: converte um slot global (ex: 30) para o slot local do barril correto (barril 1, slot 3)
    @Override
    public ItemStack getStack(int slot) {
        return inventories.get(slot / 27).getStack(slot % 27);
    }
 
    @Override
    public ItemStack removeStack(int slot, int amount) {
        return inventories.get(slot / 27).removeStack(slot % 27, amount);
    }
 
    @Override
    public ItemStack removeStack(int slot) {
        return inventories.get(slot / 27).removeStack(slot % 27);
    }
 
    @Override
    public void setStack(int slot, ItemStack stack) {
        inventories.get(slot / 27).setStack(slot % 27, stack);
    }
 
    @Override
    public void markDirty() {
        for (Inventory inv : inventories) {
            inv.markDirty();
        }
    }
 
    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }
 
    @Override
    public void clear() {
        for (Inventory inv : inventories) {
            inv.clear();
        }
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        int[] slots = new int[size()];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = i;
        }
        return slots;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }
}
