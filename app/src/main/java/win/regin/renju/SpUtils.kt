package win.regin.renju

import android.content.Context
import androidx.core.content.edit

/**
 * @author :Reginer in  2018/4/26 20:51.
 *         联系方式:QQ:282921012
 *         功能描述:
 */
class SpUtils {
    init {
        throw UnsupportedOperationException("cannot be instantiated")
    }

    companion object {

        /**
         * 保存在手机里面的文件名
         */
        private const val FILE_NAME = "renju"

        /**
         * 保存数据的方法，需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
         *
         * @param key       key
         * @param content    object
         * @param fileName fileName
         */
        fun put(key: String, content: Any, fileName: String) {
            val sharedPreferences =
                    AppRenju.instance.getSharedPreferences(fileName, Context.MODE_PRIVATE)
            when (content) {
                is String -> sharedPreferences.edit { putString(key, content) }
                is Int -> sharedPreferences.edit { putInt(key, content) }
                is Boolean -> sharedPreferences.edit { putBoolean(key, content) }
                is Float -> sharedPreferences.edit { putFloat(key, content) }
                is Long -> sharedPreferences.edit { putLong(key, content) }
                else -> sharedPreferences.edit { putString(key, content.toString()) }
            }
        }

        /**
         * 保存数据的方法，需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
         *
         * @param key       key
         * @param content    object
         */
        fun put(key: String, content: Any?) {
            val sharedPreferences =
                    AppRenju.instance.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
            when (content) {
                is String -> sharedPreferences.edit { putString(key, content) }
                is Int -> sharedPreferences.edit { putInt(key, content) }
                is Boolean -> sharedPreferences.edit { putBoolean(key, content) }
                is Float -> sharedPreferences.edit { putFloat(key, content) }
                is Long -> sharedPreferences.edit { putLong(key, content) }
                else -> sharedPreferences.edit { putString(key, content.toString()) }
            }
        }


        /**
         * 得到保存数据的方法，根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
         *
         * @param key           key
         * @param defaultObject defaultObject
         * @param fileName     fileName
         */
        operator fun get(
            key: String,
            defaultObject: Any,
            fileName: String
        ): Any? {
            val sharedPreferences =
                    AppRenju.instance.getSharedPreferences(fileName, Context.MODE_PRIVATE)
            return when (defaultObject) {
                is String -> sharedPreferences.getString(key, defaultObject)
                is Int -> sharedPreferences.getInt(key, defaultObject)
                is Boolean -> sharedPreferences.getBoolean(key, defaultObject)
                is Float -> sharedPreferences.getFloat(key, defaultObject)
                is Long -> sharedPreferences.getLong(key, defaultObject)
                else -> null
            }
        }

        /**
         * 得到保存数据的方法，根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
         *
         * @param key           key
         * @param defaultObject defaultObject
         */
        operator fun get(key: String, defaultObject: Any): Any? {
            val sharedPreferences =
                    AppRenju.instance.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

            return when (defaultObject) {
                is String -> sharedPreferences.getString(key, defaultObject)
                is Int -> sharedPreferences.getInt(key, defaultObject)
                is Boolean -> sharedPreferences.getBoolean(key, defaultObject)
                is Float -> sharedPreferences.getFloat(key, defaultObject)
                is Long -> sharedPreferences.getLong(key, defaultObject)
                else -> null
            }
        }

        /**
         * 移除某个key值已经对应的值
         *
         * @param key       key
         * @param fileName fileName
         */
        fun remove(key: String, fileName: String) {
            val sharedPreferences =
                    AppRenju.instance.getSharedPreferences(fileName, Context.MODE_PRIVATE)
            sharedPreferences.edit { remove(key) }
        }

        /**
         * 移除某个key值已经对应的值
         *
         * @param key       key
         */
        fun remove(key: String) {
            val sharedPreferences =
                    AppRenju.instance.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
            sharedPreferences.edit { remove(key) }
        }

        /**
         * 清除所有数据
         *
         * @param fileName fileName
         */
        fun clear(fileName: String) {
            val sharedPreferences =
                    AppRenju.instance.getSharedPreferences(fileName, Context.MODE_PRIVATE)
            sharedPreferences.edit { clear() }
        }

        /**
         * 清除所有数据
         *
         */
        fun clear() {
            val sharedPreferences =
                    AppRenju.instance.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
            sharedPreferences.edit { clear() }
        }


        /**
         * 查询某个key是否已经存在
         *
         * @param key       key
         * @param fileName fileName
         */
        fun contains(key: String, fileName: String): Boolean {
            val sharedPreferences =
                    AppRenju.instance.getSharedPreferences(fileName, Context.MODE_PRIVATE)
            return sharedPreferences.contains(key)
        }

        /**
         * 查询某个key是否已经存在
         *
         * @param key       key
         */
        fun contains(key: String): Boolean {
            val sharedPreferences =
                    AppRenju.instance.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
            return sharedPreferences.contains(key)
        }


        /**
         * 返回所有的键值对
         *
         * @param fileName fileName
         * @return map
         */
        fun getAll(fileName: String): Map<String, *> {
            val sharedPreferences =
                AppRenju.instance.getSharedPreferences(fileName, Context.MODE_PRIVATE)
            return sharedPreferences.all
        }

        /**
         * 返回所有的键值对
         *
         * @return map
         */
        fun getAll(): Map<String, *> {
            val sharedPreferences =
                    AppRenju.instance.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
            return sharedPreferences.all
        }

    }
}