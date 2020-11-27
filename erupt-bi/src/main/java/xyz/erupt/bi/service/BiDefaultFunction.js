/*
 * Copyright © 2020-2035 erupt.xyz All rights reserved.
 * Author: YuePeng (erupts@126.com)
 * EruptBi Default Functions
 */


/**
 * 结果预览：and field = :key
 * @param field 字段名
 * @param key   参数名
 * @returns {string}
 */
function and(field, key) {
    return eval(key) && ' and ' + field + '=:' + key
}

/**
 * 结果预览：and field like concat("%",:key,"%")'
 * @param field 字段名
 * @param key   参数名
 * @returns {string}
 */
function like(field, key) {
    return eval(key) && ' and ' + field + ' like concat("%",:' + key + ',"%")'
}

/**
 * 结果预览：and field in (:key)
 * @param field 字段名
 * @param key   参数名
 * @returns {string}
 */
function In(field, key) {
    var val = eval(key);
    return val && val.length > 0 && ' and ' + field + ' in (:' + key + ')' || null;
}