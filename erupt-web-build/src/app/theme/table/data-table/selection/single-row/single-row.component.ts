import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-single-row',
  templateUrl: './single-row.component.html',
  styleUrls: ['./single-row.component.scss']
})
export class SingleRowComponent implements OnInit {
  rows = [];
  selected = [];

  columns: any[] = [
    { prop: 'name'} ,
    { name: 'Company' },
    { name: 'Gender' }
  ];

  constructor() {
    this.fetch((data) => {
      this.selected = [data[2]];
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

  onSelect({ selected }) {}

  onActivate(event) {}

}
