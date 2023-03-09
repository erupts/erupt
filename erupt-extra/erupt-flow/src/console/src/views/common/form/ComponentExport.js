let TextInput = () => import('./components/TextInput.vue')
let NumberInput = () => import('./components/NumberInput.vue')
let AmountInput = () => import('./components/AmountInput.vue')
let TextareaInput = () => import('./components/TextareaInput.vue')
let SelectInput = () => import('./components/SelectInput.vue')
let MultipleSelect = () => import('./components/MultipleSelect.vue')
let DateTime = () => import('./components/DateTime.vue')
let DateTimeRange = () => import('./components/DateTimeRange.vue')

let Description = () => import('./components/Description.vue')
let ImageUpload = () => import('./components/ImageUpload.vue')
let FileUpload = () => import('./components/FileUpload.vue')
let Location = () => import('./components/Location.vue')
let MoneyInput = () => import('./components/MoneyInput.vue')
let DeptPicker = () => import('./components/DeptPicker.vue')
let UserPicker = () => import('./components/UserPicker.vue')
let SignPanel = () => import('./components/SignPannel.vue')

let SpanLayout = () => import('./components/SpanLayout.vue')
let TableList = () => import('./components/TableList.vue')

export default {
  //基础组件
  TextInput, NumberInput, AmountInput, TextareaInput, SelectInput, MultipleSelect,
  DateTime, DateTimeRange, UserPicker, DeptPicker,
  //高级组件
  Description, FileUpload, ImageUpload, MoneyInput, Location, SignPanel,
  SpanLayout, TableList
}
