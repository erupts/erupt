import {Component, OnInit} from '@angular/core';
import {EruptModel} from "../../erupt/model/erupt.model";
import {DataService} from "../../erupt/service/DataService";
import {EditType} from "../../erupt/model/erupt.enum";
import {eruptModelConverViews} from "../../erupt/util/ConverUtil";
import {EruptFieldModel, TabType} from "../../erupt/model/erupt-field.model";

@Component({
  selector: 'erupt-edit',
  templateUrl: './edit.component.html',
  styleUrls: ['./edit.component.scss']
})
export class EditComponent implements OnInit {

  eruptModel: EruptModel;

  groups: Set<string> = new Set;

  editType = EditType;

  constructor(private data: DataService) {

  }

  ngOnInit() {
    this.data.getEruptBuild("submmo").subscribe(
      em => {
        //计算里面所有的字段信息
        for (let field of em.eruptFieldModels) {
          /**
           * TAB control
           */
          if (field.eruptFieldJson.edit.type === EditType.TAB) {
            if (!field.eruptFieldJson.edit.tabType) {
              field.eruptFieldJson.edit.tabType = [];
            }
            field.eruptFieldJson.edit.tabType[0] = {};
            console.log(field.eruptFieldJson.edit.tabType[0]);
            this.data.getEruptBuild(field.fieldReturnName).subscribe(subErupt => {
              field.eruptFieldJson.edit.tabType[0].eruptFieldModels = subErupt.eruptFieldModels;
              field.eruptFieldJson.edit.tabType[0].views = eruptModelConverViews(subErupt);
            });
          }

          this.groups.add(field.eruptFieldJson.edit.group);
        }
        this.eruptModel = em;

      },
      error => {
        console.log(error);
      }
    );

  }

}
