import {Component, Input, OnInit} from '@angular/core';
import {Edit} from "../model/erupt-field.model";
import {EditType} from "../model/erupt.enum";


@Component({
  selector: 'erupt-edit-type',
  templateUrl: './edit-type.component.html',
  styleUrls: ['./edit-type.component.scss']
})
export class EditTypeComponent implements OnInit {

  @Input() edit: Edit;

  editType = EditType;

  basicColor = "#fff";

  checkBoxChange = function ($event, val) {
    this.edit.$value = [];
    console.log($event.check());
    if ($event.check()) {
      this.edit.$value.push(val);
    } else {

    }

    console.log(this.edit.$value);
  }

  constructor() {
  }

  ngOnInit() {
    // $('input[maxlength]').maxlength();
  }

}
