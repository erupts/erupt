// Copyright © 2020-2035 erupt.xyz All rights reserved.
// Author: YuePeng (erupts@126.com)
// Erupt Bi Default Functions


/**
 * 结果预览：and field = :code
 * @param field  字段名
 * @param code   维度编码
 * @returns {string}
 */
function and(field, code) {
    return eval(code) && ' and ' + field + '=:' + code
}

/**
 * 结果预览：and field like '%xxx%'
 * @param field  字段名
 * @param code   维度编码
 * @returns {string}
 */
function like(field, code) {
    return eval(code) && ' and ' + field + " like '%" + eval(code) + "%'"
}

/**
 * 结果预览：and field in (:code)
 * @param field  字段名
 * @param code   维度编码
 * @returns {string}
 */
function In(field, code) {
    var val = eval(code);
    return val && val.length > 0 && ' and ' + field + ' in (:' + code + ')' || null;
}

/**
 * 区间查询
 * 结果预览：and field >= a and field <= b
 * @param field  字段名
 * @param code   维度编码
 * @returns {string}
 */
function range(field, code) {
    var val = eval(code);
    return val && val.length && " and " + field + " >= '" + val[0] + "' and " + field + " <= '" + val[1] + "'"
}

