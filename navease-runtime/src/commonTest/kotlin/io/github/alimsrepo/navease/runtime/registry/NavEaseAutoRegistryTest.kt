package io.github.alimsrepo.navease.runtime.registry

import androidx.compose.runtime.Composable
import io.github.alimsrepo.navease.internal.runtime.NavKey
import io.github.alimsrepo.navease.runtime.NavEaseRoot
import io.github.alimsrepo.navease.runtime.navigation.NavEaseController
import io.github.alimsrepo.navease.runtime.screen.ActivityScreen
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

/**
 * The registry is populated only by generated code, so these tests stand in for that code and
 * cover the behaviour hosts depend on.
 */
class NavEaseAutoRegistryTest {

    private sealed class AppScreens : NavEaseRoot {
        data object Home : AppScreens()
        data object Detail : AppScreens()
    }

    private sealed class WizardStep : NavEaseRoot {
        data object Pick : WizardStep()
    }

    private class StubScreen<K : NavKey> : ActivityScreen<K>() {
        @Composable
        override fun Content(navKey: K, navEaseController: NavEaseController) = Unit
    }

    private class StubSerializer<T : Any>(name: String, private val value: T) : KSerializer<T> {
        override val descriptor = buildClassSerialDescriptor(name)
        override fun serialize(encoder: Encoder, value: T) {
            encoder.beginStructure(descriptor).endStructure(descriptor)
        }

        override fun deserialize(decoder: Decoder): T {
            decoder.beginStructure(descriptor).endStructure(descriptor)
            return value
        }
    }

    private var screensCreated = 0

    private fun registerHome() = NavEaseAutoRegistry.addEntry(
        AppScreens.Home::class,
        AppScreens::class,
        StubSerializer("Home", AppScreens.Home),
    ) { screensCreated++; StubScreen() }

    private fun registerDetail() = NavEaseAutoRegistry.addEntry(
        AppScreens.Detail::class,
        AppScreens::class,
        StubSerializer("Detail", AppScreens.Detail),
    ) { StubScreen() }

    private fun registerPick() = NavEaseAutoRegistry.addEntry(
        WizardStep.Pick::class,
        WizardStep::class,
        StubSerializer("Pick", WizardStep.Pick),
    ) { StubScreen() }

    @BeforeTest
    fun setUp() {
        NavEaseAutoRegistry.clear()
        screensCreated = 0
    }

    @AfterTest
    fun tearDown() = NavEaseAutoRegistry.clear()

    @Test
    fun registers_screens_for_a_root() {
        registerHome()
        registerDetail()

        val screens = NavEaseAutoRegistry.screensForRoot(AppScreens::class)

        assertEquals(2, screens.size)
        assertTrue(AppScreens.Home::class in screens.keys)
        assertTrue(AppScreens.Detail::class in screens.keys)
    }

    @Test
    fun filters_by_root_so_nested_graphs_stay_independent() {
        registerHome()
        registerPick()

        assertEquals(1, NavEaseAutoRegistry.screensForRoot(AppScreens::class).size)
        assertEquals(1, NavEaseAutoRegistry.screensForRoot(WizardStep::class).size)
    }

    @Test
    fun bootstrapping_twice_does_not_duplicate_entries() {
        // Several hosts, or several modules, may each call navEaseBootstrap().
        registerHome()
        registerHome()

        assertEquals(1, NavEaseAutoRegistry.screensForRoot(AppScreens::class).size)
    }

    @Test
    fun each_host_gets_its_own_screen_instances() {
        // One shared instance per screen used to be created at bootstrap, so two hosts of the
        // same root shared screen objects and any state they held.
        registerHome()

        val first = NavEaseAutoRegistry.screensForRoot(AppScreens::class).values.single()
        val second = NavEaseAutoRegistry.screensForRoot(AppScreens::class).values.single()

        assertNotSame(first, second)
        assertEquals(2, screensCreated)
    }

    @Test
    fun screens_are_not_constructed_until_a_host_asks() {
        registerHome()

        assertEquals(0, screensCreated)
    }

    @Test
    fun serializers_are_returned_for_the_matching_root_only() {
        registerHome()
        registerPick()

        val serializers = NavEaseAutoRegistry.serializersForRoot(AppScreens::class)

        assertEquals(1, serializers.size)
        assertEquals(AppScreens.Home::class, serializers.single().first)
    }

    @Test
    fun an_empty_registry_reports_that_the_build_or_the_plugin_is_missing() {
        val error = assertFailsWith<IllegalStateException> {
            NavEaseAutoRegistry.screensForRoot(AppScreens::class)
        }

        assertTrue(error.message.orEmpty().contains("registry is empty"))
    }

    @Test
    fun an_unknown_root_lists_the_roots_that_are_registered() {
        registerPick()

        val error = assertFailsWith<IllegalStateException> {
            NavEaseAutoRegistry.screensForRoot(AppScreens::class)
        }

        val message = error.message.orEmpty()
        assertTrue(message.contains("no @AutoRegister screens found"))
        assertTrue(message.contains("WizardStep"), "expected the known roots listed, got: $message")
    }

    @Test
    fun one_key_under_two_roots_is_rejected() {
        registerHome()

        assertFailsWith<IllegalStateException> {
            NavEaseAutoRegistry.addEntry(
                AppScreens.Home::class,
                WizardStep::class,
                StubSerializer("Home", AppScreens.Home),
            ) { StubScreen() }
        }
    }
}
