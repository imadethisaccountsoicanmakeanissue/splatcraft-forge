package net.splatcraft.forge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.splatcraft.forge.client.handlers.RendererHandler;
import net.splatcraft.forge.items.weapons.SubWeaponItem;

public class SplatcraftItemRenderer extends BlockEntityWithoutLevelRenderer
{
	public static final SplatcraftItemRenderer INSTANCE = new SplatcraftItemRenderer();

	public SplatcraftItemRenderer()
	{
		super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
	}

    @Override
    public void renderByItem(ItemStack pStack, ItemDisplayContext pDisplayContext, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        if (!RendererHandler.renderSubWeapon(pStack, pDisplayContext, pPoseStack, pBuffer, pPackedLight, pPackedOverlay)) {
            super.renderByItem(pStack, pDisplayContext, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        }
    }
}