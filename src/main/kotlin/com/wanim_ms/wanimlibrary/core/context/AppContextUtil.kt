package com.wanim_ms.wanimlibrary.core.context

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext

/**
 * Utility class for accessing Spring ApplicationContext beans.
 * Provides static access to beans throughout the application.
 */
class AppContextUtil private constructor(private val context: ApplicationContext) {
    
    private val log = LoggerFactory.getLogger(AppContextUtil::class.java)

    init {
        log.info("WanimLibrary AppContextUtil initialized")
    }

    companion object {
        @Volatile
        private var instance: AppContextUtil? = null

        /**
         * Initialize the AppContextUtil with the ApplicationContext.
         * Should be called once during application startup.
         */
        fun initialize(context: ApplicationContext) {
            synchronized(this) {
                if (instance == null) {
                    instance = AppContextUtil(context)
                }
            }
        }

        /**
         * Get a bean by its class type.
         */
        fun <T> bean(clazz: Class<T>): T {
            return instance?.context?.getBean(clazz) 
                ?: throw IllegalStateException("AppContextUtil not initialized. Bean: ${clazz.name}")
        }

        /**
         * Get a bean by name and class type.
         */
        fun <T> bean(name: String, clazz: Class<T>): T {
            return instance?.context?.getBean(name, clazz) 
                ?: throw IllegalStateException("AppContextUtil not initialized. Bean: $name")
        }

        /**
         * Get a bean by name.
         */
        @Suppress("UNCHECKED_CAST")
        fun <T> bean(name: String): T {
            return (instance?.context?.getBean(name) as? T)
                ?: throw IllegalStateException("AppContextUtil not initialized. Bean: $name")
        }

        /**
         * Get all beans of a given type.
         */
        fun <T> beans(clazz: Class<T>): Map<String, T> {
            return instance?.context?.getBeansOfType(clazz) ?: emptyMap()
        }

        /**
         * Check if a bean exists.
         */
        fun containsBean(name: String): Boolean {
            return instance?.context?.containsBean(name) ?: false
        }
    }
}
