package ru.pushkin.mmb.security;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@UtilityClass
public final class KeyUtil {

    public static PrivateKey getPrivateKey(String privateKey) {
        try {
            byte[] keyData = readKey(privateKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyData);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Unable to read private key: " + e, e);
        }
    }

    public static PublicKey getPublicKey(String publicKey) {
        try {
            byte[] keyData = readKey(publicKey);
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            Certificate certificate = cf.generateCertificate(new ByteArrayInputStream(keyData));
            return certificate.getPublicKey();
        } catch (CertificateException e) {
            throw new IllegalStateException("Unable to read public key: " + e, e);
        }
    }

    private static byte[] readKey(String key) {
        return Base64.getDecoder().decode(StringUtils.deleteWhitespace(key));
    }
}
