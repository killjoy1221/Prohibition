package mnm.plugins.prohibition

import com.google.common.reflect.TypeToken
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.data.value.ValueContainer
import org.spongepowered.api.data.value.mutable.CompositeValueStore
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.item.inventory.ItemStack
import java.util.*

inline fun <reified T> ConfigurationNode.getValue(noinline def: () -> T): T = this.getValue(TypeToken.of(T::class.java), def)
inline fun <reified T> ConfigurationNode.setValue(value: T): ConfigurationNode = this.setValue(TypeToken.of(T::class.java), value)

operator fun ConfigurationNode.get(vararg node: String): ConfigurationNode = this.getNode(*node)

inline fun <reified T> Cause.first(): T? = !this.first(T::class.java)
inline fun <reified T> Cause.last(): T? = !this.last(T::class.java)
inline fun <reified T> Cause.before(): Any? = !this.before(T::class.java)
inline fun <reified T> Cause.after(): Any? = !this.after(T::class.java)
inline fun <reified T> Cause.allOf(): List<T> = this.allOf(T::class.java)
inline fun <reified T> Cause.noneOf(): List<Any> = this.noneOf(T::class.java)

inline fun <reified T : ValueContainer<*>> CompositeValueStore<*, in T>.get(): T? = !this.get(T::class.java)

operator fun <T> Optional<T>?.not(): T? = this.unwrap()
val <T : Any> T?.optional: Optional<T> get() = Optional.ofNullable(this)
fun <T> Optional<T>?.unwrap(): T? = this?.orElse(null)

fun ItemStack(builder: ItemStack.Builder.() -> Unit): ItemStack = ItemStack.builder().apply(builder).build()
