package io.github.xsheeee.cs_controller.tools

object Values {
    const val processName: String = "CTS"  // 新版CTS可能使用不同的进程名，暂时保留原值
    const val CSConfigPath: String = "/sdcard/Android/CTS/mode.txt"  // 情景模式文件
    const val CSCPath: String = "/sdcard/Android/CSController/"
    const val csmodulePath: String = "/data/adb/modules/MW_CpuTurboScheduler/module.prop"
    const val csLog: String = "/sdcard/Android/CTS/log.txt"  // 日志文件
    const val csSettingsPath: String =
        "/sdcard/Android/CTS/config.json"  // JSON配置文件
    const val appConfig: String = "/storage/emulated/0/Android/CSController/app_config.json"
    const val CsServicePath: String = "/data/adb/modules/MW_CpuTurboScheduler/service.sh"
    const val balanceName: String = "balance"
    const val powersaveName: String = "powersave"
    const val performanceName: String = "performance"
    const val fastName: String = "fast"

}