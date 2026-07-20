# NhViewer Internationalization (i18n) & Translation Contribution Guide
# NhViewer 国际化 (i18n) 与多语言翻译贡献指南

Welcome to the NhViewer translation and tag dictionary contribution guide! This project adopts a decoupled dual-repository architecture separating **App UI Language** and **Tag Translations Dictionary**. Any developer or community contributor can easily add support for a new language.

欢迎参与 NhViewer 的国际化翻译与 Tag 词典贡献！本项目采用了**软件 UI 界面语言**与**标签显示词典**解耦的双库架构，任何开发者或社区贡献者都可以零门槛添加新语言支持。

---

## Table of Contents / 目录
- [1. Architecture Overview / 架构设计概览](#1-architecture-overview--架构设计概览)
- [2. App UI Language Translation / 软件 UI 界面翻译贡献](#2-app-ui-language-translation--软件-ui-界面翻译贡献)
- [3. Tag Dictionary Translation / Tag 标签词典翻译贡献](#3-tag-dictionary-translation--tag-标签词典翻译贡献)
- [4. Build & Submission Workflow / 提交与验证流程](#4-build--submission-workflow--提交与验证流程)

---

## 1. Architecture Overview / 架构设计概览

NhViewer separates multi-language support into two independently controlled modules:
NhViewer 将多语言分为两个独立控制的部分：

| Module / 模块 | Scope / 作用范围 | Storage Path / 存储路径 | Note / 说明 |
|---|---|---|---|
| **App UI / 软件 UI 界面** | Buttons, menus, dialog titles, errors / 按钮、菜单、弹窗标题、错误提示 | `app/src/main/res/values-<ISO>/strings.xml` | Standard Android XML system / 标准 Android XML 资源系统 |
| **Tag Dictionary / Tag 标签词典** | Manga Tags (`sole female` -> `单女主`), Category names / 漫画 Tag、分类名 | `app/src/main/assets/tags/<ISO>/*.json` | Categorized JSON files / 分类划分的 JSON 资产文件目录 |

---

## 2. App UI Language Translation / 软件 UI 界面翻译贡献

To add a new UI language (e.g. Japanese `ja` or Korean `ko`): / 若想为 NhViewer 添加新的界面语言（例如日文 `ja` 或韩文 `ko`）：

1. **Copy Template File / 复制模版文件**：
   Copy the UI string template [values/strings_template.xml](file:///f:/dev/nhentai/app/src/main/res/values/strings_template.xml) to new directory `app/src/main/res/values-ja/strings.xml` or `values-ko/strings.xml`.
   复制 UI 字符串全量模版文件 [values/strings_template.xml](file:///f:/dev/nhentai/app/src/main/res/values/strings_template.xml) 到新目录 `app/src/main/res/values-ja/strings.xml` 或 `values-ko/strings.xml`。

2. **Fill in Translations / 填写翻译文本**：
   Keep string key names `<string name="xxx">` unchanged and fill in the translated text:
   保留标签键名 `<string name="xxx">` 不变，将内嵌文本翻译为目标语言，例如：
   ```xml
   <string name="common_ok">OK</string>
   <string name="common_cancel">キャンセル</string>
   ```

3. **Register Language Option / 注册界面选项**：
   - In [strings.xml](file:///f:/dev/nhentai/app/src/main/res/values/strings.xml), add the language display name:
     在 [strings.xml](file:///f:/dev/nhentai/app/src/main/res/values/strings.xml) 中添加语言名称：
     ```xml
     <string name="settings_app_language_ja">日本語</string>
     ```
   - In [LanguageManager.kt](file:///f:/dev/nhentai/app/src/main/java/com/example/nhviewer/util/i18n/LanguageManager.kt), add `Locale.JAPANESE` mapping in `createLocaleContext`:
     在 [LanguageManager.kt](file:///f:/dev/nhentai/app/src/main/java/com/example/nhviewer/util/i18n/LanguageManager.kt) 的 `createLocaleContext` 方法中追加对应 Locale 映射：
     ```kotlin
     "ja" -> Locale.JAPANESE
     ```
   - In [SettingsScreen.kt](file:///f:/dev/nhentai/app/src/main/java/com/example/nhviewer/presentation/feature/settings/SettingsScreen.kt), add `"ja" to stringResource(R.string.settings_app_language_ja)`.
     在 [SettingsScreen.kt](file:///f:/dev/nhentai/app/src/main/java/com/example/nhviewer/presentation/feature/settings/SettingsScreen.kt) 弹窗选项列表添加 `"ja" to stringResource(R.string.settings_app_language_ja)`。

---

## 3. Tag Dictionary Translation / Tag 标签词典翻译贡献

To add tag translations for a new language (e.g. Japanese `ja` or Korean `ko`): / 若想为标签库添加新语言翻译（例如日文 `ja` 或韩文 `ko`）：

1. **Copy Template Directory / 复制词典模版目录**：
   Copy full key template folder [assets/tags/template/](file:///f:/dev/nhentai/app/src/main/assets/tags/template/) to `app/src/main/assets/tags/ja/` or `app/src/main/assets/tags/ko/`.
   复制全量 Key 词典模版目录 [assets/tags/template/](file:///f:/dev/nhentai/app/src/main/assets/tags/template/) 到 `app/src/main/assets/tags/ja/` 或 `app/src/main/assets/tags/ko/`。

   The folder structure is categorized by tag types: / 词典包包含以下按类型划分的 JSON 文件：
   - `category.json` : Category tags / 分类标签
   - `language.json` : Language tags / 语言标签
   - `female.json` : Female attribute tags / 女性属性标签
   - `male.json` : Male attribute tags / 男性属性标签
   - `tag.json` : General tags / 通用标签

2. **Edit JSON Key-Values / 编辑 JSON 键值对**：
   JSON keys are nhentai original English tags (lowercase). Values are translations:
   JSON 中的 Key 为 nhentai 原始英文标签（全小写），Value 为对应的翻译：
   ```json
   {
     "manga": "マンガ",
     "doujinshi": "同人誌",
     "sole female": "単体女子",
     "glasses": "メガネ"
   }
   ```
   > **Fallback Note / 降级说明**：Keep `""` for missing tag translations. The app automatically falls back to original English tag name without crashing or blank screen. / 冷门标签若暂无翻译，保持 `""` 留空即可。系统会自动降级显示原始英文标签，绝不会崩溃或显示空白。

3. **Automatic Engine Support / 系统自动支持**：
   NhViewer's `TagTranslationProvider` automatically scans and merges all JSON files inside `app/src/main/assets/tags/<lang>/` at runtime without extra code changes!
   NhViewer 的 `TagTranslationProvider` 会在运行时自动扫描并合并 `app/src/main/assets/tags/<lang>/` 目录下的所有分类 JSON 资产，无需编写任何代码！

---

## 4. Build & Submission Workflow / 提交与验证流程

1. **Run Local Build Verification / 运行本地构建验证**：
   Run Gradle build verification in the project root: / 在项目根目录下运行 Gradle 编译测试：
   ```bash
   ./gradlew assembleDebug
   ```
2. **Submit Pull Request / 提交 Pull Request**：
   Submit your new JSON files & `strings.xml` to the repository. We will review and merge them in the next release!
   将您修改或新增的 JSON 文件与 `strings.xml` 提交至仓库，我们会在审核后合并入下一个版本中。

Thank you for contributing to the NhViewer community!
感谢您为 NhViewer 社区做出的贡献！
