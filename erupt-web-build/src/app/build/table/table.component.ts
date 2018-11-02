import {Component, OnInit} from '@angular/core';
import {MatTableDataSource} from "@angular/material";
import {SelectionModel} from "@angular/cdk/collections";
import {DataService} from "../../erupt/service/DataService";
import {TableColumn} from "@swimlane/ngx-datatable";
import {Page} from "../../erupt/model/page";
import {EruptModel} from "../../erupt/model/erupt.model";
import {EruptFieldModel, View} from "../../erupt/model/erupt-field.model";
import {eruptModelConverViews} from "../../erupt/util/ConverUtil";

@Component({
  selector: 'app-list-view',
  templateUrl: './table.component.html',
  styleUrls: ['./table.component.scss']
})
export class TableComponent implements OnInit {

  eruptSearchFields: Array<EruptFieldModel> = [];

  constructor(private dataService: DataService) {
  }

  eruptModel: EruptModel;

  page: Page;

  rows;

  isSearchCollapsed: boolean = false;


  views: Array<View> = [];

  editRow(event: Event, value) {
    event.stopPropagation();
    alert("修改");
    console.log(value);
  }

  delRow(event: Event, value) {
    event.stopPropagation();
  }

  ngOnInit() {
    this.page = {
      pageNumber: 1,
      pageSize: 2,
      total: 2
    };
    this.dataService.getEruptBuild("submmo").subscribe(
      em => {
        this.eruptModel = em;
        console.log(this.eruptModel);
        this.views = eruptModelConverViews(em);
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

    this.dataService.queryEruptData("submmo").subscribe(
      data => {
        console.log(data);
        this.rows = data.list;
      },
      error => {

      }
    );

  }


}

