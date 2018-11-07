import {View} from "../model/erupt-field.model";
import {EruptModel} from "../model/erupt.model";
import {EditType} from "../model/erupt.enum";
/**
 * Created by liyuepeng on 10/31/18.
 */

//将eruptModel中的列全部取出，且组成有结构性的查询串
export function eruptModelConverViewKeys(eruptModel: EruptModel): Array<View> {
  let views: Array<View> = [];
  eruptModel.eruptFieldModels.forEach(subField => {
    subField.eruptFieldJson.views.forEach(view => {
      if (view.column) {
        view.column = subField.fieldName + "_" + view.column.replace("\.", "_");
      } else {
        view.column = subField.fieldName;
      }
      views.push(view);
    });
  });
  return views;
}

//将eruptModel中的内容拼接成后台需要的json格式
export function eruptValueToObject(eruptModel: EruptModel): any {
  let eruptData: any = {};
  eruptModel.eruptFieldModels.forEach(field => {
    if (field.eruptFieldJson.edit.type === EditType.REFERENCE) {
      eruptData[field.fieldName] = {};
      eruptData[field.fieldName][field.eruptFieldJson.edit.referenceType[0].id] = field.eruptFieldJson.edit.$value;
    } else {
      eruptData[field.fieldName] = field.eruptFieldJson.edit.$value;
    }
  });
  return eruptData;
}

