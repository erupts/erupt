import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-table-edit',
  templateUrl: './table-edit.component.html',
  styleUrls: [
    './table-edit.component.scss',
    '../../../../../assets/icon/icofont/css/icofont.scss'
  ]
})
export class TableEditComponent implements OnInit {
  editing = {};
  rows = [];

  constructor() {
    this.fetch((data) => {
      this.rows = data;
    });
  }

  ngOnInit() {
  }

  fetch(cb) {
    const req = new XMLHttpRequest();
    req.open('GET', `assets/data/company.json`);

    req.onload = () => {
      cb(JSON.parse(req.response));
    };

    req.send();
  }

  updateValue(event, cell, row) {
    this.editing[row + '-' + cell] = false;
    this.rows[row][cell] = event.target.value;
  }

}
