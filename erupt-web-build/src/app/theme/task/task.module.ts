import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {TaskRoutingModule} from './task-routing.module';
import {SharedModule} from '../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    TaskRoutingModule,
    SharedModule
  ],
  declarations: []
})
export class TaskModule { }
