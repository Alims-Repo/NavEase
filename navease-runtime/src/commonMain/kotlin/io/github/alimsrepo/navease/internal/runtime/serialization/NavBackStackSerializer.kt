package io.github.alimsrepo.navease.internal.runtime.serialization

import androidx.compose.runtime.saveable.rememberSerializable
import io.github.alimsrepo.navease.internal.runtime.NavBackStack
import io.github.alimsrepo.navease.internal.runtime.NavKey
import androidx.savedstate.compose.serialization.serializers.SnapshotStateListSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer

/**
 * A [KSerializer] for [NavBackStack].
 *
 * This serializer wraps a [KSerializer] for the element type [T], enabling serialization and
 * deserialization of [NavBackStack] instances. The serialization of individual elements is
 * delegated to the provided [elementSerializer].
 *
 * If your stack elements [T] are open polymorphic (e.g., a interface for different screens), the
 * provided [elementSerializer] must be correctly configured to handle this.
 *
 * @sample androidx.navigation3.runtime.samples.NavBackStackSerializer_withReflection
 * @param T The type of elements stored in the [NavBackStack].
 * @param elementSerializer The [KSerializer] used to serialize and deserialize individual elements.
 */
public class NavBackStackSerializer<T : NavKey>(private val elementSerializer: KSerializer<T>) :
    KSerializer<NavBackStack<T>> {

    private val delegate = SnapshotStateListSerializer(elementSerializer)

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        SerialDescriptor("androidx.navigation3.runtime.NavBackStack", delegate.descriptor)

    override fun serialize(encoder: Encoder, value: NavBackStack<T>) {
        encoder.encodeSerializableValue(serializer = delegate, value = value.base)
    }

    override fun deserialize(decoder: Decoder): NavBackStack<T> {
        return NavBackStack(base = decoder.decodeSerializableValue(deserializer = delegate))
    }
}

/**
 * Creates a [NavBackStackSerializer] for a polymorphic [NavKey] base type.
 *
 * This factory function is a convenience for creating a serializer for a [NavBackStack] whose
 * elements are polymorphic (e.g., different implementations of a `NavKey` interface).
 *
 * It retrieves a base serializer for the element type [T] (which is typically the base [NavKey]
 * interface).
 *
 * **Important:** For polymorphic serialization to work, you **must** provide a [SerializersModule]
 * (containing all concrete [NavKey] subtypes) to your `Encoder`/`Decoder` (e.g., via
 * [rememberSerializable]).
 *
 * `kotlinx.serialization`'s polymorphic dispatch relies on the module available during the
 * encoding/decoding process, not on the specific serializer retrieved by this function.
 *
 * @sample androidx.navigation3.runtime.samples.NavBackStackSerializer_withSerializersModule
 * @param T The reified element type, typically the base [NavKey] interface.
 * @return A new [NavBackStackSerializer] configured for the base polymorphic type.
 */
public inline fun <reified T : NavKey> NavBackStackSerializer(): NavBackStackSerializer<T> {
    return NavBackStackSerializer(elementSerializer = serializer<T>())
}
