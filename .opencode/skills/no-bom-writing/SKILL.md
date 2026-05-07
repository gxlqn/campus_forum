---
name: no-bom-writing
description: 规范文件写入，避免保存 UTF-8 BOM。适用于所有需要写入文本文件的场景，确保跨平台兼容性。
---

# No BOM Writing Skill

## 原则
所有写入的文本文件必须使用 **无 BOM 的 UTF-8 编码**。UTF-8 BOM（字节顺序标记，EF BB BF）在 Windows 上常见，但在跨平台环境中可能导致问题（如编译器警告、JSON 解析错误、脚本执行问题等）。

## 写入时避免 BOM 的方法

### 通用建议
- 明确指定编码为 UTF-8 且无 BOM。
- 避免使用可能默认添加 BOM 的工具或编码选项。

### PowerShell (Windows)
在 Windows PowerShell 5.1 中，`Set-Content -Encoding UTF8` 会添加 BOM。应使用以下方法之一：

**方法一：使用 .NET 类（推荐）**
```powershell
$content = "文件内容"
[System.IO.File]::WriteAllText("路径", $content)
# WriteAllText 默认使用 UTF-8 无 BOM
```

**方法二：使用 StreamWriter**
```powershell
$stream = [System.IO.StreamWriter]::new("路径", $false, [System.Text.Encoding]::UTF8)
$stream.Write("文件内容")
$stream.Close()
```

**方法三：PowerShell Core (v6+)**
在 PowerShell Core 中，`Set-Content -Encoding utf8` 默认无 BOM（但 PowerShell 5.1 不同）。如果环境是 PowerShell Core，可以使用：
```powershell
Set-Content -Path "路径" -Value "文件内容" -Encoding utf8
```

注意：在 Windows PowerShell 5.1 中，可用的编码选项中，`UTF8NoBOM` 可能不可用。因此推荐使用 .NET 方法。

### Python
Python 的 `open` 函数默认使用 UTF-8 无 BOM：
```python
with open('路径', 'w', encoding='utf-8') as f:
    f.write('内容')
```

### Node.js
使用 `fs.writeFile` 并指定 utf8 编码：
```javascript
const fs = require('fs');
fs.writeFile('路径', '内容', 'utf8', (err) => {});
// 或同步版本
fs.writeFileSync('路径', '内容', 'utf8');
```

### 其他工具
- **VS Code**：默认保存为 UTF-8 无 BOM。可在设置中确认：`"files.encoding": "utf8"`。
- **Git**：确保 `.gitattributes` 中设置 `* text=auto` 和适当的编码处理。

## 检查文件是否有 BOM
使用 PowerShell 检查文件前三个字节是否为 `EF BB BF`：
```powershell
Get-Content -Path "文件路径" -Encoding Byte -TotalCount 3 | ForEach-Object { $_.ToString('X2') }
```
如果输出为 `EF BB BF`，则有 BOM。

## 去除已有文件的 BOM
如果发现文件有 BOM，可使用以下方法去除：

**PowerShell (.NET 方法)**
```powershell
$content = Get-Content "文件路径" -Raw -Encoding UTF8
[System.IO.File]::WriteAllText("文件路径", $content)
```

**注意**：此方法会读取文件内容（自动忽略 BOM）然后重新写入无 BOM 的 UTF-8。

## 适用范围
此 skill 适用于项目中的所有文本文件，包括但不限于：
- 源代码文件（.java, .vue, .js, .ts, .wxml, .wxss 等）
- 配置文件（.json, .yml, .xml 等）
- 文档文件（.md, .txt 等）

遵循此 skill 可确保文件在不同平台和工具间保持一致，避免编码问题。
