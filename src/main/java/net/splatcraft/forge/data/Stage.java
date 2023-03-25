package net.splatcraft.forge.data;

import net.minecraft.command.impl.ScoreboardCommand;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.splatcraft.forge.registries.SplatcraftGameRules;
import net.splatcraft.forge.util.ColorUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Stage
{
	public static final ArrayList<String> VALID_SETTINGS = new ArrayList<>();

	public BlockPos cornerA;
	public BlockPos cornerB;
	public ResourceLocation dimID;

	private final HashMap<String, Boolean> settings = new HashMap<>();
	private final HashMap<String, Integer> teams = new HashMap<>();

	static
	{
		registerGameruleSetting(SplatcraftGameRules.INK_DECAY);
		registerGameruleSetting(SplatcraftGameRules.UNIVERSAL_INK);
		registerGameruleSetting(SplatcraftGameRules.REQUIRE_INK_TANK);
		registerGameruleSetting(SplatcraftGameRules.KEEP_MATCH_ITEMS);
		registerGameruleSetting(SplatcraftGameRules.WATER_DAMAGE);
		registerGameruleSetting(SplatcraftGameRules.INK_FRIENDLY_FIRE);
		registerGameruleSetting(SplatcraftGameRules.INK_HEALING);
		registerGameruleSetting(SplatcraftGameRules.INK_HEALING_CONSUMES_HUNGER);
		registerGameruleSetting(SplatcraftGameRules.INKABLE_GROUND);
		registerGameruleSetting(SplatcraftGameRules.RECHARGEABLE_INK_TANK);
	}

	public CompoundNBT writeData()
	{
		CompoundNBT nbt = new CompoundNBT();

		nbt.put("CornerA", NBTUtil.writeBlockPos(cornerA));
		nbt.put("CornerB", NBTUtil.writeBlockPos(cornerB));
		nbt.putString("Dimension", dimID.toString());

		CompoundNBT settingsNbt = new CompoundNBT();
		CompoundNBT teamsNbt = new CompoundNBT();

		for(Map.Entry<String, Boolean> setting : settings.entrySet())
			settingsNbt.putBoolean(setting.getKey(), setting.getValue());
		nbt.put("Settings", settingsNbt);

		for(Map.Entry<String, Integer> team : teams.entrySet())
			teamsNbt.putInt(team.getKey(), team.getValue());
		nbt.put("Teams", teamsNbt);

		return nbt;
	}

	public boolean hasSetting(String key)
	{
		return settings.containsKey(key);
	}

	public boolean hasSetting(GameRules.RuleKey<GameRules.BooleanValue> rule)
	{
		return hasSetting(rule.toString().replace("splatcraft.", ""));
	}

	public boolean getSetting(String key)
	{
		return settings.get(key);
	}

	public boolean getSetting(GameRules.RuleKey<GameRules.BooleanValue> rule)
	{
		return getSetting(rule.toString().replace("splatcraft.", ""));
	}

	public void applySetting(String key, @Nullable Boolean value)
	{
		if(value == null)
			settings.remove(key);
		else settings.put(key, value);
	}

	public boolean hasTeam(String teamId)
	{
		return teams.containsKey(teamId);
	}

	public int getTeamColor(String teamId)
	{
		return hasTeam(teamId) ? teams.get(teamId) : -1;
	}

	public void setTeamColor(String teamId, int teamColor)
	{
		teams.put(teamId, teamColor);
	}

	public void removeTeam(String teamId)
	{
		teams.remove(teamId);
	}

	public Collection<String> getTeamIds()
	{
		return teams.keySet();
	}

	public Stage(CompoundNBT nbt)
	{
		cornerA = NBTUtil.readBlockPos(nbt.getCompound("CornerA"));
		cornerB = NBTUtil.readBlockPos(nbt.getCompound("CornerB"));
		dimID = new ResourceLocation(nbt.getString("Dimension"));

		settings.clear();

		CompoundNBT settingsNbt = nbt.getCompound("Settings");
		for(String key : settingsNbt.getAllKeys())
			settings.put(key, settingsNbt.getBoolean(key));

		CompoundNBT teamsNbt = nbt.getCompound("Teams");
		for(String key : teamsNbt.getAllKeys())
			teams.put(key, teamsNbt.getInt(key));
	}

	public Stage(World level, BlockPos posA, BlockPos posB)
	{
		dimID = level.dimension().location();

		cornerA = posA;
		cornerB = posB;
	}

	public static void registerGameruleSetting(GameRules.RuleKey<GameRules.BooleanValue> rule)
	{
		VALID_SETTINGS.add(rule.toString().replace("splatcraft.", ""));
	}
}
