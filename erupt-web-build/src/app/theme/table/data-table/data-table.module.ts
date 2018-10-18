import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {DataTableRoutingModule} from './data-table-routing.module';
import {SharedModule} from '../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    DataTableRoutingModule,
    SharedModule
  ],
  declarations: []
})
export class DataTableModule { }
