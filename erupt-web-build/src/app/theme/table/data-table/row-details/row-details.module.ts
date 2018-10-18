import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RowDetailsComponent } from './row-details.component';
import {RowDetailsRoutingModule} from './row-details-routing.module';
import {SharedModule} from '../../../../shared/shared.module';
import {NgxDatatableModule} from '@swimlane/ngx-datatable';

@NgModule({
  imports: [
    CommonModule,
    RowDetailsRoutingModule,
    SharedModule,
    NgxDatatableModule
  ],
  declarations: [RowDetailsComponent]
})
export class RowDetailsModule { }
