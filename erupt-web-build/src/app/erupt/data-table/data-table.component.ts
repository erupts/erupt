import {Component, Input, OnInit} from '@angular/core';
import {TableColumn} from "@swimlane/ngx-datatable";
import {Page} from "../model/page";

@Component({
  selector: 'erupt-data-table',
  templateUrl: './data-table.component.html',
  styleUrls: ['./data-table.component.scss']
})
export class DataTableComponent implements OnInit {

  @Input() columns: Array<TableColumn>;

  @Input() rows;

  @Input() height;

  @Input() page: Page;

  @Input() theme: String = 'material';

  constructor() {
    console.log(this.theme);
  }

  ngOnInit() {
  }

}
