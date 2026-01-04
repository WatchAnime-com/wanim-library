package com.wanim_ms.wanimlibrary.config

import com.wanim_ms.wanimlibrary.core.context.AppContextUtil
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.*
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener

/**
 * Main configuration class for WanimLibrary.
 * Auto-configures all library components when imported.
 */
@Configuration
@ComponentScan(basePackages = ["com.wanim_ms.wanimlibrary"])
class WanimLibraryConfig {
    
    @Autowired
    private lateinit var context: ApplicationContext

    private val log = LoggerFactory.getLogger(WanimLibraryConfig::class.java)

    @PostConstruct
    fun init() {
        AppContextUtil.initialize(context)
    }

    @EventListener(ContextRefreshedEvent::class)
    fun onContextRefreshed() {
        log.info("╔══════════════════════════════════════════╗")
        log.info("║     WanimLibrary initialized ❤️          ║")
        log.info("╚══════════════════════════════════════════╝")
    }
}
