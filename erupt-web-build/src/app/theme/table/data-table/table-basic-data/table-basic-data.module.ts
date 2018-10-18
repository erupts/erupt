import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TableBasicDataComponent } from './table-basic-data.component';
import {TableBasicDataRoutingModule} from './table-basic-data-routing.module';
import {SharedModule} from '../../../../shared/shared.module';
import {NgxDatatableModule} from '@swimlane/ngx-datatable';

@NgModule({
  imports: [
    CommonModule,
    TableBasicDataRoutingModule,
    SharedModule,
    NgxDatatableModule
  ],
  declarations: [TableBasicDataComponent]
})
export class TableBasicDataModule { }
