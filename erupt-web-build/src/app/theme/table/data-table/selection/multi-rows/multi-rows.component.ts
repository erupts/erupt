import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-multi-rows',
  templateUrl: './multi-rows.component.html',
  styleUrls: [
    './multi-rows.component.scss',
    '../../../../../../assets/icon/icofont/css/icofont.scss'
  ]
})
export class MultiRowsComponent implements OnInit {
  rows = [];
  selected = [];

  columns: any[] = [
    { prop: 'name'} ,
    { name: 'Company' },
    { name: 'Gender' }
  ];

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

  onSelect({ selected }) {
    this.selected.splice(0, this.selected.length);
    this.selected.push(...selected);
  }

  onActivate(event) {}

}
