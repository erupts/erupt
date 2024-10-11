import request from "@/api/request";

export const uploadUrl = "../erupt-api/erupt-flow/upload"

// 删除文件
export function deleteFile(param) {
    return request({
        url: '../erupt-api/erupt-flow/file',
        method: 'delete',
        params: param
    })
}