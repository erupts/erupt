const path = require("path");

module.exports = {
  lintOnSave: false,
  outputDir: "dist",
  // 开发环境显示报错位置 生产环境设置为false减少打包体积
  productionSourceMap: true,
  // 资源文件路径的前缀，/=从项目根目录开始 ./从当前页面的相对目录开始
  publicPath: "./",
  devServer: {
    port: process.env.port,
    disableHostCheck: true,
    /*overlay: {
      warning: false,
      errors: false
    }*/
    //代理后台地址
    proxy: {
      // detail: https://cli.vuejs.org/config/#devserver-proxy
      [process.env.VUE_APP_BASE_API]: {
        target: process.env.baseUrl,
        ws: false,
        secure: false, // 如果是https接口，需要配置这个参数
        changeOrigin: true,
        pathRewrite: {
          ['^' + process.env.VUE_APP_BASE_API]: ''
        }
      },
    },
  },
  pluginOptions: {
    "style-resources-loader": {
      preProcessor: "less",
      patterns: [
        // 全局变量路径，不能使用路径别名
        path.resolve(__dirname, "./src/assets/theme.less"),
      ],
    },
  }
}
