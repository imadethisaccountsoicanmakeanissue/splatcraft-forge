package net.splatcraft.forge.data.capabilities.playerinfo;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.splatcraft.forge.handlers.SplatcraftCommonHandler;
import net.splatcraft.forge.items.InkTankItem;
import net.splatcraft.forge.network.SplatcraftPacket;
import net.splatcraft.forge.network.SplatcraftPacketHandler;
import net.splatcraft.forge.network.s2c.UpdatePlayerInfoPacket;
import net.splatcraft.forge.util.ColorUtils;
import net.splatcraft.forge.util.InkBlockUtils;
import net.splatcraft.forge.util.PlayerCharge;
import net.splatcraft.forge.util.PlayerCooldown;

public class PlayerInfo
{
    private int color;
    private boolean isSquid = false;

    private float storedInk = 0;

    private boolean isTarp = false;
    private boolean isUpsideDown = false;

    private boolean initialized = false;
    private NonNullList<ItemStack> matchInventory = NonNullList.create();
    private PlayerCooldown playerCooldown = null;
    private PlayerCharge playerCharge = null;
    private Player player;

    private ItemStack inkBand = ItemStack.EMPTY;

    public PlayerInfo(int defaultColor)
    {
        color = defaultColor;
    }

    public PlayerInfo()
    {
        this(ColorUtils.getRandomStarterColor());
    }
    
    public boolean isInitialized()
    {
        return initialized;
    }
    
    public void setPlayer(Player entity) {
        this.player = entity;
    }
    
    public void setInitialized(boolean init)
    {
        initialized = init;
    }

    
    public int getColor()
    {
        return color;
    }
    
    public void setColor(int color)
    {
        this.color = color;

        if(player != null)
            SplatcraftCommonHandler.LOCAL_COLOR.put(player, color);
    }

    public boolean isSquid()
    {
        return true;
    }
    
    public void setIsSquid(boolean isSquid)
    {
        this.isSquid = isSquid;
    }

    public ItemStack getInkBand() {
        return inkBand;
    }

    
    public void setInkBand(ItemStack stack) {
        inkBand = stack;
    }
    
    public InkBlockUtils.InkType getInkType() {
        return InkBlockUtils.getInkTypeFromStack(inkBand);
    }

    public NonNullList<ItemStack> getMatchInventory()
    {
        return matchInventory;
    }

    public void setMatchInventory(NonNullList<ItemStack> inventory)
    {
        this.matchInventory = inventory;
    }

    public PlayerCooldown getPlayerCooldown()
    {
        return playerCooldown;
    }

    
    public void setPlayerCooldown(PlayerCooldown cooldown)
    {
        this.playerCooldown = cooldown;
    }

    
    public boolean hasPlayerCooldown()
    {
        return playerCooldown != null && playerCooldown.getTime() > 0;
    }

    
    public PlayerCharge getPlayerCharge()
    {
        return playerCharge;
    }
    
    public void setPlayerCharge(PlayerCharge charge)
    {
        playerCharge = charge;
    }

    public void setStoredInk(float ink)
    {
        storedInk = ink;
    }

    public float getStoredInk() {
        return storedInk;
    }

    public float getMaxInk(LivingEntity player)
    {
        ItemStack chestpiece = player.getItemBySlot(EquipmentSlot.CHEST);
        return 100 + ((chestpiece.getItem() instanceof InkTankItem inkTank) ? inkTank.capacity : 0);
    }

    public void addStoredInk(float v, Player player)
    {
        storedInk = Mth.clamp(storedInk + v, 0, getMaxInk(player));
        if(!player.level.isClientSide)
            SplatcraftPacketHandler.sendToPlayer(new UpdatePlayerInfoPacket(player), (ServerPlayer) player);
    }

    public boolean isUpsideDown() {
        return isUpsideDown;
    }

    public boolean isTarp() {
        return isTarp;
    }

    public void setTarp(boolean tarp, Player player) {
        isTarp = tarp;
        if(player instanceof ServerPlayer serverPlayer)
            SplatcraftPacketHandler.sendToPlayer(new UpdatePlayerInfoPacket(serverPlayer), serverPlayer);
    }

    public void setUpsideDown(boolean upsideDown, Player player) {
        isUpsideDown = upsideDown;
        if(player instanceof ServerPlayer serverPlayer)
            SplatcraftPacketHandler.sendToPlayer(new UpdatePlayerInfoPacket(serverPlayer), serverPlayer);
    }

    public CompoundTag writeNBT(CompoundTag nbt)
    {
        nbt.putInt("Color", getColor());
        nbt.putBoolean("IsSquid", isSquid());
        nbt.putString("InkType", getInkType().getSerializedName());
        nbt.putBoolean("Initialized", initialized);
        nbt.putFloat("StoredInk", storedInk);

        nbt.putBoolean("Tarp", isTarp);
        nbt.putBoolean("UpsideDown", isUpsideDown);

        if(!inkBand.isEmpty())
            nbt.put("InkBand", getInkBand().serializeNBT());

        if (!matchInventory.isEmpty())
        {
            CompoundTag invNBT = new CompoundTag();
            ContainerHelper.saveAllItems(invNBT, matchInventory);
            nbt.put("MatchInventory", invNBT);
        }

        if (playerCooldown != null)
        {
            CompoundTag cooldownNBT = new CompoundTag();
            playerCooldown.writeNBT(cooldownNBT);
            nbt.put("PlayerCooldown", cooldownNBT);
        }

        return nbt;
    }
    
    public void readNBT(CompoundTag nbt)
    {
        setColor(ColorUtils.getColorFromNbt(nbt));
        setIsSquid(nbt.getBoolean("IsSquid"));
        setInitialized(nbt.getBoolean("Initialized"));
        setStoredInk(nbt.getFloat("StoredInk"));

        isTarp = nbt.getBoolean("Tarp");
        isUpsideDown = nbt.getBoolean("UpsideDown");

        if(nbt.contains("InkBand"))
            setInkBand(ItemStack.of(nbt.getCompound("InkBand")));
        else setInkBand(ItemStack.EMPTY);

        if (nbt.contains("MatchInventory"))
        {
            NonNullList<ItemStack> nbtInv = NonNullList.withSize(41, ItemStack.EMPTY);
            ContainerHelper.loadAllItems(nbt.getCompound("MatchInventory"), nbtInv);
            setMatchInventory(nbtInv);
        }

        if (nbt.contains("PlayerCooldown"))
        {
            setPlayerCooldown(PlayerCooldown.readNBT(nbt.getCompound("PlayerCooldown")));
        }
    }

    float prevFallDistance = 0;

	public float getPrevFallDistance()
    {
        return prevFallDistance;
	}

    public void setPrevFallDistance(float fallDistance)
    {
        prevFallDistance = fallDistance;
    }
}
