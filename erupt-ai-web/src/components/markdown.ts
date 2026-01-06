import MarkdownIt from 'markdown-it';
import hljs from 'highlight.js';
import 'highlight.js/styles/default.css';
// import emoji from 'markdown-it-emoji';
import footnote from 'markdown-it-footnote';
import mark from 'markdown-it-mark';
import sub from 'markdown-it-sub';
import sup from 'markdown-it-sup';
import ins from 'markdown-it-ins';
import abbr from 'markdown-it-abbr';
import deflist from 'markdown-it-deflist';
import linkAttributes from 'markdown-it-link-attributes';
import taskLists from 'markdown-it-task-lists';
import markdownItKatex from 'markdown-it-katex';
import 'katex/dist/katex.min.css';

// 创建 markdown-it 实例并加载插件
const md = new MarkdownIt({
    html: true,
    breaks: true,
    linkify: true,
    typographer: true,
    highlight: (str: string, lang: string) => {
        if (lang && hljs.getLanguage(lang)) {
            return `<pre class="hljs"><code>${hljs.highlight(str, {language: lang}).value}</code></pre>`;
        }
        return `<pre class="hljs"><code>${md.utils.escapeHtml(str)}</code></pre>`;
    }
})
    // .use(emoji)
    .use(footnote)
    .use(mark)
    .use(sub)
    .use(sup)
    .use(ins)
    .use(abbr)
    .use(deflist)
    .use(linkAttributes, {attrs: {target: '_blank', rel: 'noopener'}})
    .use(taskLists)
    .use(markdownItKatex);

export default md;