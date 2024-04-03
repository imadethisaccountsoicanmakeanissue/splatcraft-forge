package net.splatcraft.forge.data.capabilities.worldink;

import java.util.HashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.splatcraft.forge.util.InkBlockUtils;
import net.splatcraft.forge.util.RelativeBlockPos;
import org.jetbrains.annotations.ApiStatus;


/*  TODO
	make old inked blocks decay instantly
	piston push interactions
	fix rendering bugs (See WorldInkHandler.Render comment)
	finish Rubidium support
	add Embeddium support
	add Oculus support
	screw OptiFine
 */

/**
 * This class manages internal storage of ink and permanent ink.
 * All methods here modify the appropriate map directly and do nothing else. Consumers should use InkBlockUtils instead.
 *
 * @see InkBlockUtils
 */
@ApiStatus.Internal
public class WorldInk {
	private final HashMap<RelativeBlockPos, WorldInk.Entry> INK_MAP = new HashMap<>();
	private final HashMap<RelativeBlockPos, WorldInk.Entry> PERMANENT_INK_MAP = new HashMap<>();

	public boolean isInked(RelativeBlockPos pos)
	{
		return getInk(pos) != null;
	}

	public void setInk(RelativeBlockPos pos, int color, InkBlockUtils.InkType type) {
		INK_MAP.put(pos, new Entry(color, type));
	}

	public void setPermanentInk(RelativeBlockPos pos, int color, InkBlockUtils.InkType type) {
		PERMANENT_INK_MAP.put(pos, new Entry(color, type));
	}

	public BlockClearResult removeInk(RelativeBlockPos pos) {
		WorldInk.Entry ink = getInk(pos);
		if (ink == null || ink.equals(getPermanentInk(pos))) {
			return BlockClearResult.FAIL;
		}

		if (hasPermanentInk(pos)) {
			INK_MAP.put(pos, getPermanentInk(pos));
			return BlockClearResult.REPLACED_BY_PERMANENT_INK;
		}

		INK_MAP.remove(pos);
		return BlockClearResult.SUCCESS;
	}

	public boolean removePermanentInk(RelativeBlockPos pos)
	{
		if(hasPermanentInk(pos)) {
			PERMANENT_INK_MAP.remove(pos);
			return true;
		}
		return false;
	}

	public HashMap<RelativeBlockPos, WorldInk.Entry> getInkInChunk()
	{
		return INK_MAP;
	}

	public HashMap<RelativeBlockPos, WorldInk.Entry> getPermanentInkInChunk()
	{
		return PERMANENT_INK_MAP;
	}

	public Entry getInk(RelativeBlockPos pos) {
		return INK_MAP.get(pos);
	}

	public boolean hasPermanentInk(RelativeBlockPos pos)
	{
		return getPermanentInk(pos) != null;
	}

	public Entry getPermanentInk(RelativeBlockPos pos) {
		return PERMANENT_INK_MAP.get(pos);
	}

	public CompoundTag writeNBT(CompoundTag nbt)
	{
		ListTag inkMapList = new ListTag();

		INK_MAP.forEach((pos, entry) ->
		{
			CompoundTag element = new CompoundTag();
			element.put("Pos", pos.writeNBT(new CompoundTag()));
			element.putInt("Color", entry.color);
			element.putString("Type", entry.type.getName().toString());

			inkMapList.add(element);
		});

		nbt.put("Ink", inkMapList);

		ListTag permanentInkMapList = new ListTag();

		PERMANENT_INK_MAP.forEach((pos, entry) ->
		{
			CompoundTag element = new CompoundTag();
			element.put("Pos", pos.writeNBT(new CompoundTag()));
			element.putInt("Color", entry.color);
			element.putString("Type", entry.type.getName().toString());

			permanentInkMapList.add(element);
		});

		nbt.put("PermanentInk", permanentInkMapList);

		return nbt;
	}

	public void readNBT(CompoundTag nbt)
	{
		PERMANENT_INK_MAP.clear();
		nbt.getList("PermanentInk", Tag.TAG_COMPOUND).forEach(tag ->
		{
			CompoundTag element = (CompoundTag) tag;
			setPermanentInk(RelativeBlockPos.readNBT(element.getCompound("Pos")), element.getInt("Color"), InkBlockUtils.InkType.values.get(new ResourceLocation(element.getString("Type"))));
		});

		INK_MAP.clear();
		nbt.getList("Ink", Tag.TAG_COMPOUND).forEach(tag ->
		{
			CompoundTag element = (CompoundTag) tag;
			setInk(RelativeBlockPos.readNBT(element.getCompound("Pos")), element.getInt("Color"), InkBlockUtils.InkType.values.get(new ResourceLocation(element.getString("Type"))));
		});
	}

	public enum BlockClearResult {
		SUCCESS,
		REPLACED_BY_PERMANENT_INK,
		FAIL
	}

	public record Entry(int color, InkBlockUtils.InkType type) {
	}
}
