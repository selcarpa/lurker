package model.config

import io.ktor.http.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.peanuuutz.tomlkt.Toml
import okio.Path.Companion.toPath
import utils.readFile
import kotlin.system.exitProcess


@OptIn(ExperimentalSerializationApi::class)
private val json = Json {
    isLenient = true
    ignoreUnknownKeys = true
    explicitNulls = false
}

private val toml = Toml {
    ignoreUnknownKeys = true
}

object Config {
    var ConfigurationUrl: String? = null
    val Configuration: ConfigurationSettings by lazy { initConfiguration() }

    private fun initConfiguration(): ConfigurationSettings {
        return if (ConfigurationUrl.orEmpty().isEmpty()) {
            TODO("Kotlin native not support read resource file, please use -c=xxx to specify configuration file")
        } else {
            try {
                val content = readFile(ConfigurationUrl!!.toPath())
                if (ConfigurationUrl!!.endsWith("json") || ConfigurationUrl!!.endsWith("json5")) {
                    json.decodeFromString<ConfigurationSettings>(content)
                } else if (ConfigurationUrl!!.endsWith("toml")) {
                    toml.decodeFromString(ConfigurationSettings.serializer(), content)
                } else {
                    throw IllegalArgumentException("not supported file type")
                }
            } catch (ex: Exception) {
                print("load configuration failed: ${ex.message}")
                ex.printStackTrace()
                exitProcess(1)
            }
        }
    }
}

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

