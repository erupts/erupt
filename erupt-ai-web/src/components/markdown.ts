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
import mermaid from 'mermaid';

// 初始化 mermaid
// if (typeof window !== 'undefined') {
//     mermaid.initialize({
//         startOnLoad: true,
//         theme: 'default',
//         securityLevel: 'loose'
//     });
// }

function preprocessLatex(text: string): string {
    // 1. 先将 ```latex 代码块的内容提取出来（保留原始内容）
    text = text.replace(/```latex\s*([\s\S]*?)```/g, (_match, content) => {
        return '\n' + content.trim() + '\n';
    });

    // 2. 统一将 \[ ... \] 转换为 $$...$$（LaTeX 块级公式）
    text = text.replace(/\\\[([\s\S]*?)\\\]/g, (_match, formula) => {
        return '$$' + formula.trim() + '$$';
    });

    // 3. 将 \( ... \) 转换为 $...$（LaTeX 行内公式）
    text = text.replace(/\\\(([\s\S]*?)\\\)/g, (_match, formula) => {
        return '$' + formula.trim() + '$';
    });

    return text;
}

// 创建 markdown-it 实例并加载插件
const md = new MarkdownIt({
    html: true,
    breaks: true,
    linkify: true,
    typographer: true,
    highlight: (str: string, lang: string) => {
        // 处理 mermaid 图表
        if (lang === 'mermaid') {
            return `<div class="mermaid">${str}</div>`;
        }

        // 处理代码高亮
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
    .use(markdownItKatex, {
        throwOnError: false,
        errorColor: '#cc0000'
    });

// 重写 render 方法，添加预处理
const originalRender = md.render.bind(md);
md.render = function (text: string, env?: any): string {
    const preprocessedText = preprocessLatex(text);
    const html = originalRender(preprocessedText, env);

    if (html.indexOf('<div class="mermaid">') !== -1) {
        setTimeout(() => {
            mermaid.run({
                querySelector: '.mermaid'
            });
        }, 0);
    }

    return html;
};

export default md;