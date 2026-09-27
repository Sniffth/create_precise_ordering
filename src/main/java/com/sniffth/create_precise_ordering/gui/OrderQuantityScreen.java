package com.sniffth.create_precise_ordering.gui;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class OrderQuantityScreen extends AbstractSimiScreen {

    private final StockKeeperRequestScreen parentScreen;
    private final BigItemStack targetEntry;
    private EditBox amountInput;
    private IconButton confirmButton;
    private IconButton cancelButton;

    private final int windowWidth = AllGuiTextures.STOCK_KEEPER_REQUEST_HEADER.getWidth();

    public OrderQuantityScreen(StockKeeperRequestScreen parentScreen, BigItemStack targetEntry) {
        super(Component.literal("Select Amount"));
        this.parentScreen = parentScreen;
        this.targetEntry = targetEntry;
    }

    @Override
    protected void init() {
        super.init();
        int totalHeight = 96;
        this.guiLeft = (this.width - this.windowWidth) / 2;
        this.guiTop = (this.height - totalHeight) / 2;
        int buttonWidth = 36;
        int buttonHeight = 18;
        int startX = guiLeft + 40;

        this.addRenderableWidget(Button.builder(Component.literal("+1"), b -> modifyAmount(1))
                .bounds(startX, guiTop + 23, buttonWidth, buttonHeight).build());
        this.addRenderableWidget(Button.builder(Component.literal("+10"), b -> modifyAmount(10))
                .bounds(startX + 38, guiTop + 23, buttonWidth, buttonHeight).build());
        this.addRenderableWidget(Button.builder(Component.literal("+100"), b -> modifyAmount(100))
                .bounds(startX + 76, guiTop + 23, buttonWidth, buttonHeight).build());
        this.addRenderableWidget(Button.builder(Component.literal("+1000"), b -> modifyAmount(1000))
                .bounds(startX + 114, guiTop + 23, buttonWidth, buttonHeight).build());

        this.amountInput = new EditBox(this.font, guiLeft + 78, guiTop + 53, 50, 10,
                Component.literal("Quantity"));
        this.amountInput.setMaxLength(7);
        this.amountInput.setValue(String.valueOf(Math.max(1, targetEntry.count)));
        this.amountInput.setFilter(s -> s.matches("\\d*"));
        this.amountInput.setBordered(false);
        this.amountInput.setTextColor(0xFFFFFFFF);
        this.addRenderableWidget(this.amountInput);

        this.confirmButton = new IconButton(guiLeft + 195, guiTop + 23, AllIcons.I_CONFIRM);
        this.confirmButton.withCallback(this::confirmOrder);
        this.confirmButton.setToolTip(Component.literal("Confirm"));
        this.addRenderableWidget(this.confirmButton);

        this.cancelButton = new IconButton(guiLeft + 195, guiTop + 72, AllIcons.I_DISABLE);
        this.cancelButton.withCallback(this::onClose);
        this.cancelButton.setToolTip(Component.literal("Cancel"));
        this.addRenderableWidget(this.cancelButton);

        this.addRenderableWidget(Button.builder(Component.literal("-1"), b -> modifyAmount(-1))
                .bounds(startX, guiTop + 72, buttonWidth, buttonHeight).build());
        this.addRenderableWidget(Button.builder(Component.literal("-10"), b -> modifyAmount(-10))
                .bounds(startX + 38, guiTop + 72, buttonWidth, buttonHeight).build());
        this.addRenderableWidget(Button.builder(Component.literal("-100"), b -> modifyAmount(-100))
                .bounds(startX + 76, guiTop + 72, buttonWidth, buttonHeight).build());
        this.addRenderableWidget(Button.builder(Component.literal("-1000"), b -> modifyAmount(-1000))
                .bounds(startX + 114, guiTop + 72, buttonWidth, buttonHeight).build());

        this.setFocused(this.amountInput);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        partialTicks = AnimationTickHolder.getPartialTicksUI();
        graphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        renderBg(graphics, partialTicks, mouseX, mouseY);
        for (Renderable widget : this.renderables) {widget.render(graphics, mouseX, mouseY, partialTicks);}
        renderForeground(graphics, mouseX, mouseY, partialTicks);
    }

    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int y = guiTop;
        AllGuiTextures.STOCK_KEEPER_CATEGORY_HEADER.render(graphics, guiLeft + 30, y);
        y += 18;
        AllGuiTextures.STOCK_KEEPER_CATEGORY.render(graphics, guiLeft + 30, y);
        y += 20;
        AllGuiTextures.STOCK_KEEPER_CATEGORY_EDIT.render(graphics, guiLeft + 30, y);
        y += 38;
        AllGuiTextures.STOCK_KEEPER_CATEGORY.render(graphics, guiLeft + 30, y);

        int center = guiLeft + (windowWidth / 2);
        String titleText = targetEntry.stack.getHoverName().getString();
        if (titleText.length() > 22) {
            titleText = titleText.substring(0, 20) + "...";
        }
        graphics.drawString(font, titleText, center - font.width(titleText) / 2, guiTop + 4, 0x3D3C48, false);
    }

    protected void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.renderItem(targetEntry.stack, guiLeft + 46, guiTop + 49);
    }

    private void modifyAmount(int delta) {
        try {
            int current = amountInput.getValue().isEmpty() ? 0 : Integer.parseInt(amountInput.getValue());
            int newAmount = Math.max(1, current + delta);
            amountInput.setValue(String.valueOf(newAmount));
        } catch (NumberFormatException e) {
            amountInput.setValue("1");
        }
    }

    private void confirmOrder() {
        try {
            int requestedAmount = Integer.parseInt(amountInput.getValue());
            if (requestedAmount > 0) {
                BigItemStack existing = null;
                for (BigItemStack order : parentScreen.itemsToOrder) {
                    if (ItemStack.isSameItemSameComponents(order.stack, targetEntry.stack)) {
                        existing = order;
                        break;
                    }
                }

                if (existing != null) {
                    existing.count = requestedAmount;
                } else {
                    if (parentScreen.itemsToOrder.size() < 9) {
                        parentScreen.itemsToOrder.add(new BigItemStack(targetEntry.stack.copyWithCount(1), requestedAmount));
                    }
                }
            }
        } catch (NumberFormatException ignored) {}

        if (this.minecraft != null) {
            this.minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1.0f);
            this.minecraft.setScreen(parentScreen);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 || keyCode == 335) {
            confirmOrder();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderWindow(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parentScreen);
        }
    }
}