package com.jacky.git

import java.util.regex.Pattern

class SkipCherryPick {

    private static String SKIP_CHERRY_PICK_REGEX = "(no|don'?t|do not|skip):?[ _-]*cherry[ _-]*pick"
    private static Pattern SKIP_CHERRY_PICK_MESSAGE_PATTERN = Pattern.compile(SKIP_CHERRY_PICK_REGEX, Pattern.CASE_INSENSITIVE)

    static boolean isSkipCherryPickMessage(String message) {
        return SKIP_CHERRY_PICK_MESSAGE_PATTERN.matcher(message?:"").find()
    }
}
