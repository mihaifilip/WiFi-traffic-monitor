package client;


public class Channel {

	public static final String CHANNEL_1 = "Channel 1 @2412MHz";
	public static final String CHANNEL_2 = "Channel 2 @2417MHz";
	public static final String CHANNEL_3 = "Channel 3 @2422MHz";
	public static final String CHANNEL_4 = "Channel 4 @2427MHz";
	public static final String CHANNEL_5 = "Channel 5 @2432MHz";
	public static final String CHANNEL_6 = "Channel 6 @2437MHz";
	public static final String CHANNEL_7 = "Channel 7 @2442MHz";
	public static final String CHANNEL_8 = "Channel 8 @2447MHz";
	public static final String CHANNEL_9 = "Channel 9 @2452MHz";
	public static final String CHANNEL_10 = "Channel 10 @2457MHz";
	public static final String CHANNEL_11 = "Channel 11 @2462MHz";
	public static final String CHANNEL_12 = "Channel 12 @2467MHz";
	public static final String CHANNEL_13 = "Channel 13 @2472MHz";
	public static final String CHANNEL_14 = "Channel 14 @2484MHz";
	public static final String ERROR = "Unknown frequency";
	
	
	public static String get(int frequency) {
		switch(frequency) {
			case 2412:
				return CHANNEL_1;
			case 2417:
				return CHANNEL_2;
			case 2422:
				return CHANNEL_3;
			case 2427:
				return CHANNEL_4;
			case 2432:
				return CHANNEL_5;
			case 2437:
				return CHANNEL_6;
			case 2442:
				return CHANNEL_7;
			case 2447:
				return CHANNEL_8;
			case 2452:
				return CHANNEL_9;
			case 2457:
				return CHANNEL_10;
			case 2462:
				return CHANNEL_11;
			case 2467:
				return CHANNEL_12;
			case 2472:
				return CHANNEL_13;
			case 2484:
				return CHANNEL_14;
			default:
				return ERROR;
		}
	}
	
	
}
