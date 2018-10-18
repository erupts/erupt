import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {TaskDetailsComponent} from './task-details.component';

const routes: Routes = [
  {
    path: '',
    component: TaskDetailsComponent,
    data: {
      title: 'Task Details',
      icon: 'icon-check-box',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - task details',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TaskDetailsRoutingModule { }
