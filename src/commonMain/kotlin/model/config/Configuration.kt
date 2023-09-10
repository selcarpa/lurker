package model.config

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.mamoe.yamlkt.Yaml
import net.peanuuutz.tomlkt.Toml
import okio.Path.Companion.toPath
import utils.readFile


@OptIn(ExperimentalSerializationApi::class)
private val json = Json {
    isLenient = true
    ignoreUnknownKeys = true
    explicitNulls = false
}

private val toml = Toml {
    ignoreUnknownKeys = true
}

private val yaml = Yaml {

}

object Config {
    var ConfigurationUrl: String? = null
    val Configuration: ConfigurationSettings by lazy { initConfiguration() }

    private fun initConfiguration(): ConfigurationSettings {
        return if (ConfigurationUrl.orEmpty().isEmpty()) {
           loadFromResource(json, toml, yaml)
        } else {
            val content = readFile(ConfigurationUrl!!.toPath())
            when {
                ConfigurationUrl!!.endsWith("json") || ConfigurationUrl!!.endsWith("json5") -> {
                    json.decodeFromString<ConfigurationSettings>(content)
                }

                ConfigurationUrl!!.endsWith("toml") -> {
                    toml.decodeFromString(ConfigurationSettings.serializer(), content)
                }

                ConfigurationUrl!!.endsWith("yml") || ConfigurationUrl!!.endsWith("yaml") -> {
                    yaml.decodeFromString(ConfigurationSettings.serializer(), content)
                }

                else -> {
                    throw IllegalArgumentException("not supported file type")
                }
            }
        }
    }

}

expect fun loadFromResource(json: Json, toml: Toml, yaml: Yaml): ConfigurationSettings

@Serializable
data class ConfigurationSettings(
    val timeout: Int, var doh: DohSettings = DohSettings(), var dns: DnsSettings = DnsSettings()
)

@Serializable
data class DnsSettings(var udp: DnsUdpSettings = DnsUdpSettings(), var tcp: DnsTcpSettings = DnsTcpSettings())

@Serializable
data class DnsTcpSettings(var enable: Boolean = false, var port: Int = 53)

@Serializable
data class DnsUdpSettings(var enable: Boolean = false, var port: Int = 53)

@Serializable
data class DohSettings(var enable: Boolean = false)

