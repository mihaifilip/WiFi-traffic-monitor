package security;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class PseudoRandomFunction {

	static final String DEFAULT_ALGORITHM = "HMacSHA1";
	
	//messageAuthenticationCode
	Mac macAlgorithm = null;
	
	public PseudoRandomFunction() {
		try {
			macAlgorithm = Mac.getInstance(DEFAULT_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	public PseudoRandomFunction(String algorithm) {
		try {
			macAlgorithm = Mac.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	public byte[] doFinal(byte[] b) {
		return macAlgorithm.doFinal(b);
	}
	
	public int getHashLen() {
		return macAlgorithm.getMacLength();
	}
	
	public void init(byte[] password) {
		try {
			macAlgorithm.init(new SecretKeySpec(password, macAlgorithm.getAlgorithm()));
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
    }
	
}
