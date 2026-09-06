# erupt-tpl-ui · Element Plus

Frontend static assets for Erupt based on **Element Plus** (v2.14.5) on **Vue** (v3.5.42, global build with the runtime template compiler), plus **Element Plus Icons** (v2.3.2).

| File | Global | Notes |
|---|---|---|
| `vue3.js` | `Vue` | production build, full bundle so the runtime template compiler is included |
| `element.min.js` | `ElementPlus` | install with `app.use(ElementPlus)` |
| `element.min.css` | | |
| `element-icons.min.js` | `ElementPlusIconsVue` | no install function; register with `app.component` |
| `axios.min.js` | `axios` | |

Packages the compiled Element Plus admin UI as a Spring Boot static resource module. Include this dependency to serve the Element Plus-flavored Erupt frontend from your application without any separate frontend build step.
