package net.runelite.client.plugins.ibotutils;

import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class GrandExchangeUtils
{
	/*@Inject
	private OSBGrandExchangeClient osbGrandExchangeClient;

	private OSBGrandExchangeResult osbGrandExchangeResult;

	@Provides
	OSBGrandExchangeClient provideOsbGrandExchangeClient(OkHttpClient okHttpClient)
	{
		return new OSBGrandExchangeClient(okHttpClient);
	}

	@Provides
	GrandExchangeClient provideGrandExchangeClient(OkHttpClient okHttpClient)
	{
		return new GrandExchangeClient(okHttpClient);
	}

	public OSBGrandExchangeResult getOSBItem(int itemId)
	{
		log.debug("Looking up OSB item price {}", itemId);
		osbGrandExchangeClient.lookupItem(itemId)
				.subscribe(
						(osbresult) ->
						{
							if (osbresult != null && osbresult.getOverall_average() > 0)
							{
								osbGrandExchangeResult = osbresult;
							}
						},
						(e) -> log.debug("Error getting price of item {}", itemId, e)
				);
		if (osbGrandExchangeResult != null)
		{
			return osbGrandExchangeResult;
		}
		else
		{
			return null;
		}
	}*/
}
