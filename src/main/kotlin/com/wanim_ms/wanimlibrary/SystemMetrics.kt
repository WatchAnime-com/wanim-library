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
                val scriptPath = "/home/cri/getDiskInfo.sh"
                File(scriptPath).apply {
                    writeText(scriptContent)
                    try {
                        val perms = PosixFilePermissions.fromString("rwxr-xr-x")
                        Files.setPosixFilePermissions(Paths.get(scriptPath), perms)
                    } catch (e: UnsupportedOperationException) {
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
            // CPU Metrics
            val cpuMetrics = getCPUMetrics()

            // RAM Metrics
            val ramMetrics = getRAMMetrics()

            // Disk Metrics
            val diskMetrics = getDiskMetrics()

            return SystemMetrics(
                timestamp = LocalDateTime.now(),
                userLogin = System.getProperty("user.name"),
                cpuMetrics = cpuMetrics,
                ramMetrics = ramMetrics,
                diskMetrics = diskMetrics,
                serverInfo = ServerInfo(
                    serviceName = "VIDEO-NODE-SERVER",
                    instanceId = "archlinux:video-node-server:5353",
                    registrationStatus = 204
                )
            )
        }

        private fun getCPUMetrics(): CPUMetrics {
            var totalCPUTime: Long = 0
            var activeCPUTime: Long = 0

            try {
                BufferedReader(FileReader("/proc/stat")).use { reader ->
                    val line = reader.readLine()
                    if (line.startsWith("cpu")) {
                        val values = line.split("\\s+".toRegex())
                        val userTime = values[1].toLong()
                        val niceTime = values[2].toLong()
                        val systemTime = values[3].toLong()
                        val idleTime = values[4].toLong()

                        totalCPUTime = userTime + niceTime + systemTime + idleTime
                        activeCPUTime = totalCPUTime - idleTime
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return CPUMetrics(
                totalCPUTime = totalCPUTime,
                activeCPUTime = activeCPUTime,
                cpuUsagePercent = (activeCPUTime * 100.0) / totalCPUTime
            )
        }

        private fun getRAMMetrics(): RAMMetrics {
            var totalMem: Long = 0
            var freeMem: Long = 0

            try {
                BufferedReader(FileReader("/proc/meminfo")).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        when {
                            line!!.startsWith("MemTotal:") -> totalMem = line!!.replace("\\D+".toRegex(), "").toLong()
                            line!!.startsWith("MemAvailable:") -> freeMem = line!!.replace("\\D+".toRegex(), "").toLong()
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return RAMMetrics(
                totalRAM = totalMem / 1024,
                usedRAM = (totalMem - freeMem) / 1024,
                freeRAM = freeMem / 1024
            )
        }

        private fun getDiskMetrics(): DiskMetrics {
            try {
                // Docker içinde direkt "df" komutu çalıştırarak container'ın disk bilgilerini alıyoruz
                val process = ProcessBuilder("df", "-h", "/").start()
                process.waitFor()

                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    // Header satırını atlıyoruz
                    reader.readLine()

                    // Disk bilgilerini içeren satırı okuyoruz
                    val line = reader.readLine()
                    if (line != null) {
                        val parts = line.trim().split("\\s+".toRegex())
                        if (parts.size >= 6) {
                            return DiskMetrics(
                                diskPath = parts[0],          // Filesystem
                                totalSpace = parts[1],        // Size
                                usedSpace = parts[2],         // Used
                                availableSpace = parts[3],    // Available
                                usagePercentage = parts[4]    // Use%
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Hata durumunda varsayılan değerler
            return DiskMetrics(
                diskPath = "unknown",
                totalSpace = "N/A",
                usedSpace = "N/A",
                availableSpace = "N/A",
                usagePercentage = "N/A"
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

        fun getSystemStatus(): String {
            val metrics = createFromCurrentData()
            return formatSystemStatus(metrics)
        }
    }
}