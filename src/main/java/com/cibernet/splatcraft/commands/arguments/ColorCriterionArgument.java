package com.cibernet.splatcraft.commands.arguments;

import com.cibernet.splatcraft.Splatcraft;
import com.cibernet.splatcraft.handlers.ScoreboardHandler;
import com.cibernet.splatcraft.util.ColorUtils;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ColorCriterionArgument extends InkColorArgument
{
	
	public static final DynamicCommandExceptionType CRITERION_NOT_FOUND = new DynamicCommandExceptionType((p_208663_0_) -> new TranslationTextComponent("arg.colorCriterion.notFound", p_208663_0_));
	
	@Override
	public Integer parse(StringReader reader) throws CommandSyntaxException
	{
		int color = super.parse(reader);
		
		if(!ScoreboardHandler.hasColorCriterion(color))
			throw CRITERION_NOT_FOUND.create(ColorUtils.getColorName(color));
		return color;
	}
	
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
	{
		return ISuggestionProvider.suggest(ScoreboardHandler.getCriteriaSuggestions(), builder);
	}
}
