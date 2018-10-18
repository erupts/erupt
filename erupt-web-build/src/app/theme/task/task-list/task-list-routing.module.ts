import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {TaskListComponent} from './task-list.component';

const routes: Routes = [
  {
    path: '',
    component: TaskListComponent,
    data: {
      title: 'Task List',
      icon: 'icon-check-box',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - task list',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TaskListRoutingModule { }
