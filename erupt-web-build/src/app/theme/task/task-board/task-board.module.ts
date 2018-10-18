import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TaskBoardComponent } from './task-board.component';
import {TaskBoardRoutingModule} from './task-board-routing.module';
import {SharedModule} from '../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    TaskBoardRoutingModule,
    SharedModule
  ],
  declarations: [TaskBoardComponent]
})
export class TaskBoardModule { }
