package mnm.plugins.prohibition;

import com.google.inject.Inject
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.loader.ConfigurationLoader
import org.spongepowered.api.config.DefaultConfig
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.entity.projectile.ThrownPotion
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.entity.projectile.LaunchProjectileEvent
import org.spongepowered.api.event.filter.cause.First
import org.spongepowered.api.event.game.GameReloadEvent
import org.spongepowered.api.event.game.state.GameStartedServerEvent
import org.spongepowered.api.event.item.inventory.InteractItemEvent
import org.spongepowered.api.event.item.inventory.UseItemStackEvent
import org.spongepowered.api.item.ItemType
import org.spongepowered.api.item.ItemTypes
import org.spongepowered.api.item.inventory.ItemStackSnapshot
import org.spongepowered.api.plugin.Dependency
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.service.permission.Subject
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.chat.ChatTypes
import org.spongepowered.api.text.format.TextColors
import org.spongepowered.api.text.format.TextStyles

@Plugin(
        id = "prohibition",
        name = "Prohibition",
        description = "Blocks potions",
        authors = ["killjoy1221"],
        dependencies = [(Dependency(id = "spotlin"))]
)
class Prohibition {

    companion object {
        const val PERM_BYPASS_DRINKS = "prohibition.bypass.drinks"
        const val PERM_BYPASS_SPLASH = "prohibition.bypass.splash"
        const val PERM_BYPASS_LINGER = "prohibition.bypass.linger"
    }

//    @Inject
//    lateinit var logger: Logger

    @DefaultConfig(sharedRoot = true)
    @Inject
    lateinit var configLoader: ConfigurationLoader<CommentedConfigurationNode>

    private lateinit var config: Config


    @Listener
    fun onServerStart(event: GameStartedServerEvent) {
        loadConfig()
    }

    @Listener
    fun onReload(event: GameReloadEvent) {

        loadConfig()
    }

    private fun loadConfig() {
        val node = configLoader.load()
        config = node["prohibition"].getValue<Config>(::Config)
        node["prohibition"].setValue<Config>(config)
        configLoader.save(node)

    }

    @Listener
    fun onPotionUse(event: InteractItemEvent.Secondary, @First player: Player) {
        if (event.itemStack.type != ItemTypes.POTION) {
            val cancel = onPotionUse(event.itemStack, player)
            if (cancel) event.isCancelled = true
        }
    }

    @Listener
    fun onPotionUse(event: UseItemStackEvent.Tick, @First player: Player) {
        val cancel = onPotionUse(event.itemStackInUse, player)
        if (cancel) event.remainingDuration = 25
    }

    private fun onPotionUse(itemStack: ItemStackSnapshot, player: Player): Boolean {
        val perm = getBypassPermission(itemStack.type)

        if (perm != null && config.drinks) {

            // this is the easy part
            val effects = itemStack.get(Keys.POTION_EFFECTS).orElseGet { emptyList() }
            val cfg = isEnabled(itemStack.type)
            if (cfg && !player.hasPermission(perm) && config.isBlacklisted(*effects.toTypedArray())) {

                player.sendMessage(ChatTypes.ACTION_BAR, Text.of(
                        TextStyles.BOLD, TextColors.DARK_RED,
                        effects.firstOrNull { config.isBlacklisted(it) }?.type ?: "Water",
                        " potion is not allowed"))
                return true
            }
        }
        return false
    }

    @Listener
    fun onEntitySpawn(event: LaunchProjectileEvent) {
        val shooter = event.targetEntity.shooter
        if (config.filter && event.targetEntity is ThrownPotion) {
            val potion = event.targetEntity as ThrownPotion
            val stack = potion.item().get()
            val bypass = getBypassPermission(stack.type)
            val perm = if (shooter is Subject) shooter.hasPermission(bypass!!) else false
            if (perm && isEnabled(stack.type)) {
                stack.get(Keys.POTION_EFFECTS).ifPresent {
                    val effects = it.filter { config.isBlacklisted() }
                    val otherStack = ItemStack {
                        fromSnapshot(stack)
                        add(Keys.POTION_EFFECTS, effects)
                    }.createSnapshot()

                    potion.item().set(otherStack)
                }
            }

        }

    }

    private fun isEnabled(stack: ItemType): Boolean {
        return when (stack) {
            ItemTypes.POTION -> config.drinks
            ItemTypes.SPLASH_POTION -> config.splash
            ItemTypes.LINGERING_POTION -> config.linger
            else -> false
        }
    }

    private fun getBypassPermission(stack: ItemType): String? {
        // Return a permission if potion, null if not
        return when (stack) {
            ItemTypes.POTION -> PERM_BYPASS_DRINKS
            ItemTypes.SPLASH_POTION -> PERM_BYPASS_SPLASH
            ItemTypes.LINGERING_POTION -> PERM_BYPASS_LINGER
            else -> null
        }
    }
}
