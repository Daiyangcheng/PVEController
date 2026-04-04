package cn.locyan.pvecontroller.shared

/**
 * 模板引擎，支持 ${variable} 格式的变量替换
 *
 * template example: ip=${ip},port=${port}
 * value example: {"ip":"1.1.1.1","port":"8888"}
 */
class TemplateEngine {

    /**
     * 渲染模板
     * @param template 模板字符串，包含 ${variable} 占位符
     * @param values JSON 格式的变量值映射（仅支持扁平键值对，值必须是字符串）
     * @return 渲染后的字符串
     */
    fun render(template: String, values: String): String {
        val valueMap = parseJsonToMap(values)
        return render(template, valueMap)
    }

    /**
     * 渲染模板
     * @param template 模板字符串，包含 ${variable} 占位符
     * @param valueMap 变量值映射
     * @return 渲染后的字符串
     */
    fun render(template: String, valueMap: Map<String, String>): String {
        val pattern = Regex("\\$\\{([^}]+)}")
        return pattern.replace(template) { matchResult ->
            val key = matchResult.groupValues[1].trim()
            valueMap[key] ?: throw TemplateException(
                "模板变量 '${key}' 未在 values 中找到。模板: $template, 可用变量: ${valueMap.keys}"
            )
        }
    }

    /**
     * 批量渲染多个模板
     */
    fun renderAll(templates: List<String>, values: String): List<String> {
        val valueMap = parseJsonToMap(values)
        return templates.map { render(it, valueMap) }
    }

    /**
     * 验证模板中的所有变量是否都有对应的值
     */
    fun validate(template: String, values: String): List<String> {
        val valueMap = parseJsonToMap(values)
        return validate(template, valueMap)
    }

    /**
     * 验证模板中的所有变量是否都有对应的值
     */
    fun validate(template: String, valueMap: Map<String, String>): List<String> {
        val pattern = Regex("\\$\\{([^}]+)}")
        val requiredVars = pattern.findAll(template)
            .map { it.groupValues[1].trim() }
            .toSet()
        return requiredVars.filter { !valueMap.containsKey(it) }
    }

    /**
     * 提取模板中的所有变量名
     */
    fun extractVariables(template: String): List<String> {
        val pattern = Regex("\\$\\{([^}]+)}")
        return pattern.findAll(template)
            .map { it.groupValues[1].trim() }
            .distinct()
            .toList()
    }

    // 简单的 JSON 解析器，支持 { "key1": "value1", "key2": "value2" }
    // 不处理嵌套、数字、布尔等，值只能是字符串（双引号包裹）
    private fun parseJsonToMap(json: String): Map<String, String> {
        val trimmed = json.trim()
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            throw TemplateException("无效的 JSON 格式，必须是以 { } 包裹的对象")
        }
        val content = trimmed.substring(1, trimmed.length - 1).trim()
        if (content.isEmpty()) return emptyMap()

        val result = mutableMapOf<String, String>()
        // 简单按逗号分割，但要注意字符串内的逗号不被分割
        val parts = splitJsonMembers(content)
        for (part in parts) {
            val colonIndex = part.indexOf(':')
            if (colonIndex == -1) continue
            val key = part.substring(0, colonIndex).trim()
            val value = part.substring(colonIndex + 1).trim()
            if (key.startsWith("\"") && key.endsWith("\"")) {
                val unquotedKey = key.substring(1, key.length - 1)
                val unquotedValue = if (value.startsWith("\"") && value.endsWith("\""))
                    value.substring(1, value.length - 1)
                else
                    value
                result[unquotedKey] = unquotedValue
            } else {
                // 允许不带引号的键（但通常 JSON 要求双引号，这里为了容错）
                val unquotedValue = if (value.startsWith("\"") && value.endsWith("\""))
                    value.substring(1, value.length - 1)
                else
                    value
                result[key] = unquotedValue
            }
        }
        return result
    }

    // 分割 JSON 成员，忽略字符串内部的逗号
    private fun splitJsonMembers(content: String): List<String> {
        val members = mutableListOf<String>()
        var inString = false
        var start = 0
        var i = 0
        while (i < content.length) {
            when (content[i]) {
                '"' -> {
                    // 简单处理转义引号
                    if (i == 0 || content[i - 1] != '\\') {
                        inString = !inString
                    }
                }
                ',' -> {
                    if (!inString) {
                        members.add(content.substring(start, i).trim())
                        start = i + 1
                    }
                }
            }
            i++
        }
        if (start < content.length) {
            members.add(content.substring(start).trim())
        }
        return members.filter { it.isNotEmpty() }
    }
}

class TemplateException(message: String) : RuntimeException(message)

// 扩展函数
fun String.renderWith(values: Map<String, String>): String {
    return TemplateEngine().render(this, values)
}

fun String.renderWith(values: String): String {
    return TemplateEngine().render(this, values)
}