import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-task-list',
  templateUrl: './task-list.component.html',
  styleUrls: [
    './task-list.component.scss',
    '../../../../assets/icon/icofont/css/icofont.scss'
  ]
})
export class TaskListComponent implements OnInit {
  rowsTask = [];
  loadingIndicator = true;
  reorderable = true;

  constructor() {
    this.fetchTaskData((data) => {
      this.rowsTask = data;
      setTimeout(() => { this.loadingIndicator = false; }, 1500);
    });
  }

  ngOnInit() {
  }

  fetchTaskData(cb) {
    const req = new XMLHttpRequest();
    req.open('GET', 'assets/data/task-list.json');

    req.onload = () => {
      cb(JSON.parse(req.response));
    };

    req.send();
  }

}
