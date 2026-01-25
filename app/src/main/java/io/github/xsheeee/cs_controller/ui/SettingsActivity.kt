package io.github.xsheeee.cs_controller.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.materialswitch.MaterialSwitch
import io.github.xsheeee.cs_controller.R
import io.github.xsheeee.cs_controller.tools.Logger
import io.github.xsheeee.cs_controller.tools.SuManager
import io.github.xsheeee.cs_controller.tools.Values
import io.github.xsheeee.cs_controller.ui.adapter.SwitchAdapter
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Locale

class SettingsActivity : BaseActivity() {

    private val switchKeys: MutableList<String> = ArrayList()
    private val configMap: MutableMap<String, Boolean> = HashMap()
    private val keyTranslations: MutableMap<String, String> = HashMap()
    private val keyDisplayMap: MutableMap<String, String> = HashMap()
    private var adapter: SwitchAdapter? = null

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val uri = result.data!!.data
            uri?.let { importTranslationFile(it) }
        }
    }

    private fun initSwitchKeys() {
        switchKeys.clear()
        switchKeys.addAll(configMap.keys)
    }

    private fun setupPermissionSwitches() {
        val isAccessibilityEnabled = isAccessibilityServiceEnabled
        val switchAccessibility = findViewById<MaterialSwitch>(R.id.switchAccessibility)
        switchAccessibility.isChecked = isAccessibilityEnabled
        switchAccessibility.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) enableAccessibilityService() else disableAccessibilityService()
        }

        val appConfigPath = Values.appConfig
        val isFloatingWindowEnabled = readFloatingWindowConfig(appConfigPath)
        val switchFloatingWindow = findViewById<MaterialSwitch>(R.id.switchFloatingWindow)
        switchFloatingWindow.isChecked = isFloatingWindowEnabled
        switchFloatingWindow.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            updateFloatingWindowConfig(appConfigPath, isChecked)
        }
    }

    private fun readFloatingWindowConfig(configPath: String): Boolean {
        return try {
            val jsonContent = String(Files.readAllBytes(Paths.get(configPath)))
            val config = JSONObject(jsonContent)
            config.optBoolean("floatingWindow", false)
        } catch (e: Exception) {
            showToast("读取悬浮窗配置失败：" + e.message)
            false
        }
    }

    private fun updateFloatingWindowConfig(configPath: String, isEnabled: Boolean) {
        try {
            val jsonContent = String(Files.readAllBytes(Paths.get(configPath)))
            val config = JSONObject(jsonContent)
            config.put("floatingWindow", isEnabled)
            BufferedWriter(FileWriter(configPath)).use { writer ->
                writer.write(config.toString(2))
                writer.flush()
            }
        } catch (e: Exception) {
            showToast("更新悬浮窗配置失败：" + e.message)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<Toolbar>(R.id.backButton5)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.outline_arrow_back_24)
        toolbar.setNavigationOnClickListener { finish() }

        loadConfig()
        initSwitchKeys()

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SwitchAdapter(this, switchKeys, configMap, CONFIG_FILE_PATH)
        recyclerView.adapter = adapter

        val view = findViewById<View>(R.id.DeviceInfo)
        view.setOnClickListener {
            val intent = Intent(this, DeviceInfoActivity::class.java)
            startActivity(intent)
        }



        loadTranslationsFromPreferences()
        reorderKeys()

        setupPermissionSwitches()
    }

    private val isAccessibilityServiceEnabled: Boolean
        get() {
            val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            return enabledServices?.contains("io.github.xsheeee.cs_controller/io.github.xsheeee.cs_controller.MyAccessibilityService") == true
        }

    private fun enableAccessibilityService() {
        val componentName = "io.github.xsheeee.cs_controller/io.github.xsheeee.cs_controller.MyAccessibilityService"
        try {
            val existing = SuManager.exec("settings get secure enabled_accessibility_services").trim()
            val services = existing.split(":").filter { it.isNotEmpty() }.toMutableSet()
            services.add(componentName)
            val updatedList = services.joinToString(":")
            SuManager.exec("settings put secure enabled_accessibility_services \"$updatedList\"")
            SuManager.exec("settings put secure accessibility_enabled 1")
        } catch (e: Exception) {
            showToast("Root启用失败，尝试使用手动方式")
            try {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            } catch (e2: Exception) {
                showToast("无法打开无障碍设置：" + e2.message)
            }
        }
    }

    private fun disableAccessibilityService() {
        val componentName = "io.github.xsheeee.cs_controller/io.github.xsheeee.cs_controller.MyAccessibilityService"
        try {
            val existing = SuManager.exec("settings get secure enabled_accessibility_services").trim()
            val updatedList = existing.split(":")
                .filter { it.isNotEmpty() && it != componentName }
                .joinToString(":")
            SuManager.exec("settings put secure enabled_accessibility_services \"$updatedList\"")
            if (updatedList.isEmpty()) {
                SuManager.exec("settings put secure accessibility_enabled 0")
            }
        } catch (e: Exception) {
            showToast("禁用无障碍服务失败：" + e.message)
        }
    }



