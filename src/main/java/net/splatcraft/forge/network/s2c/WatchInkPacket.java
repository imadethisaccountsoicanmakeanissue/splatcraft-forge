package net.splatcraft.forge.network.s2c;

import java.util.HashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.splatcraft.forge.data.capabilities.worldink.WorldInk;
import net.splatcraft.forge.handlers.WorldInkHandler;
import net.splatcraft.forge.util.InkBlockUtils;
import net.splatcraft.forge.util.RelativeBlockPos;

public class WatchInkPacket extends PlayS2CPacket
{
	private final ChunkPos chunkPos;
	private final HashMap<RelativeBlockPos, WorldInk.Entry> dirty;

	public WatchInkPacket(ChunkPos pos, HashMap<RelativeBlockPos, WorldInk.Entry> dirty)
	{
		this.chunkPos = pos;
		this.dirty = dirty;
	}

	public static WatchInkPacket decode(FriendlyByteBuf buffer)
	{
		ChunkPos pos = buffer.readChunkPos();
		HashMap<RelativeBlockPos, WorldInk.Entry> dirty = new HashMap<>();
		int size = buffer.readInt();
		for (int i = 0; i < size; i++) {
			dirty.put(RelativeBlockPos.fromBuf(buffer), new WorldInk.Entry(buffer.readInt(), InkBlockUtils.InkType.values.getOrDefault(buffer.readResourceLocation(), null)));
		}

		return new WatchInkPacket(pos, dirty);
	}

	@Override
	public void encode(FriendlyByteBuf buffer)
	{
		buffer.writeChunkPos(chunkPos);
		buffer.writeInt(dirty.size());

		dirty.forEach((pos, entry) ->
		{
			pos.writeBuf(buffer);
			buffer.writeInt(entry.color());
			buffer.writeResourceLocation(entry.type() == null ? new ResourceLocation("") : entry.type().getName());
		});
	}

	@Override
	public void execute()
	{
		WorldInkHandler.markInkInChunkForUpdate(chunkPos, dirty);
	}
}
