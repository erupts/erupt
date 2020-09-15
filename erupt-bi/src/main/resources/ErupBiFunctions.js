/**
 *
 * @param field 字段名
 * @param key   参数名
 * @returns {string}
 */
function and(field, key) {
    return eval(key) && ' and ' + field + '=:' + key
}

/**
 *
 * @param field 字段名
 * @param key   参数名
 * @returns {string}
 */
function like(field, key) {
    return eval(key) && ' and ' + field + ' like concat("%",:' + key + ',"%")'
}