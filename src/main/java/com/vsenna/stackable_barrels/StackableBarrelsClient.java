package com.vsenna.stackable_barrels;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;


public class StackableBarrelsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            return 0xFF5555;
        }, StackableBarrels.STACKABLE_BARREL);

        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            return 0xFF5555;
        }, StackableBarrels.STACKABLE_BARREL);
    }
}
