/**
 * Created by liyuepeng on 10/17/18.
 */
export enum UiColor {
  DEFAULT, primary, success, info, warning, danger, inverse
}
export enum RgbColor {
  not, yellow, green, red, blue, white, black
}

export enum EditType {
  INPUT = 1,            //输入框
  CHOICE = 2,           //选择框
  DATE = 3,             //日期
  REFERENCE = 4,        //引用
  BOOLEAN = 5,          //布尔
  DICT = 6,             //字典
  IMAGE = 7,            //图片
  QRCODE = 8,           //二维码
  ATTACHMENT = 9,       //附件
  TAB = 10              //TAB选项卡
}

export enum DateEnum {
  DATE, DATE_TIME, MONTH
}

export enum InputEnum {
  TEXT, EMAIL, TEXTAREA, TAG
}

export enum ChoiceEnum {
  SINGLE, MULTI
}
