package com.jacky.git

import org.junit.Test

import static com.jacky.git.EncryptionUtil.*
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotSame

/**
 * User: oriy
 * Date: 05/11/2018
 */
class EncryptionUtilTest {

    private final String CLEAR_TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum"

    private final String EXPECTED_CLEAR_TEXT_ENCRYPTION = "SlTOjlcls12W0bGtKwsPh_fhVnKp7eg1hDnuc04p8CVySPeYRJ6c4VsLAEyOUNMHrwqs9Tpqy-y5SB9gzJ7sEyXG_0MJAQ8WktaoSzQVgw13A5Mna9xz3DNOgTw01ge_D6Rtjc1GqP4WHNMmtdVft_i2DVgG1Dr2g7pEnkCNIaWJHuIHLgzzWA55OPHwPPt2xIDNRgOFUYhTPnHngAKWOKtlJvgQ2GBDfWZb7FGTFwwpH31TStV4GCnRiCiiNeyp1RUmtDDX3Y2trsPrZkW5Z-FcrKOqQt1YqAFLJlNCoQKf6pYbF1I0j3D45HYe0k6b9v0KRTWHOcrQDQvcueP2Xl8ygwV4zoyYDNyKnd-pVvziJSmBmxepn-8fchSrdblBPC7tH72Qq3XxW20Yudv9E3rSRGLLqF1s-tDP28dXINTO0GnAO8-U0dOM4yZGg0-8647M0kBwoXJjjoTNLWoeRxF13u4KuscWsRlCbdjPTVWaQHXQ5mX8KMrnjLlRcl5tRR5H-497fFaTIpT28D-gUVJ7UucRkCIs8z9NFZNfDxNXrBfIjemjKCrxXC0M1JeUZ609TAIzxInjLSzsi230SQ"

    @Test
    void testEncryptConsistency() {
        assertEquals(EXPECTED_CLEAR_TEXT_ENCRYPTION, encrypt(CLEAR_TEXT))
    }

    @Test
    void testEncryptDecrypt() {
        assertEquals(CLEAR_TEXT, decrypt(encrypt(CLEAR_TEXT)))
    }

    @Test
    void testEncryptDifferently() {
        assertNotSame(EXPECTED_CLEAR_TEXT_ENCRYPTION, encrypt(CLEAR_TEXT + '1'))
    }

}
