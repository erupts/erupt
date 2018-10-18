
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TaskDetailsComponent } from './task-details.component';
import {SharedModule} from '../../../shared/shared.module';
import {TaskDetailsRoutingModule} from './task-details-routing.module';
import {UiSwitchModule} from 'ng2-ui-switch';

@NgModule({
  imports: [
    CommonModule,
    TaskDetailsRoutingModule,
    SharedModule,
    UiSwitchModule
  ],
  declarations: [TaskDetailsComponent]
})
export class TaskDetailsModule { }
