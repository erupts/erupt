import {ChoiceEnum, DateEnum, EditType, InputEnum, UiColor} from "./erupt.enum";
/**
 * Created by liyuepeng on 10/17/18.
 */
export interface EruptFieldModel {
  fieldName: string;
  fieldReturnName: string;
  eruptFieldJson: EruptField;
}


//field detail
interface EruptField {
  views: Array<View>;
  edit: Edit;
}

interface View {
  column: string;
  title: string;
  show: boolean;
  sort: number;
}

export interface Edit {
  title: string;
  notNull: boolean;
  $value: any;
  type: EditType;
  show: boolean;
  readOnly: boolean;
  sort: number;
  group: string;
  groupIcon: string;
  col: number;
  tabType: Array<TabType>;
  inputType: Array<InputType>;
  referenceType: Array<ReferenceType>;
  boolType: Array<BoolType>;
  choiceType: Array<ChoiceType>;
  dictType: Array<DictType>;
  dateType: Array<DateType>;
  search: Search;
}


interface Search {
  isSearch: boolean;
  isFuzzy: boolean;
}

interface TabType {
  icon: string;
}


//Edit Type
interface InputType {
  color: UiColor;
  type: InputEnum;
  length: number;
  placeholder: string;
  defaultVal: string;
  icon: string;
}

interface ReferenceType {
  id: string;
  label: string;
  filter: string;
}

interface BoolType {
  trueText: string;
  falseText: string;
  defaultValue: boolean;
}

interface ChoiceType {
  type: ChoiceEnum;
  vl: VL;
  color: string;
}

interface DictType {
  dictCode: string;
}

interface DateType {
  type: DateEnum;
  isRange: boolean;
}


interface VL {
  value: number;
  label: string;
}
