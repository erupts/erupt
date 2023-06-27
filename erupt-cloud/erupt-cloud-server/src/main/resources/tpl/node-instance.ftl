<html>
<head>
    <base href="${base}/">
    <meta charset="UTF-8">
    <link rel="stylesheet" href="ant-design/antd.min.css">
</head>
<body>
<div id="app">
    <template>
        <a-list bordered :data-source="instances" style="border-radius: 0;border:1px solid #f0f0f0;border-top: none;">
            <a-list-item slot="renderItem" slot-scope="item, index">
                <div style="display: flex;justify-content: space-between;align-items: center;width: 100%">
                    <span>{{item}}</span>
                    <span>
                        <a-popconfirm title="Are you remove Instance?" @confirm="confirm(item)">
                            <a-icon type="delete" theme="twoTone" two-tone-color="#f00"/>
                        </a-popconfirm>
                    </span>
                </div>
            </a-list-item>
        </a-list>
    </template>
</div>
<script src="ant-design/vue.min.js"></script>
<script src="ant-design/antd.min.js"></script>
<script src="ant-design/axios.min.js"></script>
<script>
    var vue = new Vue({
        el: '#app',
        data() {
            return {
                instances: ${instances},
            };
        },
        methods: {
            confirm(item) {
                let param = {};
                let paramsArr = location.search.substring(1).split('&')
                for (let i = 0, len = paramsArr.length; i < len; i++) {
                    let arr = paramsArr[i].split('=')
                    param[arr[0]] = arr[1];
                }
                axios.get("/erupt-cloud-api/remove-instance/${row.nodeName}?instance=" + item, {
                    headers: {
                        token: param["_token"]
                    }
                }).then(data => {
                    window.location.reload();
                }).catch((err) => {
                    if (err.response.status === 401) {
                        window.parent.location.reload();
                    }
                    if (err.response.status === 403) {
                        this.$message.error("permission denied");
                    } else {
                        this.$message.info(err.response.statusText);
                    }
                });
            }
        }
    });
</script>
</body>
</html>