import {Component, OnInit} from '@angular/core';
import {MatTableDataSource} from "@angular/material";
import {SelectionModel} from "@angular/cdk/collections";
import {DataService} from "../../erupt/service/DataService";
import {TableColumn} from "@swimlane/ngx-datatable";

@Component({
  selector: 'app-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss']
})
export class ListComponent implements OnInit {

  constructor(private data: DataService) {
  }

  rows;

  columns: Array<TableColumn> = [];

  ngOnInit() {
    this.data.getEruptBuild("mmo").subscribe(
      em => {
        em.eruptFieldModels.forEach((field, i) => {
          field.eruptFieldJson.views.forEach((view, ii) => {
            this.columns.push({
              name: view.title,
              prop: view.column || field.fieldName
            });
          });
        });
        console.log(this.columns);
      },
      error => {
        console.log(error);
      }
    );


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

