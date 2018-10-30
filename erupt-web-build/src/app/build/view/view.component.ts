import {Component, OnInit} from '@angular/core';
import {MatTableDataSource} from "@angular/material";
import {SelectionModel} from "@angular/cdk/collections";
import {DataService} from "../../erupt/service/DataService";
import {TableColumn} from "@swimlane/ngx-datatable";
import {Page} from "../../erupt/model/page";
import {EruptModel} from "../../erupt/model/erupt.model";
import {EruptFieldModel} from "../../erupt/model/erupt-field.model";

@Component({
  selector: 'app-list-view',
  templateUrl: './view.component.html',
  styleUrls: ['./view.component.scss']
})
export class ViewComponent implements OnInit {

  eruptSearchFields: Array<EruptFieldModel> = [];

  constructor(private data: DataService) {
  }

  eruptModel: EruptModel;

  page: Page;

  rows;

  isSearchCollapsed: boolean = false;


  columns = [];

  ngOnInit() {
    this.page = {
      pageNumber: 1,
      pageSize: 50,
      total: 100
    };
    this.data.getEruptBuild("mmo").subscribe(
      em => {
        console.log(em);
        this.eruptModel = em;

        em.eruptFieldModels.forEach((field, i) => {
          if (field.eruptFieldJson.edit.search.isSearch) {
            this.eruptSearchFields.push(field);
          }

          field.eruptFieldJson.views.forEach((view, ii) => {
            this.columns.push({
              name: view.title,
              prop: view.column || field.fieldName,
              sort: true
            });
          });
        });
        console.log(this.eruptSearchFields);
        console.log(this.columns);
      },
      error => {
        console.log(error);
      }
    )
    ;


    this.fun(data => {
      this.rows = data;
    });
  }


  fun(cb) {
    const req = new XMLHttpRequest();
    req.open('GET', `assets/company.json`);

    req.onload = () => {
      cb(JSON.parse(req.response));
    };

    req.send();
  }


}

