package com.wanim_ms.wanimlibrary

import org.springframework.context.ApplicationContext
import org.slf4j.LoggerFactory

class AppContextUtil(context: ApplicationContext) {
    private var context: ApplicationContext? = context
    private val log = LoggerFactory.getLogger(EnableWanimService::class.java)

    /**
     * Logs a message indicating that the AppContextUtil has been initialized.
     */
    init {
        log.info("Wanimservice Utils AppContextUtil initialized.")
    }

    companion object {
        var instance: AppContextUtil? = null

        /**
         * Initializes the AppContextUtil with an instance of ApplicationContext.
         *
         * @param context The ApplicationContext instance to initialize the AppContextUtil with.
         */
        fun initialize(context: ApplicationContext) {
            instance = AppContextUtil(context)
        }

        /**
         * Retrieves a bean from the application context by its class.
         *
         * @param clazz The class of the bean to retrieve.
         * @return The bean instance.
         * @throws RuntimeException if the bean could not be retrieved.
         */
        fun <T> bean(clazz: Class<T>): T {
            return instance?.context?.getBean(clazz) ?: throw RuntimeException("Could not get bean (${clazz.name})")
        }

        /**
         * Retrieves a bean from the application context by its name and class.
         *
         * @param name The name of the bean to retrieve.
         * @param clazz The class of the bean to retrieve.
         * @return The bean instance.
         * @throws RuntimeException if the bean could not be retrieved.
         */
        fun <T> bean(name: String, clazz: Class<T>): T {
            return instance?.context?.getBean(name, clazz) ?: throw RuntimeException("Could not get bean (${clazz.name})")
        }

        /**
         * Retrieves a bean from the application context by its name.
         *
         * @param name The name of the bean to retrieve.
         * @return The bean instance.
         * @throws RuntimeException if the bean could not be retrieved.
         */
        @Suppress("UNCHECKED_CAST")
        fun <T> bean(name: String): T {
            return instance?.context?.getBean(name) as T ?: throw RuntimeException("Could not get bean ($name)")
        }

        /**
         * Retrieves all beans of a given class from the application context.
         *
         * @param clazz The class of the beans to retrieve.
         * @return A map of bean names to bean instances.
         */
        fun <T> beans(clazz: Class<T>): Map<String, T> {
            return instance?.context?.getBeansOfType(clazz) ?: emptyMap()
        }
    }
}