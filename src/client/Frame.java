package client;

public interface Frame {
	
	public static final String MANAGEMENT_FRAME = "00";
	public static final String CONTROL_FRAME = "01";
	public static final String DATA_FRAME = "10";
	
	
	public interface ManagementFrame {
		  public static final String ASSOCIATION_REQUEST = "0000";
		  public static final String ASSOCIATION_RESPONSE = "0001";
		  public static final String REASSOCIATION_REQUEST = "0010";
		  public static final String REASSOCIATION_RESPONSE = "0011";
		  public static final String PROBE_REQUEST = "0100";
		  public static final String PROBE_RESPONSE = "0101";
		  public static final String BEACON = "1000";
		  public static final String AITM = "1001";
		  public static final String DISASSOCIATION = "1010";
		  public static final String AUTHENTICATION = "1011";
		  public static final String DEAUTHENTICATION = "1100";
	}
	
	public interface ControlFrame {
		public static final String BLOCK_ACK_REQUEST = "1000";
		public static final String BLOCK_ACK = "1001";
		public static final String POWER_SAVE_POLL = "1010";
		public static final String RTS = "1011";
		public static final String CTS = "1100";
		public static final String ACK = "1101";
		public static final String CONTENTION_FREE_END = "1110";
		public static final String CONTENTION_FREE_END_ACK = "1111";
	}
	
	public interface DataFrame {
		public static final String DATA = "0000";
		public static final String DATA_CF_ACK = "0001";
		public static final String DATA_CF_POLL = "0010";
		public static final String NULL = "0100";
		public static final String QoS = "1000";
	}
	
	public interface TaggedParameters {
		public static final String SSID = "00";
		public static final String SUPPORTED_RATES = "01";
		public static final String EXTENDED_SUPPORTED_RATES = "32";
		public static final String HT_CAPABILITIES = "2d";
		public static final String VENDOR_SPECIFIC = "dd";
		public static final String DS_PARAMETER = "03";
		public static final String ERP_INFORMATION = "2a";
		public static final String HT_INFORMATION = "3d";
		public static final String SECONDARY_CHANNEL_OFFSET = "3e";
		public static final String RSN_INFORMATION = "30";
		public static final String EXTENDED_CAPABILITIES = "7f";
		public static final String COUNTRY_INFORMATION = "07";
	}
}
