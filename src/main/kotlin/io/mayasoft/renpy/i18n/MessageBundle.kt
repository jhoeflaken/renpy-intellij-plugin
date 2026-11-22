package io.mayasoft.renpy.i18n

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

private const val BUNDLE = "messages.MessageBundle"

/**
 * Provides localized messages for the Ren'Py plugin using IntelliJ Platform's [DynamicBundle] mechanism.
 */
internal object MessageBundle {
    private val instance = DynamicBundle(MessageBundle::class.java, BUNDLE)

    /**
     * Resolves a localized message by its [key] with optional formatting parameters.
     *
     * @param key the message key in the resource bundle
     * @param params optional parameters to format into the localized message template
     * @return the localized string
     */
    @JvmStatic
    fun message(key: @PropertyKey(resourceBundle = BUNDLE) String, vararg params: Any?): @Nls String {
        return instance.getMessage(key, *params)
    }

    /**
     * Resolves a localized message lazily as a [Supplier].
     *
     * Useful for UI components and actions where message resolution should occur on demand.
     *
     * @param key the message key in the resource bundle
     * @param params optional parameters to format into the localized message template
     * @return a [Supplier] producing the localized string
     */
    @JvmStatic
    fun lazyMessage(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any?): Supplier<@Nls String> {
        return instance.getLazyMessage(key, *params)
    }
}
