import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {TaskBoardComponent} from './task-board.component';

const routes: Routes = [
  {
    path: '',
    component: TaskBoardComponent,
    data: {
      title: 'Task Board',
      icon: 'icon-check-box',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - task board',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TaskBoardRoutingModule { }
