package com.jacky.git

/**
 * Created by IntelliJ IDEA.
 * User: alon
 * Date: 4/18/13
 * Time: 4:01 PM
 */
class CherryPicksResult {
    String existInBranch
    String commitHash
    String commitMessage


    public boolean isExistInBranch() {
        if (existInBranch.equals("+")) {
            return false
        }
        return true
    }

}
