package com.jacky.git

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory

import static com.jacky.git.EncryptionUtil.decrypt
import static com.jacky.git.EncryptionUtil.encrypt

/**
 * User: oriy
 * Date: 06/11/2018
 */
class ConfigurationUtil {

    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory())

    static final Configuration configuration

    static {
        InputStream inputStream = getClass().getResourceAsStream("/config.yml")
        configuration = parseYaml(inputStream)
    }

    static Configuration parseYaml(InputStream inputStream) {
        return mapper.readValue(inputStream, Configuration.class)
    }

    static void writeYaml(File configFile, Configuration configuration) {
        mapper.writeValue(configFile, configuration)
    }

    static String decryptPass(String encodedPassword) {
        decrypt(encodedPassword)
    }

    public static void main(String[] args) {

        File configFile = new File("./gitk/src/main/resources/config.yml")
        Configuration configuration = parseYaml(configFile.newInputStream())

        printConfigFile(configFile)

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in))

        br.print("Please enter git user ${configuration.gitUserName}'s password: ")
        def gitUserPass = br.readLine()

        br.print("Please enter gmail account ${configuration.gmailUser}'s password: ")
        def gmailPass = br.readLine()

        configuration.setGitUserPass(encrypt(gitUserPass))
        configuration.setGmailPass(encrypt(gmailPass))
        writeYaml(configFile, configuration)

        println()
        println("--- passwords saved ---")
        println()

        printConfigFile(configFile)
    }

    private static def printConfigFile(File configFile) {
        println()
        println("::: config.yml :::")
        println(configFile.text)
        println()
    }
}
