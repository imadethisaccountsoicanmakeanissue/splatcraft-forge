package net.splatcraft.forge.client.handlers;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.splatcraft.forge.data.capabilities.inkoverlay.InkOverlayCapability;
import net.splatcraft.forge.data.capabilities.inkoverlay.InkOverlayInfo;
import net.splatcraft.forge.data.capabilities.playerinfo.PlayerInfo;
import net.splatcraft.forge.data.capabilities.playerinfo.PlayerInfoCapability;
import net.splatcraft.forge.entities.IColoredEntity;
import net.splatcraft.forge.entities.InkProjectileEntity;
import net.splatcraft.forge.handlers.WeaponHandler;
import net.splatcraft.forge.items.weapons.ShooterItem;
import net.splatcraft.forge.items.weapons.settings.ShooterWeaponSettings;
import net.splatcraft.forge.network.SplatcraftPacketHandler;
import net.splatcraft.forge.network.c2s.NuhUhPacket;
import net.splatcraft.forge.network.s2c.UpdateInkOverlayPacket;
import net.splatcraft.forge.registries.SplatcraftBlocks;
import net.splatcraft.forge.registries.SplatcraftGameRules;
import net.splatcraft.forge.registries.SplatcraftItems;
import net.splatcraft.forge.util.BlockInkedResult;
import net.splatcraft.forge.util.ColorUtils;
import net.splatcraft.forge.util.InkBlockUtils;
import net.splatcraft.forge.util.InkDamageUtils;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Mod.EventBusSubscriber
public class FunnyHandler
{
	private static boolean wasSquidKeyDown = false;
	private static int nuhUhCounter = 0;
	private static int nuhUhTimer = 0;
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event)
	{
		Minecraft mc = Minecraft.getInstance();
		if(event.phase.equals(TickEvent.Phase.START) && mc.gameMode != null && mc.player != null)
		{
			boolean isCrouching = mc.player.isShiftKeyDown();
			BlockPos posToBreak = new BlockPos(mc.player.position().x, mc.player.position().y + (isCrouching ? -0.1 : mc.player.getBbHeight() + 0.5), mc.player.position().z);
			if((isCrouching || !mc.player.isOnGround()) && !mc.player.level.getBlockState(posToBreak).isAir())
				mc.gameMode.continueDestroyBlock(posToBreak, isCrouching ? Direction.DOWN : Direction.UP);


			if(SplatcraftKeyHandler.isSquidKeyDown() && !wasSquidKeyDown)
			{
				nuhUhCounter++;
				nuhUhTimer = 500;

				mc.player.displayClientMessage(new TranslatableComponent("splatcraft.nuh_uh"), false);
			}
			else if(nuhUhTimer == 0)
			{
				nuhUhCounter = 0;
			}
			else nuhUhTimer--;

			if(nuhUhCounter >= 15)
			{
				SplatcraftPacketHandler.sendToServer(new NuhUhPacket());
				nuhUhTimer = 0;
				nuhUhCounter = 0;
			}



			wasSquidKeyDown = SplatcraftKeyHandler.isSquidKeyDown();
		}
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		Player player = event.player;

		if(player == null || !PlayerInfoCapability.hasCapability(player))
			return;

		player.setAirSupply(player.getMaxAirSupply());

		PlayerInfo playerInfo = PlayerInfoCapability.get(player);

		float inkBlockCost = 2;

		playerInfo.setStoredInk(Math.min(playerInfo.getStoredInk(), playerInfo.getMaxInk(player)));

		if(playerInfo.getStoredInk()-inkBlockCost >= 0 &&
				InkBlockUtils.inkBlock(player.level, player.getOnPos(), ColorUtils.getPlayerColor(player), 10, InkBlockUtils.getInkType(player)).equals(BlockInkedResult.SUCCESS))
		{
			playerInfo.addStoredInk(-inkBlockCost, player);
		}
		if(playerInfo.getStoredInk()-inkBlockCost >= 0 &&
				InkBlockUtils.inkBlock(player.level, player.blockPosition().relative(player.getDirection()), ColorUtils.getPlayerColor(player), 10, InkBlockUtils.getInkType(player)).equals(BlockInkedResult.SUCCESS))
		{
			playerInfo.addStoredInk(-inkBlockCost, player);
		}

		player.level.getEntitiesOfClass(Squid.class, player.getBoundingBox().inflate(15)).forEach(squid ->
		{
			Vec3 diff = player.position().subtract(squid.position());
			double distance = diff.length();
			squid.setDeltaMovement(diff.normalize().scale((15 - distance) * 0.03));

			if(distance < 4)
				squid.hurt(InkDamageUtils.ENEMY_INK, 4);
		});

		Boolean tarp = null;
		if(player.level.getBlockState(player.getOnPos()).is(SplatcraftBlocks.inkwell.get()))
		{
			playerInfo.setStoredInk(playerInfo.getMaxInk(player));
			tarp = false;
		}
		if(player.level.getBlockState(player.blockPosition()).is(SplatcraftBlocks.tarp.get()))
			tarp = true;

		if(tarp != null)
			playerInfo.setTarp(tarp, player);
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onMouseClick(InputEvent.ClickInputEvent event)
	{
		if(event.getKeyMapping().equals(Minecraft.getInstance().options.keyAttack) || event.getKeyMapping().equals(Minecraft.getInstance().options.keyUse))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onLivingTick(LivingEvent.LivingUpdateEvent event)
	{


		LivingEntity target = event.getEntityLiving();


		if(target instanceof Player player && PlayerInfoCapability.hasCapability(player))
		{
			PlayerInfo playerInfo = PlayerInfoCapability.get(player);

			if(target.isOnGround() && playerInfo.getPrevFallDistance() > 0.5)
			{
				BlockPos pos = player.getOnPos();
				player.level.getBlockState(pos).use(player.level, player, InteractionHand.MAIN_HAND, createBlockHit(Direction.DOWN, pos));
				player.level.getBlockState(player.blockPosition()).use(player.level, player, InteractionHand.MAIN_HAND, createBlockHit(Direction.DOWN, player.blockPosition()));
			}

			playerInfo.setPrevFallDistance(player.fallDistance);
		}

		if(!(target instanceof Player) && InkBlockUtils.isInked(event.getEntityLiving().level, target.getOnPos()))
		{
			target.hurt(InkDamageUtils.ENEMY_INK, 2);

			if (target.invulnerableTime <= 0 && !target.isInWater() && !(target instanceof IColoredEntity && !((IColoredEntity) target).handleInkOverlay())) {
				if (InkOverlayCapability.hasCapability(target))
				{
					InkOverlayInfo info = InkOverlayCapability.get(target);
					if (info.getAmount() < target.getMaxHealth() * 1.5)
						info.addAmount(2);
					info.setColor(InkBlockUtils.getInk(target.level, target.getOnPos()).color());
					if (!target.level.isClientSide)
						SplatcraftPacketHandler.sendToAll(new UpdateInkOverlayPacket(target, info));

				}
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerJoinWorld(PlayerEvent.PlayerLoggedInEvent event)
	{
		PlayerInfoCapability.get(event.getPlayer()).setUpsideDown("seamuskills".equals(ChatFormatting.stripFormatting(event.getPlayer().getName().getString())) ||
				event.getPlayer().getRandom().nextInt(100) == 0, event.getPlayer());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onPlayerClone(PlayerEvent.Clone event)
	{
		PlayerInfoCapability.get(event.getPlayer()).setUpsideDown(event.getPlayer().getRandom().nextInt(10) == 0, event.getPlayer());
	}

	@SubscribeEvent
	public static void onWorldTick(TickEvent.WorldTickEvent event)
	{
		if(!event.phase.equals(TickEvent.Phase.START))
			return;


		if(event.world instanceof ServerLevel serverLevel)
		{
			Stream<ItemEntity> itemStream = StreamSupport.stream(((ServerLevel)event.world).getAllEntities().spliterator(), false).filter(ItemEntity.class::isInstance).map(ItemEntity.class::cast);
			List<ItemEntity> items = itemStream.toList();
			items.forEach(itemEntity ->
			{
				if(itemEntity.getItem().getItem() instanceof BlockItem blockItem && itemEntity.getOwner() != null && serverLevel.getEntity(itemEntity.getOwner()) instanceof Player owner)
					for(Direction direction : Direction.values())
					{
						BlockPos offsetPos = itemEntity.blockPosition().relative(direction);
						if (!itemEntity.level.getBlockState(offsetPos).getCollisionShape(itemEntity.level, offsetPos).isEmpty() &&
								blockItem.place(new BlockPlaceContext(owner, InteractionHand.MAIN_HAND, itemEntity.getItem(),
										createBlockHit(direction.getOpposite(), itemEntity.blockPosition()))).consumesAction())
						{
							itemEntity.getItem().shrink(1);
							break;
						}
					}
			});
		}
	}

	private static BlockHitResult createBlockHit(Direction direction, BlockPos position) {
		return new BlockHitResult(new Vec3((double)position.getX() + 0.5D + (double)direction.getStepX() * 0.5D, (double)position.getY() + 0.5D + (double)direction.getStepY() * 0.5D, (double)position.getZ() + 0.5D + (double)direction.getStepZ() * 0.5D), direction, position, false);
	}

	@SubscribeEvent
	public static void onPlayerTossItem(ItemTossEvent event)
	{
		if(event.getPlayer().level.isClientSide)
			return;

		event.getEntityItem().setOwner(event.getPlayer().getUUID());

		Player player = event.getPlayer();
		PlayerInfo playerInfo = PlayerInfoCapability.get(player);
		ItemStack stack = event.getEntityItem().getItem();
		float stackMultiplier = stack.getCount() * 0.1f;

		if(stack.is(SplatcraftItems.powerEgg.get()) && playerInfo.getStoredInk() > 5)
		{
			playerInfo.addStoredInk(-5, player);
			InkProjectileEntity proj = new InkProjectileEntity(player.level, player, ColorUtils.getPlayerColor(player), InkBlockUtils.getInkType(player), 1 + stackMultiplier , new ShooterWeaponSettings("egg").setBaseDamage(6.5f  + stack.getCount()), stack);
			proj.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, 1f, player.isOnGround() ? 10 : 3);

			proj.trailSize = 0.75f + stackMultiplier * 0.1f;
			proj.trailCooldown = 2;
			proj.impactCoverage = 0.95f + stackMultiplier * 0.1f;

			proj.setGravity(0.25f);
			proj.setStraightShotTime(5);

			proj.setProjectileType(InkProjectileEntity.Types.SHOOTER);

			player.level.addFreshEntity(proj);

			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onItemPickUp(EntityItemPickupEvent event)
	{
		if(!(event.getEntityLiving() instanceof Player player))
			return;
		ItemStack stack = event.getItem().getItem();

		if(stack.is(Items.INK_SAC))
		{
			PlayerInfo info = PlayerInfoCapability.get(player);

			float maxInk = info.getMaxInk(player);

			if(info.getStoredInk() < maxInk)
			{
				int inkSacsToConsume = Math.min(stack.getCount(), (int) ((maxInk - info.getStoredInk()) / 10));
				info.addStoredInk(inkSacsToConsume * 10, player);
				stack.shrink(inkSacsToConsume);
			}
		}
		else if (stack.isEdible() && player.canEat(stack.getFoodProperties(player).canAlwaysEat()))
		{
			player.eat(player.level, stack);
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void onKey(InputEvent event)
	{
	}
}
