<html>
<head>
    <base href="${base}/">
    <meta charset="UTF-8">
    <link rel="stylesheet" href="ant-design/antd.min.css">
</head>
<body>
<div id="app">
    <template>
        <a-list bordered :data-source="instances">
            <a-list-item slot="renderItem" slot-scope="item, index">
                <div style="display: flex;justify-content: space-between;align-items: center;width: 100%">
                    <span>{{item}}</span>
                    <span>
                        <a-popconfirm title="确定要移除该实例吗？" @confirm="confirm">
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
            confirm() {
                alert(123)
            }
        }
    });
</script>
</body>
</html>