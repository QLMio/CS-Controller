package io.github.xsheeee.cs_controller.tools

import android.content.Context
import com.topjohnwu.superuser.Shell
import io.github.xsheeee.cs_controller.tools.Logger.writeLog
import io.github.xsheeee.cs_controller.tools.Logger.showToast
import io.github.xsheeee.cs_controller.tools.Values.processName
import org.json.JSONObject

class Tools(private val context: Context) {
    fun getModeName(mode: Int): String? {
        return MODE_MAP[mode]
    }

    fun changeMode(modeName: String?) {
        if (modeName.isNullOrEmpty()) {
            showToast(context,"模式名称不能为空")
            return
        }

        val mode = getModeByName(modeName)
        if (mode != null) {
            writeToFile(CS_CONFIG_PATH, modeName)
        } else {
            showToast(context,"无效的模式名称：$modeName")
        }
    }

    private fun getModeByName(modeName: String): Int? {
        for ((key, value) in MODE_MAP) {
            if (value == modeName) {
                return key
            }
        }
        return null
    }

    fun readFileWithShell(filePath: String): String? {
        val result = Shell.cmd("cat $filePath").exec()
        if (result.isSuccess && result.out.isNotEmpty()) {
            return java.lang.String.join("\n", result.out)
        } else {
            logError("读取文件失败：$filePath", result)
            return null
        }
    }

    fun updateConfigEntry(filePath: String, key: String, newValue: String) {
        val content = readFileWithShell(filePath)
        if (content == null) {
            showToast(context,"无法读取配置文件：$filePath")
            return
        }

        try {
            // 解析为JSON
            val jsonObject = JSONObject(content)
            
            // 更新JSON中的值
            val booleanValue = when (newValue.lowercase()) {
                "true" -> true
                "false" -> false
                else -> {
                    // 如果不是布尔值，保持为字符串
                    jsonObject.put(key, newValue)
                    writeJsonToFile(filePath, jsonObject)
                    return
                }
            }
            
            jsonObject.put(key, booleanValue)
            writeJsonToFile(filePath, jsonObject)
            
        } catch (e: Exception) {
            // JSON解析失败，直接创建新的JSON配置
            createNewJsonConfig(filePath, key, newValue)
        }
    }
    
    private fun writeJsonToFile(filePath: String, jsonObject: JSONObject) {
        val jsonString = jsonObject.toString(2) // 使用2个空格缩进
        writeToFile(filePath, jsonString)
    }
    
    private fun createNewJsonConfig(filePath: String, key: String, newValue: String) {
        try {
            val jsonObject = JSONObject()
            val booleanValue = when (newValue.lowercase()) {
                "true" -> true
                "false" -> false
                else -> {
                    jsonObject.put(key, newValue)
                    writeJsonToFile(filePath, jsonObject)
                    return
                }
            }
            jsonObject.put(key, booleanValue)
            writeJsonToFile(filePath, jsonObject)
        } catch (e: Exception) {
            showToast(context,"创建JSON配置失败: ${e.message}")
        }
    }

    private fun writeToFile(filePath: String, content: String) {
        if (!executeShellCommand("echo \"" + content.replace("\"", "\\\"") + "\" > " + filePath)) {
            showToast(context,"写入失败：$filePath")
        }
    }

    val versionFromModuleProp: String?
        get() {
            val filePath = Values.csmodulePath
            val result = Shell.cmd("grep version= $filePath").exec()

            if (result.isSuccess && result.out.isNotEmpty()) {
                return result.out[0].replace("version=", "").trim { it <= ' ' }
            } else {
                logError("读取 module.prop 失败：$filePath", result)
                return null
            }
        }

    fun isProcessRunning(): Boolean {
        val result = Shell.cmd("pgrep -f $processName").exec()
        return result.isSuccess && result.out.isNotEmpty()
    }

    private fun executeShellCommand(command: String): Boolean {
        val result = Shell.cmd(command).exec()
        if (!result.isSuccess) {
            logError("Shell 命令执行失败：$command", result)
        }
        return result.isSuccess
    }

//    private fun showToast(message: String) {
//        showToast(context, message)
//    }

    private fun logError(message: String, result: Shell.Result) {
        writeLog("ERROR", TAG, message + " | Error: " + java.lang.String.join("\n", result.err))
    }

    companion object {
        private val MODE_MAP: MutableMap<Int, String> = HashMap()
        private const val TAG = "Tools"
        private const val CS_CONFIG_PATH = Values.CSConfigPath

        init {
            MODE_MAP[1] = Values.powersaveName
            MODE_MAP[2] = Values.balanceName
            MODE_MAP[3] = Values.performanceName
            MODE_MAP[4] = Values.fastName
        }
    }
}