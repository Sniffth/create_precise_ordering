package com.sniffth.create_precise_ordering.mixin;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.sniffth.create_precise_ordering.gui.OrderQuantityScreen;
import net.createmod.catnip.data.Couple;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = StockKeeperRequestScreen.class, remap = false)
public abstract class StockKeeperRequestScreenMixin {

    @Shadow
    protected abstract Couple<Integer> getHoveredSlot(int x, int y);

    @Shadow
    public List<List<BigItemStack>> displayedItems;

    @Shadow
    public List<BigItemStack> itemsToOrder;

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMiddleClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button == 2) {
            StockKeeperRequestScreen screen = (StockKeeperRequestScreen) (Object) this;
            Couple<Integer> hovered = getHoveredSlot((int) mouseX, (int) mouseY);

            if (hovered == null || (hovered.getFirst() == -1 && hovered.getSecond() == -1)) {
                return;
            }
            int categoryIndex = hovered.getFirst();
            int slotIndex = hovered.getSecond();
            BigItemStack hoveredBigStack = null;

            if (categoryIndex >= 0 && categoryIndex < displayedItems.size()) {
                List<BigItemStack> category = displayedItems.get(categoryIndex);
                if (slotIndex >= 0 && slotIndex < category.size()) {
                    hoveredBigStack = category.get(slotIndex);
                }
            }

            else if (categoryIndex == -1) {
                if (slotIndex >= 0 && slotIndex < itemsToOrder.size()) {
                    hoveredBigStack = itemsToOrder.get(slotIndex);
                }
            }

            if (hoveredBigStack != null && !hoveredBigStack.stack.isEmpty()) {
                Minecraft.getInstance().setScreen(
                        new OrderQuantityScreen(screen, hoveredBigStack)
                );
                cir.setReturnValue(true);
            }
        }
    }
}