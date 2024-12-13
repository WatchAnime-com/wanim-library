package com.wanim_ms.wanimlibrary

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.*
import org.springframework.context.event.EventListener
import java.net.http.HttpClient


@Configuration
@ComponentScans(
    ComponentScan(COMPONENT_SCAN),
)
@EnableAspectJAutoProxy
@ConfigurationPropertiesScan(CONFIGURATION_PROPERTIES_SCAN)
class WanimServiceConfig {
    @Autowired
    private lateinit var context: ApplicationContext

    private val log = LoggerFactory.getLogger(EnableWanimService::class.java)

    @PostConstruct
    fun init() {
        AppContextUtil.initialize(context)
    }

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady() {
        log.info("WanimService Utils initialized with <3")
    }


    @Bean
    fun http(): HttpClient {
        return HttpClient.newHttpClient()
    }
}

private const val COMPONENT_SCAN = "com.wanim_ms.wanimlibrary"
private const val CONFIGURATION_PROPERTIES_SCAN = "com.wanim_ms.wanimlibrary"