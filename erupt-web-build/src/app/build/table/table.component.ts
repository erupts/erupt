import {Component, OnInit} from '@angular/core';
import {DataService} from "../../erupt/service/DataService";
import {Page} from "../../erupt/model/page";
import {EruptModel} from "../../erupt/model/erupt.model";
import {EruptFieldModel, View} from "../../erupt/model/erupt-field.model";
import {eruptModelConverViewKeys} from "../../erupt/util/conver-util";
import {MatDialog} from "@angular/material";
import {EditTypeComponent} from "../../erupt/edit-type/edit-type.component";

@Component({
  selector: 'app-list-view',
  templateUrl: './table.component.html',
  styleUrls: ['./table.component.scss']
})
export class TableComponent implements OnInit {

  eruptSearchFields: Array<EruptFieldModel> = [];

  constructor(private dataService: DataService, private dialog: MatDialog) {

  }

  eruptName: string = "submmo";

  eruptModel: EruptModel;

  page: Page;

  rows;

  isSearchCollapsed: boolean = false;


  views: Array<View> = [];

  editRow(event: Event, value) {
    this.dialog.open(EditTypeComponent);
    event.stopPropagation();
    alert("修改");
    console.log(value);
  }

  delRow(event: Event, value) {
    event.stopPropagation();
    console.log(value[this.eruptModel.primaryKeyCol]);
    this.dataService.deleteEruptData(this.eruptName, value[this.eruptModel.primaryKeyCol]).subscribe(val => {
      console.log(val);
    });
  }

  ngOnInit() {
    this.page = {
      pageNumber: 1,
      pageSize: 10,
      total: 10
    };
    this.dataService.getEruptBuild(this.eruptName).subscribe(
      em => {
        this.eruptModel = em;
        console.log(this.eruptModel);
        this.views = eruptModelConverViewKeys(em);
        em.eruptFieldModels.forEach((field, i) => {
          if (field.eruptFieldJson.edit.search.isSearch) {
            field.eruptFieldJson.edit.notNull = false;
            this.eruptSearchFields.push(field);
          }
        });
      },
      error => {
        console.log(error);
      }
    );

    this.dataService.queryEruptData(this.eruptName).subscribe(
      data => {
        console.log(data);
        this.rows = data.list;
      },
      error => {

      }
    );

  }


}

