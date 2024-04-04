package net.splatcraft.forge.network.s2c;

import java.util.HashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.splatcraft.forge.data.capabilities.worldink.WorldInk;
import net.splatcraft.forge.data.capabilities.worldink.WorldInkCapability;
import net.splatcraft.forge.util.InkBlockUtils;
import net.splatcraft.forge.util.RelativeBlockPos;

public class UpdateInkPacket extends PlayS2CPacket
{
	private final ChunkPos chunkPos;
	private final HashMap<RelativeBlockPos, WorldInk.Entry> dirty;

	public UpdateInkPacket(BlockPos pos, int color, InkBlockUtils.InkType type)
	{
		chunkPos = new ChunkPos(pos);
		this.dirty = new HashMap<>();
		dirty.put(RelativeBlockPos.fromAbsolute(pos), new WorldInk.Entry(color, type));
	}

	public UpdateInkPacket(ChunkPos pos, HashMap<RelativeBlockPos, WorldInk.Entry> dirty)
	{
		this.chunkPos = pos;
		this.dirty = dirty;
	}

	public static UpdateInkPacket decode(FriendlyByteBuf buffer)
	{
		ChunkPos pos = buffer.readChunkPos();
		HashMap<RelativeBlockPos, WorldInk.Entry> dirty = new HashMap<>();
		int size = buffer.readInt();
		for (int i = 0; i < size; i++) {
			dirty.put(RelativeBlockPos.fromBuf(buffer), new WorldInk.Entry(buffer.readInt(), InkBlockUtils.InkType.values.getOrDefault(buffer.readResourceLocation(), null)));
		}

		return new UpdateInkPacket(pos, dirty);
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
		ClientLevel level = Minecraft.getInstance().level;

		if(level != null)
		{
            WorldInk worldInk = WorldInkCapability.get(level, chunkPos);

			dirty.forEach((pos, entry) ->
			{
				if (entry == null || entry.type() == null) {
					worldInk.removeInk(pos);
				} else {
					worldInk.setInk(pos, entry.color(), entry.type());
				}


				BlockPos blockPos = pos.toAbsolute(chunkPos);
				BlockState state = level.getBlockState(blockPos);
				level.sendBlockUpdated(blockPos, state, state, 0);
			});
		}

	}
}
