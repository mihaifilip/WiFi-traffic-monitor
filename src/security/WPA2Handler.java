package security;

public class WPA2Handler {
	
	public static final String hex = "0123456789ABCDEF";

	public byte[] PBKDF2_HMAC_SHA1(String inputPassword, String SSID) {
		
		if (inputPassword == null) {
			return null;
		}
		
		byte[] salt= SSID.getBytes();
		int iterations = 4096;
		int keyLen = 256; 
		PseudoRandomFunction prf = new PseudoRandomFunction("HmacSHA1");
		byte[] password = inputPassword.getBytes();
		
		prf.init(password);
		
		int hLen = prf.getHashLen();
        int l = ceil(keyLen, hLen);
        int r = keyLen - (l - 1) * hLen;
        byte T[] = new byte[l * hLen];
        int ti_offset = 0;
        
        for (int i = 1; i <= l; i++) {
            byte U_r[] = new byte[hLen];
            // U0 = S || INT (i);
            byte U_i[] = new byte[salt.length + 4];
            System.arraycopy(salt, 0, U_i, 0, salt.length);
            INT(U_i, salt.length, i);

            for (int j = 0; j < iterations; j++) {
                U_i = prf.doFinal(U_i);
                for (int k = 0; k < U_r.length; k++) {
                	U_r[k] ^= U_i[k];
                }
            }
            System.arraycopy(U_r, 0, T, ti_offset, hLen);
            
            ti_offset += hLen;
        }
        
        if (r < hLen)
        {
            // Incomplete last block
            byte DK[] = new byte[keyLen];
            System.arraycopy(T, 0, DK, 0, keyLen);
            return DK;
        }
        return T;
	}
	
	public void PRF_512(byte[] pmk, byte[] apMAC, byte[] staMAC, byte[] apNonce, byte[] staNonce) {
		
	}
	
	public void PRF_384(byte[] pmk, byte[] apMAC, byte[] staMAC, byte[] apNonce, byte[] staNonce) {
		
	}
	
	public void PRF_256(byte[] gmk, byte[] apMAC, byte[] staMAC, byte[] apNonce, byte[] staNonce) {
		
	}
	
	public void PRF_128(byte[] gmk, byte[] apMAC, byte[] staMAC, byte[] apNonce, byte[] staNonce) {
		
	}
	
	protected void INT(byte[] dest, int offset, int i) {
        dest[offset + 0] = (byte) (i / (256 * 256 * 256));
        dest[offset + 1] = (byte) (i / (256 * 256));
        dest[offset + 2] = (byte) (i / (256));
        dest[offset + 3] = (byte) (i);
    }
	
	protected int ceil(int a, int b) {
        int m = 0;
        if (a % b > 0)
        {
            m = 1;
        }
        return a / b + m;
    }
	
	public String bin2hex(final byte[] b) {
        if (b == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer(2 * b.length);
        for (int i = 0; i < b.length; i++) {
            int v = (256 + b[i]) % 256;
            sb.append(hex.charAt((v / 16) & 15));
            sb.append(hex.charAt((v % 16) & 15));
        }
        return sb.toString();
    }
	
}
