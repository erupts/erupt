import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SelectionComponent } from './selection.component';
import { CellComponent } from './cell/cell.component';
import { CheckboxComponent } from './checkbox/checkbox.component';
import { MultiRowsComponent } from './multi-rows/multi-rows.component';
import { SingleRowComponent } from './single-row/single-row.component';
import {SelectionRoutingModule} from './selection-routing.module';
import {SharedModule} from '../../../../shared/shared.module';
import {NgxDatatableModule} from '@swimlane/ngx-datatable';

@NgModule({
  imports: [
    CommonModule,
    SelectionRoutingModule,
    SharedModule,
    NgxDatatableModule
  ],
  declarations: [SelectionComponent, CellComponent, CheckboxComponent, MultiRowsComponent, SingleRowComponent]
})
export class SelectionModule { }
