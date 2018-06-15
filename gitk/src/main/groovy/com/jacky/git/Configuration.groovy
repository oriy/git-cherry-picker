package com.jacky.git

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory

/**
 * Created by alon on 12/15/14.
 */
class Configuration {

    String repository

    String organization

    String gitUserName

    String gitUserEmail

    String gitUserPass

    String gitUserToken

    String gmailUser

    String gmailPass

    static Configuration parseYaml(File configFile) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
        return mapper.readValue(configFile, Configuration.class)
    }
}
