import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TableEditComponent } from './table-edit.component';
import {TableEditRoutingModule} from './table-edit-routing.module';
import {SharedModule} from '../../../../shared/shared.module';
import {NgxDatatableModule} from '@swimlane/ngx-datatable';

@NgModule({
  imports: [
    CommonModule,
    TableEditRoutingModule,
    SharedModule,
    NgxDatatableModule
  ],
  declarations: [TableEditComponent]
})
export class TableEditModule { }
