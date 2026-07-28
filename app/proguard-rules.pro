# 保留源文件名与行号，线上崩溃栈才能还原；同时抹掉原始文件名避免泄漏目录结构
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---------------- kotlinx.serialization ----------------
# 多态与默认值在运行时依赖注解信息
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# @Serializable 类的 Companion 字段
-if @kotlinx.serialization.Serializable class *
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Companion 上的 serializer() 工厂方法
-if @kotlinx.serialization.Serializable class * {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# @Serializable object 的 INSTANCE 与 serializer()，覆盖 Route 里的 data object 导航路由
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# ---------------- Retrofit 请求/响应模型 ----------------
# suspend 方法的真实返回类型在 JVM 层被擦除为 Object，只存在于 Continuation 的泛型签名里，
# Retrofit 运行时反射解析它来挑选转换器。若调用方丢弃返回值（如 addToBlacklist），
# 字节码里不会留下任何引用，R8 会把整个 DTO 裁掉，运行时抛 NoClassDefFoundError。
# 这类引用 R8 静态分析不可见，只能整包保留
-keep class com.example.nhviewer.data.remote.dto.** { *; }

# ---------------- security-crypto (Google Tink) ----------------
# Tink 仅在编译期用到 error_prone 注解做静态检查，运行时不存在也不影响功能
-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn com.google.errorprone.annotations.CheckReturnValue
-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn com.google.errorprone.annotations.RestrictedApi
