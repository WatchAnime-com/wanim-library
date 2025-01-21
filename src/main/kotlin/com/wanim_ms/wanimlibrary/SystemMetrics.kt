package com.wanim_ms.wanimlibrary

import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermissions
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SystemMetrics(
    val timestamp: LocalDateTime,
    val userLogin: String,
    val cpuMetrics: CPUMetrics,
    val ramMetrics: RAMMetrics,
    val diskMetrics: DiskMetrics,
    val serverInfo: ServerInfo
) {
    data class CPUMetrics(
        val totalCPUTime: Long,
        val activeCPUTime: Long,
        val cpuUsagePercent: Double
    )

    data class RAMMetrics(
        val totalRAM: Long,  // MB cinsinden
        val usedRAM: Long,   // MB cinsinden
        val freeRAM: Long    // MB cinsinden
    )

    data class DiskMetrics(
        val diskPath: String,
        val totalSpace: String,
        val usedSpace: String,
        val availableSpace: String,
        val usagePercentage: String
    )

    data class ServerInfo(
        val serviceName: String,
        val instanceId: String,
        val registrationStatus: Int
    )

    companion object {
        // Shell script oluşturma fonksiyonu
        fun createShellScript() {
            val scriptContent = """
                #!/bin/bash
                
                # Current Date and Time
                echo "Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): $(date -u '+%Y-%m-%d %H:%M:%S')"
                echo "Current User's Login: ${'$'}USER"
                echo ""
                
                # Get disk information for /dev/mapper/root
                echo "Disk Information:"
                df -h /dev/mapper/root | grep "/dev/mapper/root" | 
                    awk '{
                        print "Disk: " $1;
                        print "Total Space: " $2;
                        print "Used Space: " $3;
                        print "Available Space: " $4;
                        print "Usage Percentage: " $5
                    }'
            """.trimIndent()

            try {
                // Script dosyasını oluştur
                val scriptPath = "/home/cri/getDiskInfo.sh"
                File(scriptPath).apply {
                    writeText(scriptContent)
                    // Linux'ta çalıştırma izni ver
                    try {
                        val perms = PosixFilePermissions.fromString("rwxr-xr-x")
                        Files.setPosixFilePermissions(Paths.get(scriptPath), perms)
                    } catch (e: UnsupportedOperationException) {
                        // Windows sistemlerde çalışırsa bu kısmı atla
                        println("POSIX file permissions not supported on this system")
                    }
                }
                println("Shell script created successfully at: $scriptPath")
            } catch (e: Exception) {
                println("Error creating shell script: ${e.message}")
                e.printStackTrace()
            }
        }

        fun createFromCurrentData(): SystemMetrics {
            return SystemMetrics(
                timestamp = LocalDateTime.parse(
                    "2025-01-20 18:17:24",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                ),
                userLogin = "Cr-i",
                cpuMetrics = CPUMetrics(
                    totalCPUTime = 47524229,
                    activeCPUTime = 4156699,
                    cpuUsagePercent = 8.746483819863759
                ),
                ramMetrics = RAMMetrics(
                    totalRAM = 15803,
                    usedRAM = 13338,
                    freeRAM = 2464
                ),
                diskMetrics = DiskMetrics(
                    diskPath = "/dev/mapper/root",
                    totalSpace = "468G",
                    usedSpace = "50G",
                    availableSpace = "394G",
                    usagePercentage = "12%"
                ),
                serverInfo = ServerInfo(
                    serviceName = "VIDEO-NODE-SERVER",
                    instanceId = "archlinux:video-node-server:5353",
                    registrationStatus = 204
                )
            )
        }

        fun formatSystemStatus(status: SystemMetrics): String {
            return buildString {
                append("Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): ${status.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}\n")
                append("Current User's Login: ${status.userLogin}\n\n")

                append("CPU Metrics:\n")
                append("Total CPU Time: ${status.cpuMetrics.totalCPUTime}\n")
                append("Active CPU Time: ${status.cpuMetrics.activeCPUTime}\n")
                append("CPU Usage: ${status.cpuMetrics.cpuUsagePercent}%\n\n")

                append("RAM Metrics:\n")
                append("Total RAM: ${status.ramMetrics.totalRAM} MB\n")
                append("Used RAM: ${status.ramMetrics.usedRAM} MB\n")
                append("Free RAM: ${status.ramMetrics.freeRAM} MB\n\n")

                append("Disk Metrics:\n")
                append("Disk: ${status.diskMetrics.diskPath}\n")
                append("Total Space: ${status.diskMetrics.totalSpace}\n")
                append("Used Space: ${status.diskMetrics.usedSpace}\n")
                append("Available Space: ${status.diskMetrics.availableSpace}\n")
                append("Usage Percentage: ${status.diskMetrics.usagePercentage}\n\n")

                append("Server Info:\n")
                append("Service: ${status.serverInfo.serviceName}\n")
                append("Instance ID: ${status.serverInfo.instanceId}\n")
                append("Registration Status: ${status.serverInfo.registrationStatus}")
            }
        }
    }
}