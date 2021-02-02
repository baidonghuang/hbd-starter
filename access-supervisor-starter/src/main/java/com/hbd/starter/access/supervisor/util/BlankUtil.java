package com.hbd.starter.access.supervisor.util;

import java.util.Collection;
import java.util.Map;

/**
 * 非空类型判断，工具类
 * 判断集合、数组、字符串、MAP
 * 2016-1-25
 * created by hbd
 */
@SuppressWarnings("rawtypes")
public class BlankUtil {


	/**
	 * 判断对象类型是否为空
	 * @param obj
	 * @return
	 */
	public static boolean isEmpty(Object obj) {
		if(obj == null ) {
			return true;
		} else {
			if(obj instanceof Collection) {
				return isEmpty((Collection)obj);
			} else if (obj instanceof Map) {
				return isEmpty((Map)obj);
			} else if(String.valueOf(obj).trim().equals("")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断对象类型是否不为空
	 * @param obj
	 * @return
	 */
	public static boolean isNotEmpty(Object obj) {
		return !isEmpty(obj);
	}
	
	/**
	 * 判断字符串类型是否为空
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str) {
		if(str == null || str.trim().equals("")) {
			return true;
		}
		return false;
	}

	/**
	 * 判断字符串类型是否不为空
	 * @param str
	 * @return
	 */
	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

	/**
	 * 判断容器类型是否为空
	 * @param list
	 * @return
	 */
	public static boolean isEmpty(Collection list) {
		if (list == null || list.isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * 判断容器类型是否不为空
	 * @param list
	 * @return
	 */
	public static boolean isNotEmpty(Collection list) {
		return !isEmpty(list);
	}

	/**
	 * 判断Map类型是否为空
	 * @param map
	 * @return
	 */
	public static boolean isEmpty(Map map) {
		if (map == null || map.isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * 判断容器类型是否不为空
	 * @param map
	 * @return
	 */
	public static boolean isNotEmpty(Map map) {
		return !isEmpty(map);
	}

	/**
	 * 判断数组类型是否为空
	 * @param array
	 * @return
	 */
	public static boolean isEmpty(Object[] array) {
		if (array == null || array.length == 0) {
			return true;
		}
		return false;
	}

	/**
	 * 判断数组类型是否不为空
	 * @param array
	 * @return
	 */
	public static boolean isNotEmpty(Object[] array) {
		return !isEmpty(array);
	}

}
