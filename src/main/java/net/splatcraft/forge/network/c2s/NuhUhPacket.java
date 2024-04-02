package net.splatcraft.forge.network.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import net.splatcraft.forge.Splatcraft;

public class NuhUhPacket extends PlayC2SPacket
{
	@Override
	public void encode(FriendlyByteBuf buffer)
	{

	}

	public static NuhUhPacket decode(FriendlyByteBuf buffer)
	{
		return new NuhUhPacket();
	}
	private static final DamageSource damage = new DamageSource(Splatcraft.MODID + ":nuh_uh").bypassInvul();

	@Override
	public void execute(Player player)
	{
		player.hurt(damage, Float.MAX_VALUE);
		player.level.explode(player, player.position().x, player.position().y, player.position().z, 2, Explosion.BlockInteraction.NONE);
		player.setDeltaMovement(new Vec3(0, 3, 0));
		player.hasImpulse = true;
	}
}
