/*
 * Copyright (c) 2018, Seth <Sethtroll3@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.blastfurnacebot;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.api.Varbits;

public enum Ores
{
	COPPER_ORE(Varbits.BLAST_FURNACE_COPPER_ORE, ItemID.COPPER_ORE),
	TIN_ORE(Varbits.BLAST_FURNACE_TIN_ORE, ItemID.TIN_ORE),
	IRON_ORE(Varbits.BLAST_FURNACE_IRON_ORE, ItemID.IRON_ORE),
	COAL(Varbits.BLAST_FURNACE_COAL, ItemID.COAL),
	MITHRIL_ORE(Varbits.BLAST_FURNACE_MITHRIL_ORE, ItemID.MITHRIL_ORE),
	ADAMANTITE_ORE(Varbits.BLAST_FURNACE_ADAMANTITE_ORE, ItemID.ADAMANTITE_ORE),
	RUNITE_ORE(Varbits.BLAST_FURNACE_RUNITE_ORE, ItemID.RUNITE_ORE),
	SILVER_ORE(Varbits.BLAST_FURNACE_SILVER_ORE, ItemID.SILVER_ORE),
	GOLD_ORE(Varbits.BLAST_FURNACE_GOLD_ORE, ItemID.GOLD_ORE);

	private static final Map<Varbits, Ores> VARBIT;

	static
	{
		ImmutableMap.Builder<Varbits, Ores> builder = new ImmutableMap.Builder<>();

		for (Ores s : values())
		{
			builder.put(s.getVarbit(), s);
		}

		VARBIT = builder.build();
	}

	@Getter(AccessLevel.PACKAGE)
	private final Varbits varbit;
	@Getter(AccessLevel.PACKAGE)
	private final int oreID;

	Ores(Varbits varbit, int oreID)
	{
		this.varbit = varbit;
		this.oreID = oreID;
	}

	public static Ores getVarbit(Varbits varbit)
	{
		return VARBIT.get(varbit);
	}

}
