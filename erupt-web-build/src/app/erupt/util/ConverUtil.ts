import {View} from "../model/erupt-field.model";
import {EruptModel} from "../model/erupt.model";
/**
 * Created by liyuepeng on 10/31/18.
 */

//将eruptModel中的列全部取出，且组成有结构性的查询串
export function eruptModelConverViews(eruptModel: EruptModel): Array<View> {
  let views: Array<View> = [];
  eruptModel.eruptFieldModels.forEach(subField => {
    subField.eruptFieldJson.views.forEach(view => {
      if (!view.column) {
        view.column = subField.fieldName;
      } else {
        view.column = subField.fieldName + "." + view.column;
      }
      views.push(view);
    });
  });
  return views;
}
