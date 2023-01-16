package com.jacky.git

import org.junit.Test

import static com.jacky.git.SkipCherryPick.isSkipCherryPickMessage
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class SkipCherryPickTest {

    @Test
    void testSkip() {
        ['no cherry pick',
         'no cherry-pick',
         'no-cheRRy pick',
         'no-cherry-pick',
         'dont cherry pick',
         'dont cherry-pick',
         'dont-cherry pick',
         'dont-cherry-pick',
         "don't cherry pick",
         "don't cherry-pick",
         "don't-cherry pick",
         "don't-cherry-pick",
         'do not cherry pick',
         'do not cherry-pick',
         'do not-cherry pick',
         'do not-cherry-pick',
         'skip cherry pick',
         'skip cherry-pick',
         'skip-cherry pick',
         'skip-cherry-pick',
         'no cherrypick',
         'no cherrypick',
         'no-cheRRypick',
         'no-cherrypick',
        ].each({ String msg ->
            assertSkipCherryPick(msg)
            assertSkipCherryPick("$msg as message prefix")
            assertSkipCherryPick("mid $msg dle of message")
            assertSkipCherryPick("message as suffix $msg")
        })
    }

    static void assertSkipCherryPick(String message) {
        println "Verifying skip cherry pick for message: $message"
        assertTrue("Expected to skip message $message", isSkipCherryPickMessage(message))
    }

    @Test
    void testKeep() {
        assertFalse(isSkipCherryPickMessage(null))
        assertFalse(isSkipCherryPickMessage(""))
        assertFalse(isSkipCherryPickMessage(" a message "))
        assertFalse(isSkipCherryPickMessage("""
                                           | multi
                                           | line
                                           | message
                                           | """))
    }
}
