import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {TaskIssueComponent} from './task-issue.component';

const routes: Routes = [
  {
    path: '',
    component: TaskIssueComponent,
    data: {
      title: 'Task Issue',
      icon: 'icon-check-box',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - task issue',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TaskIssueRoutingModule { }
