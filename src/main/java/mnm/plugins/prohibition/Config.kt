package mnm.plugins.prohibition;

import ninja.leaping.configurate.objectmapping.Setting
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable
import org.spongepowered.api.effect.potion.PotionEffect
import org.spongepowered.api.effect.potion.PotionEffectType

@ConfigSerializable()
class Config {

    @Setting
    var drinks = false
    @Setting
    var splash = true
    @Setting
    var linger = true
    @Setting
    var effects = setOf<PotionEffectType>()
    @Setting
    var blacklist = false
    @Setting
    var filter = true
    @Setting(comment = "When true, will include water in the effect list")
    var water = false

    fun isBlacklisted(vararg effects: PotionEffect): Boolean {
        if (effects.isEmpty()) return water && blacklist // we're a dry county

        val list: (PotionEffect) -> Boolean = { this.effects.contains(it.type) }

        val allowed = if (blacklist) effects.any(list) else effects.none(list)

        System.out.println(allowed)

        return allowed
    }
}

