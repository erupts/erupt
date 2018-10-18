import { NgModule } from '@angular/core';
import {RouterModule, Routes} from '@angular/router';


const routes: Routes = [
  {
    path: '',
    data: {
      title: 'User',
      status: false
    },
    children: [
      {
        path: 'list',
        loadChildren: './task-list/task-list.module#TaskListModule'
      },
      {
        path: 'board',
        loadChildren: './task-board/task-board.module#TaskBoardModule'
      },
      {
        path: 'details',
        loadChildren: './task-details/task-details.module#TaskDetailsModule'
      },
      {
        path: 'issue',
        loadChildren: './task-issue/task-issue.module#TaskIssueModule'
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TaskRoutingModule { }
