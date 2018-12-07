package com.jacky.git

import javax.crypto.spec.DESKeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory

/**
 * User: oriy
 * Date: 05/11/2018
 */
class EncryptionUtil {

    private static final def KEY_SPEC = [234.byteValue(), 133.byteValue(), 242.byteValue(), 30, 65, 192.byteValue(), 17, 215.byteValue(),
            199.byteValue(), 138.byteValue(), 240.byteValue(), 26, 83, 200.byteValue(), 15, 213.byteValue()] as byte[]

    private static final def DES = "DES"
    private static final def DES_W_PADDING_TRANS = "DES/ECB/PKCS5Padding"

    private static final def base64Encoder = Base64.getUrlEncoder().withoutPadding()

    private static def getKey = {
        def keySpec = new DESKeySpec(KEY_SPEC)
        def secretKeyFactory = SecretKeyFactory.getInstance(DES)
        secretKeyFactory.generateSecret(keySpec)
    }

    static String encrypt(String source) {
        def desCipher = Cipher.getInstance(DES_W_PADDING_TRANS)
        desCipher.init(Cipher.ENCRYPT_MODE, getKey())
        def bytes = desCipher.doFinal(source.getBytes())
        base64Encoder.encodeToString(bytes)
    }

    static decrypt(String source) {
        def desCipher = Cipher.getInstance(DES_W_PADDING_TRANS)
        desCipher.init(Cipher.DECRYPT_MODE, getKey())
        def bytes = Base64.getUrlDecoder().decode(source)
        def bytes1 = desCipher.doFinal(bytes)
        new String(bytes1)
    }

}
