package com.moegirlviewer.util

import android.os.Bundle
import androidx.navigation.*
import com.moegirlviewer.util.RouteArguments.Companion.getRouteArgumentFields
import java.lang.reflect.Field

// 路由跳转来时会收到一个bundle，其中是路由的参数，这个方法将其转化为实体类实例
inline fun <reified T : RouteArguments> Bundle.toRouteArguments(): T {
  val entityRef = T::class.java
  val entityInstance = entityRef.newInstance()
  val argumentFields = entityRef.getRouteArgumentFields()
  val argumentNameMapToInPoolId = mutableMapOf<String, String>()

  argumentFields.forEach {
    it.isAccessible = true
    val argumentKey = this.get(it.name) as String
    val value = RouteArgumentsPool.get(argumentKey)
    it.set(entityInstance, value)
    argumentNameMapToInPoolId[it.name] = argumentKey
  }

  val poolIdsField = RouteArguments::class.java.getDeclaredField("argumentNameMapToInPoolId")
  poolIdsField.isAccessible = true
  poolIdsField.set(entityInstance, argumentNameMapToInPoolId.toMap())

  return entityInstance
}

abstract class RouteArguments {
  private var argumentNameMapToInPoolId = emptyMap<String, String>()
  private fun createArgumentQueryStr(): String {
    if (argumentNameMapToInPoolId.isEmpty()) {
      argumentNameMapToInPoolId = this::class.java.getRouteArgumentFields()
        .map {
          it.isAccessible = true
          val name = it.name
          val value = RouteArgumentsPool.putIn(it.get(this))

          name to value
        }
        .associate { it }
    }

    return argumentNameMapToInPoolId.entries.joinToString("&") { "${it.key}=${it.value}" }
  }

  fun createRouteNameWithArguments(): String {
    val routeName = this::class.java.declaredAnnotations.filterIsInstance<RouteName>()[0].name
    return "${routeName}?${createArgumentQueryStr()}"
  }

  // 必须在ViewModel.onCleared中调用，从参数池中清除数据，防止内存泄漏
  fun removeReferencesFromArgumentPool() {
    argumentNameMapToInPoolId.values.forEach { RouteArgumentsPool.delete(it) }
  }

  companion object {
    private fun Class<out RouteArguments>.checkRouteNameAnnotation() {
      if (!this.isAnnotationPresent(RouteName::class.java)) {
        error("RouteArguments的实现类必须包含@RouteName注解")
      }
    }

    // 从实体类中获取路由参数(之前这里是通过注解来标记路由参数的，后来发现有点多余)
    fun Class<out RouteArguments>.getRouteArgumentFields(): List<Field> {
      // 有父类的类declaredFields会包含一个名为$stable的属性，这里过滤掉
      return this.declaredFields.filter { it.name != "\$stable" }
    }

    // 将路由名后带上参数插值的标记
    val Class<out RouteArguments>.formattedRouteName: String get() {
      this.checkRouteNameAnnotation()
      val routeName = this.declaredAnnotations.filterIsInstance<RouteName>()[0].name
      val argumentFields = this.getRouteArgumentFields()
      val argStr = argumentFields.joinToString("&") { "${it.name}={${it.name}}" }
      return "$routeName?$argStr"
    }

    // 路由参数
    val Class<out RouteArguments>.formattedArguments: List<NamedNavArgument> get() {
      this.checkRouteNameAnnotation()
      val argumentFields = this.getRouteArgumentFields()
      return argumentFields.map { RouteArgumentConfig(it.name).toNavArgument() }
    }
  }
}

// 路由参数项配置
class RouteArgumentConfig(
  val name: String,
  val type: NavType<*> = NavType.StringType,
  val nullable: Boolean = false,
  /*
    关于!nullable时默认值为空字符串，不知道navArgument是什么个设计，nullable如果为true，defaultValue就必须设置，
    按常理来想应该是可空才应该设置默认值的。因为这里已经将NavType.StringType, nullable = false作为默认值了，
    所以defaultValue也之好设置默认值为空字符串了。
    另外要注意：除了NavType.StringType，其他类型的默认值都不能为null
  */
  val defaultValue: Any? = if (nullable) null else ""
) {
  fun toNavArgument() = navArgument(name) {
    this.type = this@RouteArgumentConfig.type
    this.nullable = this@RouteArgumentConfig.nullable
    this.defaultValue = this@RouteArgumentConfig.defaultValue
  }
}

// 标记路由名
@Target(AnnotationTarget.CLASS)
annotation class RouteName(val name: String)

// 路由参数池，用来保存要跨页面传输无法序列化的对象
object RouteArgumentsPool {
  private var incrementId = 0
  private val pool = mutableMapOf<String, Any?>()

  fun putIn(value: Any?): String {
    val id = (++incrementId).toString()
    pool[id] = value
    return id
  }

  fun get(id: String): Any? {
    return pool[id]
  }

  fun delete(id: String) {
    pool.remove(id)
  }
}

fun NavHostController.navigate(
  arguments: RouteArguments,
  builder: (NavOptionsBuilder.() -> Unit) = {}
) {
  this.navigate(arguments.createRouteNameWithArguments(), builder)
}

fun NavHostController.replace(
  route: String,
  builder: (NavOptionsBuilder.() -> Unit) = {}
) {
  val currentRouteId = this.currentBackStackEntry!!.id
  this.navigate(route, builder)
  this.backQueue.removeIf { it.id == currentRouteId }
}

fun NavHostController.replace(
  arguments: RouteArguments,
  builder: (NavOptionsBuilder.() -> Unit) = {}
) {
  val currentRouteId = this.currentBackStackEntry!!.id
  this.navigate(arguments, builder)
  this.backQueue.removeIf { it.id == currentRouteId }
}