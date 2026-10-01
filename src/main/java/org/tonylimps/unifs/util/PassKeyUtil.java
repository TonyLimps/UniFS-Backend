package org.tonylimps.unifs.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class PassKeyUtil {
    private final int CHALLENGE_LENGTH;
    private final Base64.Encoder encoder = Base64.getEncoder();
    private final Base64.Decoder decoder = Base64.getDecoder();
    private final Signature signature;
    private final KeyFactory keyFactory;
    private final SecureRandom random = new SecureRandom();

    @Autowired
    public PassKeyUtil(@Value("${challenge-length}") int challengeLength,
                       @Value("${signature-algorithm}") String alg) throws Exception {
        this.CHALLENGE_LENGTH = challengeLength;
        this.signature = Signature.getInstance(alg);
        this.keyFactory = KeyFactory.getInstance("RSA");
    }

    public String generateChallenge(){
        byte[] challenge = new byte[CHALLENGE_LENGTH];
        random.nextBytes(challenge);
        return encoder.encodeToString(challenge);
    }

    public boolean verify(String rawChallenge, String encodedSignature, String encodedPublicKey){
        try {
            // decode
            var challenge = decoder.decode(rawChallenge);
            var sig = decoder.decode(encodedSignature);

            // restore public key
            var der = decoder.decode(encodedPublicKey);
            var spec = new X509EncodedKeySpec(der);
            var publicKey = keyFactory.generatePublic(spec);

            // verify signature
            signature.initVerify(publicKey);
            signature.update(challenge);
            return signature.verify(sig);
        } catch (Exception e) {
            return false;
        }
    }
}
