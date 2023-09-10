package model.config

import kotlinx.serialization.json.Json
import net.mamoe.yamlkt.Yaml
import net.peanuuutz.tomlkt.Toml

actual fun loadFromResource(json: Json, toml: Toml, yaml: Yaml): ConfigurationSettings {
    val content = ConfigurationSettings::class.java.getResource("/default.yml")?.readText()
    return yaml.decodeFromString(ConfigurationSettings.serializer(), content!!)
}
