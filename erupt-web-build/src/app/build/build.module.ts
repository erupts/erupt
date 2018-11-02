import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {BuildRoutingModule} from "./build-routing.module";
import {EditComponent} from "./edit/edit.component";
import {SharedModule} from "../shared/shared.module";
import {EruptModule} from "../erupt/erupt.module";
import {
  MatButtonModule,
  MatCardModule,
  MatCheckboxModule, MatExpansionModule, MatMenuModule, MatRippleModule,
  MatTabsModule, MatToolbarModule
} from "@angular/material";
import {TableComponent} from './table/table.component';
import {NgxDatatableModule} from "@swimlane/ngx-datatable";
import {CollapseModule} from "ngx-bootstrap";

@NgModule({
  imports: [
    CommonModule,
    SharedModule,
    BuildRoutingModule,
    EruptModule,
    MatTabsModule,
    NgxDatatableModule,
    MatCheckboxModule,
    MatExpansionModule,
    MatCardModule,
    MatRippleModule,
    MatToolbarModule,
    CollapseModule,
    MatRippleModule,
    MatButtonModule,
    MatMenuModule
  ],
  declarations: [EditComponent, TableComponent]
})
export class BuildModule {

}
