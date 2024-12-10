#!/bin/bash

# 退出时显示错误
set -e

# 获取当前目录下所有子目录的路径
for dir in */; do
  if [ -f "$dir/deploy.sh" ]; then
    echo "Executing deploy.sh in $dir"
    (cd "$dir" && ./deploy.sh)
  fi
done

echo "All deploy scripts executed successfully!"