import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TaskIssueComponent } from './task-issue.component';
import {TaskIssueRoutingModule} from './task-issue-routing.module';
import {SharedModule} from '../../../shared/shared.module';
import {NgxDatatableModule} from '@swimlane/ngx-datatable';

@NgModule({
  imports: [
    CommonModule,
    TaskIssueRoutingModule,
    SharedModule,
    NgxDatatableModule
  ],
  declarations: [TaskIssueComponent]
})
export class TaskIssueModule { }
