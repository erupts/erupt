<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            height: 100vh;
            display: flex;
            flex-direction: column;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", "PingFang SC", "Microsoft YaHei", sans-serif;
            background: #fff;
            color: rgba(0, 0, 0, .85);
        }

        .toolbar {
            display: flex;
            align-items: center;
            padding: 0 16px;
            border-bottom: 1px solid #f0f0f0;
            flex-shrink: 0;
        }

        .tabs {
            display: flex;
            flex: 1;
            gap: 28px;
            overflow-x: auto;
        }

        .tab {
            display: inline-flex;
            align-items: center;
            gap: 7px;
            padding: 13px 2px 11px 2px;
            font-size: 14px;
            color: rgba(0, 0, 0, .65);
            cursor: pointer;
            user-select: none;
            border-bottom: 2px solid transparent;
            white-space: nowrap;
            transition: color .2s;
        }

        .tab:hover {
            color: #1677ff;
        }

        .tab .count {
            font-size: 12px;
            line-height: 18px;
            padding: 0 8px;
            border-radius: 9px;
            background: rgba(0, 0, 0, .05);
            color: rgba(0, 0, 0, .45);
            transition: all .2s;
        }

        .tab.active {
            color: #1677ff;
            font-weight: 500;
            border-bottom-color: #1677ff;
        }

        .tab.active .count {
            background: #e6f4ff;
            color: #1677ff;
        }

        .search-box {
            position: relative;
            margin-left: 16px;
        }

        .search-box svg {
            position: absolute;
            left: 10px;
            top: 50%;
            transform: translateY(-50%);
            width: 14px;
            height: 14px;
            fill: rgba(0, 0, 0, .3);
            pointer-events: none;
        }

        .search {
            width: 170px;
            padding: 5px 10px 5px 30px;
            border: 1px solid #d9d9d9;
            border-radius: 6px;
            outline: none;
            font-size: 13px;
            color: rgba(0, 0, 0, .85);
            transition: all .2s;
        }

        .search::placeholder {
            color: rgba(0, 0, 0, .25);
        }

        .search:hover {
            border-color: #4096ff;
        }

        .search:focus {
            border-color: #1677ff;
            box-shadow: 0 0 0 2px rgba(22, 119, 255, .1);
        }

        .content {
            flex: 1;
            overflow: auto;
            padding: 14px 16px;
        }

        .items {
            display: none;
            grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
            gap: 8px;
        }

        .items.active {
            display: grid;
        }

        .item {
            background: #fafafa;
            border: 1px solid #f0f0f0;
            border-radius: 6px;
            padding: 7px 12px;
            font-size: 13px;
            line-height: 20px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            transition: all .2s;
        }

        .item:hover {
            border-color: #91caff;
            background: #f0f7ff;
            color: #1677ff;
        }

        .empty {
            display: none;
            text-align: center;
            color: rgba(0, 0, 0, .25);
            padding: 70px 0;
            font-size: 13px;
        }

        .empty svg {
            width: 48px;
            height: 48px;
            fill: rgba(0, 0, 0, .12);
            display: block;
            margin: 0 auto 10px auto;
        }
    </style>
</head>
<body>
<div class="toolbar">
    <div class="tabs">
        <#list resources?keys as key>
            <div class="tab" data-group="${key?html}">${key?html}<span class="count">${resources[key]?size}</span></div>
        </#list>
    </div>
    <label class="search-box">
        <svg viewBox="0 0 1024 1024"><path d="M909.6 854.5L649.9 594.8C690.2 542.7 712 479 712 412c0-80.2-31.3-155.4-87.9-212.1-56.6-56.7-132-87.9-212.1-87.9s-155.5 31.3-212.1 87.9C143.2 256.5 112 331.8 112 412c0 80.1 31.3 155.5 87.9 212.1C256.5 680.8 331.8 712 412 712c67 0 130.6-21.8 182.7-62l259.7 259.6a8.2 8.2 0 0 0 11.6 0l43.6-43.5a8.2 8.2 0 0 0 0-11.6zM570.4 570.4C528 612.7 471.8 636 412 636s-116-23.3-158.4-65.6C211.3 528 188 471.8 188 412s23.3-116.1 65.6-158.4C296 211.3 352.2 188 412 188s116.1 23.2 158.4 65.6S636 352.2 636 412s-23.3 116.1-65.6 158.4z"/></svg>
        <input class="search" id="search" placeholder="Search...">
    </label>
</div>
<div class="content">
    <#list resources?keys as key>
        <div class="items" data-group="${key?html}">
            <#list resources[key] as item>
                <div class="item" title="${item?html}">${item?html}</div>
            </#list>
        </div>
    </#list>
    <div class="empty" id="empty">
        <svg viewBox="0 0 1024 1024"><path d="M855.6 427.2H168.5c-12.7 0-24.4 6.9-30.6 18L4.4 684.7C1.5 689.9 0 695.8 0 701.8v287.1c0 19.4 15.7 35.1 35.1 35.1h953.8c19.4 0 35.1-15.7 35.1-35.1V701.8c0-6-1.5-11.8-4.4-17.1L886.2 445.2c-6.2-11.1-17.9-18-30.6-18zM315.2 704H184.4l102.9-185.8h449.4L839.6 704H708.8c-19.4 0-35.1 15.7-35.1 35.1 0 44.3-36 80.3-80.3 80.3H430.6c-44.3 0-80.3-36-80.3-80.3 0-19.4-15.7-35.1-35.1-35.1z"/></svg>
        No Data
    </div>
</div>
<script>
    (function () {
        var tabs = Array.prototype.slice.call(document.querySelectorAll('.tab'));
        var lists = Array.prototype.slice.call(document.querySelectorAll('.items'));
        var empty = document.getElementById('empty');
        var search = document.getElementById('search');
        var active = '${active?js_string}';
        if (!tabs.some(function (t) { return t.dataset.group === active; }) && tabs.length > 0) {
            active = tabs[0].dataset.group;
        }

        function render() {
            var keyword = search.value.toLowerCase();
            tabs.forEach(function (tab) {
                tab.classList.toggle('active', tab.dataset.group === active);
            });
            var visible = 0;
            lists.forEach(function (list) {
                var isActive = list.dataset.group === active;
                list.classList.toggle('active', isActive);
                var count = 0;
                Array.prototype.slice.call(list.children).forEach(function (item) {
                    var match = !keyword || item.textContent.toLowerCase().indexOf(keyword) >= 0;
                    item.style.display = match ? '' : 'none';
                    if (match) count++;
                });
                var tab = tabs.filter(function (t) { return t.dataset.group === list.dataset.group; })[0];
                if (tab) tab.querySelector('.count').textContent = count;
                if (isActive) visible = count;
            });
            empty.style.display = (tabs.length === 0 || visible === 0) ? 'block' : 'none';
        }

        tabs.forEach(function (tab) {
            tab.addEventListener('click', function () {
                active = tab.dataset.group;
                render();
            });
        });
        search.addEventListener('input', render);
        render();
    })();
</script>
</body>
</html>