//    private fun executeRootCommand(command: String) {
//        try {
//            val process = Runtime.getRuntime().exec("su")
//            val writer = BufferedWriter(OutputStreamWriter(process.outputStream))
//            writer.write(command)
//            writer.flush()
//            writer.close()
//            process.waitFor()
//        } catch (e: Exception) {
//            showToast("ERROR" + e.message)
//        }
//    }

    private fun loadConfig() {
        configMap.clear()
        try {
            val file = File(CONFIG_FILE_PATH)
            if (!file.exists()) {
                // 如果文件不存在，创建默认的JSON配置
                createDefaultConfig()
                return
            }
            
            val jsonContent = file.readText()
            val jsonObject = JSONObject(jsonContent)
            
            // 遍历JSON对象的所有键
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = jsonObject.opt(key)
                
                // 只处理布尔值
                if (value is Boolean) {
                    configMap[key] = value
                } else if (value is String) {
                    // 尝试将字符串转换为布尔值
                    val lowerValue = value.lowercase(Locale.getDefault())
                    if (lowerValue == "true" || lowerValue == "false") {
                        configMap[key] = lowerValue.toBoolean()
                    }
                } else if (value is Int) {
                    // 将1/0转换为布尔值
                    configMap[key] = value == 1
                }
            }
        } catch (e: Exception) {
            Logger.showToast(this@SettingsActivity, getString(R.string.read_mode_error) + ": " + e.message)
            // JSON解析失败，不进行INI回退
        }
    }
    
    private fun createDefaultConfig() {
        try {
            val defaultConfig = JSONObject()
            // 添加一些默认的配置项
            defaultConfig.put("enable_logging", true)
            defaultConfig.put("auto_start", false)
            defaultConfig.put("show_notification", true)
            
            val file = File(CONFIG_FILE_PATH)
            file.parentFile?.mkdirs()
            file.writeText(defaultConfig.toString(2))
            
            // 将默认配置添加到configMap
            configMap["enable_logging"] = true
            configMap["auto_start"] = false
            configMap["show_notification"] = true
        } catch (e: Exception) {
            Logger.e("SettingsActivity", "创建默认配置失败: ${e.message}")
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.setType("text/plain")
        filePickerLauncher.launch(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings_topbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.import_translation) {
            openFilePicker()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun importTranslationFile(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            inputStream?.let {
                val reader = BufferedReader(InputStreamReader(it))
                var line: String
                while ((reader.readLine().also { line = it }) != null) {
                    line = line.trim()
                    if (line.contains("=")) {
                        val parts = line.split("=".toRegex(), limit = 2).toTypedArray()
                        val key = parts[0].trim()
                        val value = parts[1].trim()
                        keyTranslations[key] = value
                    }
                }
                it.close()
                saveTranslationsToPreferences()
                applyTranslations()
                showToast("翻译文件导入成功！")
            }
        } catch (e: Exception) {
            showToast("翻译文件导入失败：" + e.message)
        }
    }

    private fun applyTranslations() {
        for (key in switchKeys) {
            keyDisplayMap[key] = keyTranslations[key] ?: key
        }
        adapter?.setKeyDisplayMap(keyDisplayMap)
        adapter?.notifyDataSetChanged()
    }

    private fun loadTranslationsFromPreferences() {
        val sharedPreferences = getSharedPreferences("Translations", MODE_PRIVATE)
        switchKeys.forEach { key ->
            sharedPreferences.getString(key, null)?.let { translation ->
                keyTranslations[key] = translation
            }
        }
        applyTranslations()
    }

    private fun saveTranslationsToPreferences() {
        val sharedPreferences = getSharedPreferences("Translations", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        keyTranslations.forEach { (key, value) ->
            editor.putString(key, value)
        }
        editor.apply()
    }

    private fun reorderKeys() {
        val validKeys = switchKeys.filter { configMap.containsKey(it) }
        val invalidKeys = switchKeys.filterNot { configMap.containsKey(it) }

        switchKeys.clear()
        switchKeys.addAll(validKeys)
        switchKeys.addAll(invalidKeys)
    }

    private fun showToast(message: String) {
        Logger.showToast(this@SettingsActivity, message)
    }

    companion object {
        private const val CONFIG_FILE_PATH = Values.csSettingsPath
    }
}
