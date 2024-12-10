# 打包上传所有文件执行下方命令即可

```shell
bash deploy_all.sh
```

如果出现报错：deploy_all.sh: line 12: ./deploy.sh: Permission denied

执行下方命令后，重新执行 “bash deploy_all.sh” 即可 

```shell
find . -type f -name "deploy.sh" -exec chmod +x {} \;
```


__需要注意⚠️⚠️⚠️ 后续新增的sh命名需要是 "deploy.sh"__ 