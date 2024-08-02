package net.splatcraft.forge.handlers;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.splatcraft.forge.Splatcraft;

public class ScoreboardHandler
{
    public static final ObjectiveCriteria COLOR = new ObjectiveCriteria(Splatcraft.MODID + ".inkColor");
    public static final ObjectiveCriteria TURF_WAR_SCORE = new ObjectiveCriteria(Splatcraft.MODID + ".turfWarScore");

    public static void updatePlayerScore(ObjectiveCriteria criteria, Player player, int color)
    {
        player.getScoreboard().forAllObjectives(criteria, player.getScoreboardName(), p_195397_1_ -> p_195397_1_.setScore(color));
    }

    public static void register()
    {
    }

}
