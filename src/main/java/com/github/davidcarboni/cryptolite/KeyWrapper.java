package com.github.davidcarboni.cryptolite;

import org.apache.commons.lang.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.Serializable;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * This class provides secure "wrapping" of keys. Wrapping a key is important if
 * you need to store it in, for example, a database. It is not safe to store a
 * raw key as this could be compromised, so you need to wrap a key before
 * storing it.
 *
 * The wrapping process encrypts the key, so that it can be safely stored.
 * Great! However the wrapping encryption process requires a further key! The
 * question therefore arises - how does one safely store the wrapping key?
 *
 * The answer is to use a "password-based key derivation function" (PBKDF for
 * short). This provides a way to generate the same key repeatedly, from a
 * password (or some other secret string).
 *
 * There is one last problem to be overcome, which is that if two people use
 * identical passwords, the generated keys will be identical. This could perhaps
 * give away someone's password. The answer is therefore to use a "salt value"
 * (which can be generated by calling {@link Random#salt()}). The salt
 * adds randomness to the process so that two people using the same password
 * will still get different keys.
 *
 * This does means you'll need to store the salt value for each person and use
 * it each time in order to ensure you can regenerate the key. This is
 * considered acceptable as the salt is not a particularly sensitive value - all
 * it does is add a little randomness.
 *
 * The cryptographic algorithms used in this class are those indicated in the
 * "Beginning Cryptography with Java" book. See:
 * http://p2p.wrox.com/book-beginning
 * -cryptography-java/67710-wrapping-rsa-keys.html
 *
 * @author David Carboni
 */
public class KeyWrapper implements Serializable {

    /**
     * Generated by Eclipse.
     */
    private static final long serialVersionUID = -6232240044326148130L;

    /**
     * The algorithm for the wrapping key: {@value #WRAP_KEY_ALGORITHM}.
     */
    private static final String WRAP_KEY_ALGORITHM = "AES";

    /**
     * The algorithm to use for wrapping secret keys:
     * {@value #WRAP_ALGORITHM_SYMMETRIC}.
     */
    private static final String WRAP_ALGORITHM_SYMMETRIC = "AESWrap";

    /**
     * The algorithm to use for wrapping private keys:
     * {@value #WRAP_ALGORITHM_ASYMMETRIC}.
     */
    private static final String WRAP_ALGORITHM_ASYMMETRIC = "AES/ECB/PKCS7Padding";

    /**
     * The number of iterations to perform when doing the password-based key
     * derivation to generate the wrapping key: {@value #PBKD_ITERATIONS}.
     */
    public static final int PBKD_ITERATIONS = 1024;

    private SecretKey wrapKey;

    /**
     * This is the constructor you should typically use. It initialises the
     * instance with a wrap key based on the given password and salt values.
     *
     * @param password The password to use as the basis for wrapping keys.
     * @param salt     A value for this can be obtained from
     *                 {@link Random#salt()}. You need to store a salt value
     *                 for each password and ensure the matching one is passed in
     *                 each time this constructor is invoked.
     */
    public KeyWrapper(String password, String salt) {

        wrapKey = Keys.generateSecretKey(password, salt);
    }

    /**
     * This constructor sets the wrap key directly, rather than generating it
     * from a password and salt as {@link #KeyWrapper(String, String)} does.
     *
     * The wrap key must be an {@link #WRAP_KEY_ALGORITHM} key. Both
     * {@link Keys#newSecretKey()} and
     * {@link Keys#generateSecretKey(String, String)} can be used to generate
     * the right type of key.
     *
     * @param wrapKey The key which will be used for wrapping other keys.
     */
    public KeyWrapper(SecretKey wrapKey) {
        if (!StringUtils.equals(WRAP_KEY_ALGORITHM, wrapKey.getAlgorithm())) {
            throw new IllegalArgumentException("The wrapping key algorithm needs to be " + WRAP_KEY_ALGORITHM);
        }
        this.wrapKey = wrapKey;
    }

    /**
     * Wraps the given {@link SecretKey} using
     * {@value #WRAP_ALGORITHM_SYMMETRIC}.
     *
     * @param key The {@link SecretKey} to be wrapped. This method internally
     *            just calls {@link #wrap(Key, String)}, but this provides a
     *            clear naming match with {@link #unwrapSecretKey(String)}.
     * @return A String representation (base64-encoded) of the wrapped
     * {@link SecretKey}, for ease of storage.
     */
    public String wrapSecretKey(SecretKey key) {
        return wrap(key, WRAP_ALGORITHM_SYMMETRIC);
    }

    /**
     * Wraps the given {@link SecretKey} using
     * {@value #WRAP_ALGORITHM_ASYMMETRIC}.
     *
     * @param key The {@link PrivateKey} to be wrapped. This method internally
     *            just calls {@link #wrap(Key, String)}, but this provides a
     *            clear naming match with {@link #unwrapPrivateKey(String)}.
     * @return A String representation (base64-encoded) of the wrapped
     * {@link PrivateKey}, for ease of storage.
     */
    public String wrapPrivateKey(PrivateKey key) {

        return wrap(key, WRAP_ALGORITHM_ASYMMETRIC);
    }

    /**
     * Encodes the given {@link PublicKey} <em>without wrapping</em>. Since a
     * public key is public, this is a convenience method provided to convert it
     * to a String for unprotected storage.
     *
     * This method internally calls {@link ByteArray#toBase64String(byte[])},
     * passing the value of {@link PublicKey#getEncoded()}.
     *
     * @param key The {@link PublicKey} to be encoded.
     * @return A String representation (base64-encoded) of the raw
     * {@link PublicKey}, for ease of storage.
     */
    public static String encodePublicKey(PublicKey key) {
        byte[] bytes = key.getEncoded();
        return ByteArray.toBase64String(bytes);
    }

    /**
     * Unwraps the given encoded {@link SecretKey}, using
     * WRAP_ALGORITHM_SYMMETRIC.
     *
     * @param wrappedKey The wrapped key, base-64 encoded, as returned by
     *                   {@link #wrapSecretKey(SecretKey)} .
     * @return The unwrapped {@link SecretKey}.
     */
    public SecretKey unwrapSecretKey(String wrappedKey) {

        return (SecretKey) unwrap(wrappedKey, Keys.SYMMETRIC_ALGORITHM,
                Cipher.SECRET_KEY, WRAP_ALGORITHM_SYMMETRIC);
    }

    /**
     * Unwraps the given encoded {@link PrivateKey}, using
     * WRAP_ALGORITHM_ASYMMETRIC.
     *
     * @param wrappedKey The wrapped key, base-64 encoded, as returned by
     *                   {@link #wrapPrivateKey(PrivateKey)} .
     * @return The unwrapped {@link PrivateKey}.
     */
    public PrivateKey unwrapPrivateKey(String wrappedKey) {

        return (PrivateKey) unwrap(wrappedKey, Keys.ASYMMETRIC_ALGORITHM,
                Cipher.PRIVATE_KEY, WRAP_ALGORITHM_ASYMMETRIC);
    }

    /**
     * Decodes the given encoded {@link PublicKey}.
     *
     * See:
     * http://stackoverflow.com/questions/2411096/how-to-recover-a-rsa-public
     * -key-from-a-byte- array
     *
     * @param encodedKey The public key, base-64 encoded, as returned by
     *                   {@link #encodePublicKey(PublicKey)}.
     * @return The unwrapped {@link PublicKey}.
     */
    public static PublicKey decodePublicKey(String encodedKey) {

        byte[] bytes = ByteArray.fromBase64String(encodedKey);
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance(Keys.ASYMMETRIC_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            if (SecurityProvider.addProvider()) {
                return decodePublicKey(encodedKey);
            } else {
                throw new IllegalStateException("Algorithm unavailable: " + Keys.SYMMETRIC_ALGORITHM, e);
            }
        }
        try {
            return keyFactory.generatePublic(new X509EncodedKeySpec(bytes));
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException("Unable to convert key '" + encodedKey + "' to a valid public key.", e);
        }
    }

    /**
     * Convenience method to unwrap a public-private key pain in a single call.
     *
     * @param wrappedPrivateKey The wrapped key, base-64 encoded, as returned by
     *                          {@link #wrapPrivateKey(PrivateKey)}.
     * @param encodedPublicKey  The public key, base-64 encoded, as returned by
     *                          {@link #encodePublicKey(PublicKey)}.
     * @return A {@link KeyPair} containing the unwrapped {@link PrivateKey} and the decoded {@link PublicKey}.
     */
    public KeyPair unwrapKeyPair(String wrappedPrivateKey, String encodedPublicKey) {

        PrivateKey privateKey = unwrapPrivateKey(wrappedPrivateKey);
        PublicKey publicKey = decodePublicKey(encodedPublicKey);
        return new KeyPair(publicKey, privateKey);
    }

    /**
     * Wraps the given {@link Key} using the given wrap algorithm.
     *
     * @param key           The {@link Key} to be wrapped. This can be a secret
     *                      (symmetric) key or a private (asymmetric) key.
     * @param wrapAlgorithm The algorithm to use to wrap the key. This has to be different
     *                      for a {@link SecretKey} than for a {@link PrivateKey}.
     * @return A String representation (base64-encoded) of the wrapped
     * {@link Key}.
     */
    private String wrap(Key key, String wrapAlgorithm) {

        try {

            Cipher cipher = Cipher.getInstance(wrapAlgorithm);
            cipher.init(Cipher.WRAP_MODE, wrapKey, Random.getInstance());
            byte[] wrappedKey = cipher.wrap(key);
            return ByteArray.toBase64String(wrappedKey);

        } catch (NoSuchAlgorithmException e) {
            if (SecurityProvider.addProvider()) {
                return wrap(key, wrapAlgorithm);
            } else {
                throw new IllegalStateException("Algorithm unavailable: " + wrapAlgorithm, e);
            }
        } catch (NoSuchPaddingException e) {
            throw new IllegalStateException("Padding unavailable: " + wrapAlgorithm, e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid key for " + wrapAlgorithm, e);
        } catch (IllegalBlockSizeException e) {
            throw new IllegalStateException("Error in block size for algorithm " + wrapAlgorithm, e);
        }
    }

    /**
     * Unwraps the given encoded {@link PrivateKey}.
     *
     * @param wrappedKey    The wrapped key, base-64 encoded, as returned by
     *                      {@link #wrapPrivateKey(PrivateKey)} .
     * @param keyAlgorithm  The algorithm that the reconstituted key will be for.
     * @param keyType       The type of key. This should be a constant from the
     *                      {@link Cipher} class.
     * @param wrapAlgorithm The algorithm to use to unwrap the key. This has to be
     *                      different for a {@link SecretKey} than for a
     *                      {@link PrivateKey}.
     * @return The unwrapped {@link PrivateKey}.
     */
    private Key unwrap(String wrappedKey, String keyAlgorithm, int keyType,
                       String wrapAlgorithm) {

        try {
            byte[] wrapped = ByteArray.fromBase64String(wrappedKey);
            Cipher cipher = Cipher.getInstance(wrapAlgorithm);
            cipher.init(Cipher.UNWRAP_MODE, wrapKey, Random.getInstance());
            Key result = cipher.unwrap(wrapped, keyAlgorithm, keyType);
            return result;

        } catch (NoSuchAlgorithmException e) {
            if (SecurityProvider.addProvider()) {
                return unwrap(wrappedKey, keyAlgorithm, keyType, wrapAlgorithm);
            } else {
                throw new IllegalStateException("Algorithm unavailable: " + wrapAlgorithm, e);
            }
        } catch (NoSuchPaddingException e) {
            throw new IllegalStateException("Padding unavailable: " + wrapAlgorithm, e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid key for algorithm " + wrapAlgorithm, e);
        }
    }

}
