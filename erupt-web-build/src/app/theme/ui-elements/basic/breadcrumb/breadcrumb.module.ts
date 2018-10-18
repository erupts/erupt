import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BreadcrumbComponent } from './breadcrumb.component';
import {BreadcrumbRoutingModule} from './breadcrumb-routing.module';
import {SharedModule} from '../../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    BreadcrumbRoutingModule,
    SharedModule
  ],
  declarations: [BreadcrumbComponent]
})
export class BreadcrumbModule { }
