package com.vsenna.stackable_barrels;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StackableBarrelScreen extends HandledScreen<StackableBarrelScreenHandler> {

    private static final Identifier TEXTURE = new Identifier("minecraft", "textures/gui/container/generic_54.png");

    private float scrollPosition = 0.0f;
    private boolean isScrolling = false;
    private static final int VISIBLE_ROWS = 6;
    private TextFieldWidget searchField;
    private List<Slot> filteredSlots = new ArrayList<>();

    public StackableBarrelScreen(StackableBarrelScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 114 + VISIBLE_ROWS * 18;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        this.searchField = new TextFieldWidget(this.textRenderer, x, y - 18, 100, 12, Text.literal(""));
        this.searchField.setMaxLength(32);
        this.searchField.setChangedListener(s -> updateSlots());
        this.addDrawableChild(this.searchField);

        updateSlots();
    }

    private boolean isItemMatching(ItemStack stack, String query) {
        if (query.isEmpty()) return true;
        String lowerQuery = query.toLowerCase(Locale.ROOT);

        if (lowerQuery.startsWith("@")) {
            String modId = lowerQuery.substring(1);
            return Registries.ITEM.getId(stack.getItem()).getNamespace().contains(modId);
        } else if (lowerQuery.startsWith("#")) {
            String tagId = lowerQuery.substring(1);
            return stack.streamTags().anyMatch(tag -> tag.id().toString().contains(tagId));
        } else {
            return stack.getName().getString().toLowerCase(Locale.ROOT).contains(lowerQuery);
        }
    }

    private void updateSlots() {
        String query = this.searchField != null ? this.searchField.getText() : "";
        filteredSlots.clear();

        // 1. Separa quem passa no filtro
        for (int i = 0; i < this.handler.getBarrelSize(); i++) {
            Slot slot = this.handler.slots.get(i);
            if (query.isEmpty() || (!slot.getStack().isEmpty() && isItemMatching(slot.getStack(), query))) {
                filteredSlots.add(slot);
            }
        }

        // 2. Calcula a barra de rolagem baseada SÓ nos itens filtrados
        int totalFilteredRows = (int) Math.ceil(filteredSlots.size() / 9.0);
        int hiddenRows = Math.max(0, totalFilteredRows - VISIBLE_ROWS);
        int startRow = MathHelper.clamp((int) (this.scrollPosition * hiddenRows + 0.5f), 0, hiddenRows);

        // 3. Aplica as posições usando a técnica de "Clonar o Slot"
        for (int i = 0; i < this.handler.getBarrelSize(); i++) {
            Slot oldSlot = this.handler.slots.get(i);
            int index = filteredSlots.indexOf(oldSlot);

            int newX, newY;

            if (index != -1) {
                // Passou no filtro: calcula a nova posição na grade virtual
                int row = index / 9;
                int col = index % 9;

                if (row >= startRow && row < startRow + VISIBLE_ROWS) {
                    newX = 8 + col * 18;
                    newY = 18 + (row - startRow) * 18;
                } else {
                    newX = -2000;
                    newY = -2000;
                }
            } else {
                // Não passou no filtro: vai pro limbo
                newX = -2000;
                newY = -2000;
            }

            // Se a posição mudou, a gente clona e substitui burlando o 'final'
            if (oldSlot.x != newX || oldSlot.y != newY) {
                Slot newSlot = new Slot(oldSlot.inventory, oldSlot.getIndex(), newX, newY);
                newSlot.id = oldSlot.id;
                this.handler.slots.set(i, newSlot);
            }
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        int totalFilteredRows = (int) Math.ceil(filteredSlots.size() / 9.0);
        if (totalFilteredRows > VISIBLE_ROWS) {
            int scrollbarX = x + 170;
            int scrollbarY = y + 18;
            int scrollbarHeight = VISIBLE_ROWS * 18;
            context.fill(scrollbarX, scrollbarY, scrollbarX + 4, scrollbarY + scrollbarHeight, 0xFF333333);
            int thumbY = scrollbarY + (int) (this.scrollPosition * (scrollbarHeight - 15));
            context.fill(scrollbarX, thumbY, scrollbarX + 4, thumbY + 15, 0xFFC6C6C6);
        }
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        super.drawForeground(context, mouseX, mouseY);

        long occupied = this.handler.slots.stream().limit(this.handler.getBarrelSize()).filter(Slot::hasStack).count();
        String text = occupied + " / " + this.handler.getBarrelSize();
        context.drawText(this.textRenderer, text, this.backgroundWidth - this.textRenderer.getWidth(text), -11, 0xFFFFFF, true);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.searchField.isActive() && this.searchField.isFocused()) {
            this.searchField.keyPressed(keyCode, scanCode, modifiers);
            if (this.client.options.inventoryKey.matchesKey(keyCode, scanCode)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // O modelo tinha apagado esta linha e o @Override aqui embaixo!
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int totalFilteredRows = (int) Math.ceil(filteredSlots.size() / 9.0);
        int hiddenRows = Math.max(0, totalFilteredRows - VISIBLE_ROWS);
        if (hiddenRows > 0) {
            float step = 1.0f / hiddenRows;
            this.scrollPosition = MathHelper.clamp(this.scrollPosition - (float) verticalAmount * step, 0.0f, 1.0f);
            updateSlots();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int totalFilteredRows = (int) Math.ceil(filteredSlots.size() / 9.0);
        if (button == 0 && totalFilteredRows > VISIBLE_ROWS) {
            int x = (this.width - this.backgroundWidth) / 2;
            int y = (this.height - this.backgroundHeight) / 2;
            int scrollbarX = x + 170;
            int scrollbarY = y + 18;

            if (mouseX >= scrollbarX && mouseX <= scrollbarX + 4 && mouseY >= scrollbarY && mouseY <= scrollbarY + (VISIBLE_ROWS * 18)) {
                this.isScrolling = true;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int totalFilteredRows = (int) Math.ceil(filteredSlots.size() / 9.0);
        if (this.isScrolling && totalFilteredRows > VISIBLE_ROWS) {
            int y = (this.height - this.backgroundHeight) / 2;
            int scrollbarY = y + 18;
            this.scrollPosition = ((float) mouseY - scrollbarY - 7.5f) / ((VISIBLE_ROWS * 18) - 15.0f);
            this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0f, 1.0f);
            updateSlots();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
}
