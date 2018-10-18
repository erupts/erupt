import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-paging',
  templateUrl: './paging.component.html',
  styleUrls: [
    './paging.component.scss',
    '../../../../../assets/icon/icofont/css/icofont.scss'
  ]
})
export class PagingComponent implements OnInit {
  rowsClient = [];

  constructor() {
    this.fetch((data) => {
      this.rowsClient = data;
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

}
