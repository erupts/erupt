import {Component, Input, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {Edit, EruptFieldModel, ReferenceType} from "../model/erupt-field.model";
import {ChoiceEnum, EditType} from "../model/erupt.enum";
import {BsModalRef, BsModalService} from "ngx-bootstrap";
import {MatDatepickerInputEvent, MatDialog, MatSnackBar} from "@angular/material";
import * as moment from "moment";
import {DataService} from "../service/DataService";
@Component({
  selector: 'erupt-edit-type',
  templateUrl: './edit-type.component.html',
  styleUrls: ['./edit-type.component.scss']
})
export class EditTypeComponent implements OnInit {

  @Input() eruptFieldModels: EruptFieldModel;


  @Input() colClass: string;

  editType = EditType;

  choiceEnum = ChoiceEnum;

  edit: Edit;

  referenceLists: Array<ReferenceType> = [];

  modalRef: BsModalRef;

  zhDate = "YYYY-MM-DD";
  zhDateTime = "YYYY-MM-DD HH:mm:ss";


  constructor(private dataService: DataService, private modalService: BsModalService, public snackBar: MatSnackBar) {

  }


  checkRefValue(edit: Edit) {
    if (!edit.referenceType[0].tempVal) {
      this.snackBar.open("未选中数据项", "warning", {
        duration: 2000
      });
      return;
    }
    edit.$value = edit.referenceType[0].tempVal.id;
    edit.$viewValue = edit.referenceType[0].tempVal.label;
    this.modalRef.hide();
  }

  openModal(template: TemplateRef<any>) {
    this.modalRef = this.modalService.show(
      template,
      // Object.assign({}, { class: 'gray modal-lg' })
    );
  }

  ngOnInit() {

  }

  dateChange(event: MatDatepickerInputEvent<Date>, field: EruptFieldModel) {
    field.eruptFieldJson.edit.$value = event.value;
    console.log(field.eruptFieldJson.edit.$value);
    console.log(moment(event.value).format(this.zhDateTime).toString());
  }


  queryReference(field: EruptFieldModel) {
    field.eruptFieldJson.edit.referenceType[0].tempVal = null;
    this.dataService.queryEruptReferenceData("submmo", field.fieldName).subscribe(data => {
      this.referenceLists = data;
    });
  }

  addValue(value) {
    alert(value);
  }

  clearValue(field: EruptFieldModel, event: Event) {
    if (event) {
      event.stopPropagation();
    }
    field.eruptFieldJson.edit.$value = null;
    field.eruptFieldJson.edit.$viewValue = null;
  }

  checkBoxChange = function ($event, val) {
    this.edit.$value = [];
    console.log($event.check());
    if ($event.check()) {
      this.edit.$value.push(val);
    } else {

    }
  };

}
