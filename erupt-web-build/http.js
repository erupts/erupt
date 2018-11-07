/**
 * Created by roc on 11/6/18.
 */

const http = require('http');
const fs = require('fs');
const url = require('url');
const console = require('console');
const path = require('path');
const mime = require('./mime');

let port = 8086;
let pathFolder = "dist";
//创建服务器
http.createServer(function (request, response) {
  //解析请求，包括文件名
  let pathname = url.parse(request.url).pathname;
  let realPath = path.join(pathFolder, pathname);
  //默认进入index.html
  if (pathname === "/") {
    realPath = realPath + "index.html";
  }
  let ext = path.extname(realPath); //扩展名
  //从文件系统中都去请求的文件内容
  fs.exists(realPath, function (exists) {
    if (!exists) {
      response.writeHead(404, {
        'Content-Type': 'text/plain'
      });
      response.write("This request URL " + pathname + " was not found on this server.");
      response.end();
    } else {
      fs.readFile(realPath, "binary", function (err, data) {
        if (err) {
          //HTTP 状态码 404 ： NOT FOUND
          //Content Type:text/plain
          response.writeHead(404, {'Content-Type': 'text/html'});
        }
        else {
          //HTTP 状态码 200 ： OK
          //Content Type:text/plain
          let contentType = mime.type[ext.substr(1)] || "text/plain";
          response.writeHead(200, {
            'Content-Type': contentType
          });
          //回写相应内容
          response.write(data.toString(), "binary");
        }
        //发送响应数据
        response.end();
      });
    }
  });
}).listen(port);

console.log('访问地址：http://127.0.0.1:' + port + '/');
